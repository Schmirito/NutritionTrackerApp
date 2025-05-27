package com.example.nutritiontracker.ui.screens.shopping

import MainViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.nutritiontracker.data.database.entities.Ingredient
import com.example.nutritiontracker.data.database.entities.IngredientUnit
import com.example.nutritiontracker.data.database.entities.Recipe
import com.example.nutritiontracker.data.database.entities.ShoppingListItem
import com.example.nutritiontracker.utils.Constants
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShoppingListItemDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var manualEntryName by remember { mutableStateOf("") }
    var manualEntryAmount by remember { mutableStateOf("") }
    var showManualEntry by remember { mutableStateOf(false) }

    val ingredients by viewModel.ingredients.collectAsState(initial = emptyList())
    val recipes by viewModel.recipes.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    // Gefilterte Listen
    val filteredIngredients = remember(ingredients, searchQuery) {
        ingredients.filter {
            !it.name.startsWith(Constants.ManualEntry.PREFIX) &&
                    (searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true))
        }
    }

    val filteredRecipes = remember(recipes, searchQuery) {
        recipes.filter {
            searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        ),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .fillMaxHeight(0.8f),
        title = { Text("Zur Einkaufsliste hinzufügen") },
        text = {
            Column {
                // Tab Row
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        text = { Text("Zutaten") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    Tab(
                        text = { Text("Rezepte") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                    Tab(
                        text = { Text("Manuell") },
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0, 1 -> {
                        // Suchfeld für Zutaten und Rezepte
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Suchen...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Suchen") },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Löschen")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Liste
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (selectedTab == 0) {
                                // Zutaten
                                items(filteredIngredients) { ingredient ->
                                    IngredientListItem(
                                        ingredient = ingredient,
                                        onClick = {
                                            scope.launch {
                                                val amount = when (ingredient.unit) {
                                                    IngredientUnit.GRAM -> "${Constants.ShoppingList.DEFAULT_AMOUNT_GRAMS}g"
                                                    IngredientUnit.MILLILITER -> "${Constants.ShoppingList.DEFAULT_AMOUNT_GRAMS}ml"
                                                    IngredientUnit.PIECE -> "${Constants.ShoppingList.DEFAULT_AMOUNT_PIECES} Stück"
                                                }
                                                viewModel.addShoppingListItem(
                                                    ShoppingListItem(
                                                        name = ingredient.name,
                                                        amount = amount,
                                                        ingredientId = ingredient.id
                                                    )
                                                )
                                                onDismiss()
                                            }
                                        }
                                    )
                                }

                                if (filteredIngredients.isEmpty()) {
                                    item {
                                        Text(
                                            text = if (searchQuery.isEmpty()) {
                                                "Keine Zutaten vorhanden"
                                            } else {
                                                "Keine Zutaten gefunden"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            } else {
                                // Rezepte
                                items(filteredRecipes) { recipe ->
                                    RecipeListItem(
                                        recipe = recipe,
                                        onClick = {
                                            scope.launch {
                                                viewModel.addRecipeToShoppingList(recipe.id)
                                                onDismiss()
                                            }
                                        }
                                    )
                                }

                                if (filteredRecipes.isEmpty()) {
                                    item {
                                        Text(
                                            text = if (searchQuery.isEmpty()) {
                                                "Keine Rezepte vorhanden"
                                            } else {
                                                "Keine Rezepte gefunden"
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // Manuelle Eingabe
                        OutlinedTextField(
                            value = manualEntryName,
                            onValueChange = { manualEntryName = it },
                            label = { Text("Was möchten Sie kaufen?") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = manualEntryAmount,
                            onValueChange = { manualEntryAmount = it },
                            label = { Text("Menge (optional)") },
                            placeholder = { Text("z.B. 500g, 2 Stück, 1L") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (manualEntryName.isNotBlank()) {
                                    scope.launch {
                                        viewModel.addShoppingListItem(
                                            ShoppingListItem(
                                                name = manualEntryName.trim(),
                                                amount = manualEntryAmount.trim(),
                                                isManualEntry = true
                                            )
                                        )
                                        onDismiss()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = manualEntryName.isNotBlank()
                        ) {
                            Text("Hinzufügen")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@Composable
private fun IngredientListItem(
    ingredient: Ingredient,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = when (ingredient.unit) {
                        IngredientUnit.GRAM -> "pro 100g"
                        IngredientUnit.MILLILITER -> "pro 100ml"
                        IngredientUnit.PIECE -> "pro Stück"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.Add,
                contentDescription = "Hinzufügen",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RecipeListItem(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Fügt alle Zutaten zur Liste hinzu",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Hinzufügen",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}