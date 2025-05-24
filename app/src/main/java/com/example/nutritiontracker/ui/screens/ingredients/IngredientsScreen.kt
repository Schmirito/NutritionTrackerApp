package com.example.nutritiontracker.ui.screens.ingredients

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.data.database.entities.Ingredient
import com.example.nutritiontracker.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsScreen(viewModel: MainViewModel) {
    val ingredients by viewModel.ingredients.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingIngredient by remember { mutableStateOf<Ingredient?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Gefilterte Zutaten basierend auf Suche
    val filteredIngredients = remember(ingredients, searchQuery) {
        if (searchQuery.isEmpty()) {
            ingredients
        } else {
            ingredients.filter { ingredient ->
                ingredient.name.contains(searchQuery, ignoreCase = true) ||
                        ingredient.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zutaten") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zutat hinzufügen")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Suchleiste
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Zutaten suchen...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Suchen") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Löschen")
                        }
                    }
                },
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredIngredients) { ingredient ->
                    IngredientCard(
                        ingredient = ingredient,
                        onEdit = { editingIngredient = ingredient },
                        onDelete = { viewModel.deleteIngredient(ingredient) }
                    )
                }

                if (filteredIngredients.isEmpty() && searchQuery.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Keine Zutaten gefunden für:\n\"$searchQuery\"",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (ingredients.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Keine Zutaten vorhanden.\nTippen Sie auf + um eine hinzuzufügen.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Spacer für FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddDialog || editingIngredient != null) {
        IngredientDialog(
            ingredient = editingIngredient,
            onDismiss = {
                showAddDialog = false
                editingIngredient = null
            },
            onSave = { ingredient ->
                if (editingIngredient != null) {
                    viewModel.updateIngredient(ingredient)
                } else {
                    viewModel.addIngredient(ingredient)
                }
                showAddDialog = false
                editingIngredient = null
            }
        )
    }
}