
package com.example.nutritiontracker.ui.screens.diary

import MainViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.nutritiontracker.data.database.entities.DiaryEntry
import com.example.nutritiontracker.data.database.entities.Ingredient
import com.example.nutritiontracker.data.database.entities.IngredientUnit
import com.example.nutritiontracker.data.database.entities.Recipe
import com.example.nutritiontracker.data.models.EntryType
import com.example.nutritiontracker.data.models.MealType
import com.example.nutritiontracker.utils.MealTypeUtils
import com.example.nutritiontracker.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDiaryEntryDialog(
    viewModel: MainViewModel,
    initialMealType: MealType,
    date: Long,
    onDismiss: () -> Unit
) {
    var selectedMealType by remember { mutableStateOf(initialMealType) }
    var entryType by remember { mutableStateOf(EntryType.INGREDIENT) }
    var selectedItem by remember { mutableStateOf<Any?>(null) }
    var amount by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showManualEntry by remember { mutableStateOf(false) }
    var showMealTypeDialog by remember { mutableStateOf(false) }

    val ingredients by viewModel.ingredients.collectAsState(initial = emptyList())
    val recipes by viewModel.recipes.collectAsState(initial = emptyList())

    // Gefilterte Listen basierend auf Suche
    val filteredIngredients = remember(ingredients, searchQuery) {
        if (searchQuery.isEmpty()) ingredients
        else ingredients.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredRecipes = remember(recipes, searchQuery) {
        if (searchQuery.isEmpty()) recipes
        else recipes.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    if (showManualEntry) {
        ManualEntryDialog(
            viewModel = viewModel,
            mealType = selectedMealType,
            date = date,
            onDismiss = { showManualEntry = false },
            onConfirm = {
                showManualEntry = false
                onDismiss()
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f),
            title = { Text("Eintrag hinzufügen") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Entry type selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(
                            selected = entryType == EntryType.INGREDIENT,
                            onClick = {
                                entryType = EntryType.INGREDIENT
                                selectedItem = null
                                amount = ""
                                searchQuery = ""
                            },
                            label = { Text("Zutat") }
                        )
                        FilterChip(
                            selected = entryType == EntryType.RECIPE,
                            onClick = {
                                entryType = EntryType.RECIPE
                                selectedItem = null
                                amount = ""
                                searchQuery = ""
                            },
                            label = { Text("Rezept") }
                        )
                    }

                    // Meal type selection - vereinfacht
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMealTypeDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Mahlzeit",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    text = MealTypeUtils.getMealTypeName(selectedMealType),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Text(
                                text = "▼",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    // Suchfeld
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

                    // Item selection
                    Text(
                        text = if (entryType == EntryType.INGREDIENT) "Zutat auswählen" else "Rezept auswählen",
                        style = MaterialTheme.typography.titleSmall
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        val items = if (entryType == EntryType.INGREDIENT) filteredIngredients else filteredRecipes

                        if (items.isEmpty()) {
                            item {
                                Text(
                                    text = if (searchQuery.isNotEmpty()) {
                                        "Keine Ergebnisse für \"$searchQuery\""
                                    } else if (entryType == EntryType.INGREDIENT) {
                                        "Keine Zutaten vorhanden"
                                    } else {
                                        "Keine Rezepte vorhanden"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            items(items) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedItem = item
                                            // Setze Standardwert basierend auf Einheit
                                            if (item is Ingredient) {
                                                amount = when (item.unit) {
                                                    IngredientUnit.GRAM -> "100"
                                                    IngredientUnit.MILLILITER -> "100"
                                                    IngredientUnit.PIECE -> "1"
                                                }
                                            } else {
                                                amount = "1"
                                            }
                                        }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = when(item) {
                                                is Ingredient -> item.name
                                                is Recipe -> item.name
                                                else -> ""
                                            },
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = when(item) {
                                                is Ingredient -> when(item.unit) {
                                                    IngredientUnit.GRAM -> "${item.calories.toInt()} kcal/100g"
                                                    IngredientUnit.MILLILITER -> "${item.calories.toInt()} kcal/100ml"
                                                    IngredientUnit.PIECE -> "${item.calories.toInt()} kcal/Stück"
                                                }
                                                is Recipe -> "${item.servings} ${if (item.servings == 1) "Portion" else "Portionen"}"
                                                else -> ""
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    RadioButton(
                                        selected = selectedItem == item,
                                        onClick = {
                                            selectedItem = item
                                            // Setze Standardwert basierend auf Einheit
                                            if (item is Ingredient) {
                                                amount = when (item.unit) {
                                                    IngredientUnit.GRAM -> "100"
                                                    IngredientUnit.MILLILITER -> "100"
                                                    IngredientUnit.PIECE -> "1"
                                                }
                                            } else {
                                                amount = "1"
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Amount input
                    selectedItem?.let { item ->
                        OutlinedTextField(
                            value = amount,
                            onValueChange = {
                                when {
                                    item is Ingredient && item.unit == IngredientUnit.PIECE -> {
                                        // Nur ganze Zahlen für Stückzahl
                                        amount = it.filter { char -> char.isDigit() }
                                    }
                                    else -> {
                                        // Dezimalzahlen für Gramm, Milliliter und Portionen
                                        amount = it.filter { char -> char.isDigit() || char == '.' }
                                    }
                                }
                                amountError = false
                            },
                            label = {
                                Text(
                                    when {
                                        item is Ingredient && item.unit == IngredientUnit.GRAM -> "Menge (g)"
                                        item is Ingredient && item.unit == IngredientUnit.MILLILITER -> "Menge (ml)"
                                        item is Ingredient && item.unit == IngredientUnit.PIECE -> "Anzahl (Stück)"
                                        else -> "Portionen"
                                    }
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (item is Ingredient && item.unit == IngredientUnit.PIECE)
                                    KeyboardType.Number
                                else
                                    KeyboardType.Decimal
                            ),
                            isError = amountError,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Manueller Eintrag Hinweis
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showManualEntry = true }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Manuellen Eintrag erstellen",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Für Restaurantbesuche oder unbekannte Speisen",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (amount.isEmpty()) {
                            amountError = true
                            return@TextButton
                        }

                        selectedItem?.let { item ->
                            val amountValue = when {
                                item is Ingredient && item.unit == IngredientUnit.PIECE -> {
                                    amount.toIntOrNull()?.toDouble()
                                }
                                else -> {
                                    amount.toDoubleOrNull()
                                }
                            }

                            amountValue?.let { amt ->
                                val entry = DiaryEntry(
                                    date = date,
                                    mealType = selectedMealType,
                                    entryType = entryType,
                                    ingredientId = if (item is Ingredient) item.id else null,
                                    recipeId = if (item is Recipe) item.id else null,
                                    amount = amt
                                )
                                viewModel.addDiaryEntry(entry)
                                onDismiss()
                            }
                        }
                    },
                    enabled = selectedItem != null && amount.isNotEmpty()
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Abbrechen")
                }
            }
        )

        // Meal Type Selection Dialog
        if (showMealTypeDialog) {
            AlertDialog(
                onDismissRequest = { showMealTypeDialog = false },
                title = { Text("Mahlzeit auswählen") },
                text = {
                    Column {
                        MealType.values().forEach { mealType ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedMealType = mealType
                                        showMealTypeDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedMealType == mealType,
                                    onClick = {
                                        selectedMealType = mealType
                                        showMealTypeDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(MealTypeUtils.getMealTypeName(mealType))
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showMealTypeDialog = false }) {
                        Text("Abbrechen")
                    }
                }
            )
        }
    }
}