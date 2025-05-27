package com.example.nutritiontracker.ui.screens.recipes

import MainViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.data.database.entities.Recipe
import com.example.nutritiontracker.data.models.Category
import com.example.nutritiontracker.ui.screens.ingredients.CategoryFilterDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(viewModel: MainViewModel) {
    val recipes by viewModel.recipes.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRecipe by remember { mutableStateOf<Recipe?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilters by remember { mutableStateOf<List<Category>>(emptyList()) }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Gefilterte Rezepte basierend auf Suche und Kategorien
    val filteredRecipes = remember(recipes, searchQuery, selectedFilters) {
        recipes.filter { recipe ->
            val matchesSearch = searchQuery.isEmpty() ||
                    recipe.name.contains(searchQuery, ignoreCase = true) ||
                    recipe.description.contains(searchQuery, ignoreCase = true)

            val matchesFilters = selectedFilters.isEmpty() ||
                    selectedFilters.all { filter -> filter in recipe.categories }

            matchesSearch && matchesFilters
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rezepte") },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        BadgedBox(
                            badge = {
                                if (selectedFilters.isNotEmpty()) {
                                    Badge {
                                        Text(selectedFilters.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Rezept hinzufügen")
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
                placeholder = { Text("Rezepte suchen...") },
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

            // Aktive Filter anzeigen
            if (selectedFilters.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedFilters) { filter ->
                        FilterChip(
                            selected = true,
                            onClick = { selectedFilters = selectedFilters - filter },
                            label = { Text(filter.displayName) },
                            trailingIcon = { Icon(Icons.Default.Clear, contentDescription = "Entfernen", modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredRecipes) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onEdit = { editingRecipe = recipe },
                        onDelete = { viewModel.deleteRecipe(recipe) }
                    )
                }

                if (filteredRecipes.isEmpty() && (searchQuery.isNotEmpty() || selectedFilters.isNotEmpty())) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Keine Rezepte gefunden",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (recipes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Keine Rezepte vorhanden.\nTippen Sie auf + um eins hinzuzufügen.",
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

    if (showAddDialog || editingRecipe != null) {
        RecipeDialog(
            recipe = editingRecipe,
            viewModel = viewModel,
            onDismiss = {
                showAddDialog = false
                editingRecipe = null
            }
        )
    }

    if (showFilterDialog) {
        CategoryFilterDialog(
            selectedCategories = selectedFilters,
            onDismiss = { showFilterDialog = false },
            onConfirm = { categories ->
                selectedFilters = categories
                showFilterDialog = false
            }
        )
    }
}