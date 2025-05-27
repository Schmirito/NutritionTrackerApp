package com.example.nutritiontracker.ui.screens.ingredients

import MainViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.data.database.entities.Ingredient
import com.example.nutritiontracker.data.models.Category
import com.example.nutritiontracker.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsScreen(viewModel: MainViewModel) {
    val ingredients by viewModel.ingredients.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingIngredient by remember { mutableStateOf<Ingredient?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilters by remember { mutableStateOf<List<Category>>(emptyList()) }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Gefilterte Zutaten basierend auf Suche und Kategorien
    val filteredIngredients = remember(ingredients, searchQuery, selectedFilters) {
        ingredients.filter { ingredient ->
            // Verstecke manuelle Einträge in der Zutatenliste
            val isManualEntry = ingredient.name.startsWith(Constants.ManualEntry.PREFIX)
            if (isManualEntry) return@filter false

            val matchesSearch = searchQuery.isEmpty() ||
                    ingredient.name.contains(searchQuery, ignoreCase = true) ||
                    ingredient.description.contains(searchQuery, ignoreCase = true)

            val matchesFilters = selectedFilters.isEmpty() ||
                    selectedFilters.all { filter -> filter in ingredient.categories }

            matchesSearch && matchesFilters
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zutaten") },
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
                placeholder = { Text(Constants.UI.INGREDIENT_SEARCH_PLACEHOLDER) },
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
                items(filteredIngredients) { ingredient ->
                    IngredientCard(
                        ingredient = ingredient,
                        onEdit = { editingIngredient = ingredient },
                        onDelete = { viewModel.deleteIngredient(ingredient) }
                    )
                }

                if (filteredIngredients.isEmpty() && (searchQuery.isNotEmpty() || selectedFilters.isNotEmpty())) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Keine Zutaten gefunden",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterDialog(
    selectedCategories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (List<Category>) -> Unit
) {
    var tempSelectedCategories by remember { mutableStateOf(selectedCategories) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nach Kategorien filtern") },
        text = {
            Column {
                // Gruppierte Kategorien
                val groupedCategories = mapOf(
                    "Nährwerte" to listOf(
                        Category.HIGH_PROTEIN, Category.LOW_CARB, Category.LOW_FAT,
                        Category.HIGH_FIBER, Category.LOW_CALORIE
                    ),
                    "Ernährungsform" to listOf(
                        Category.VEGAN, Category.VEGETARIAN, Category.GLUTEN_FREE,
                        Category.LACTOSE_FREE, Category.KETO, Category.PALEO
                    ),
                    "Lebensmittel" to listOf(
                        Category.MEAT, Category.FISH, Category.DAIRY, Category.CHEESE,
                        Category.EGGS, Category.VEGETABLES, Category.FRUITS, Category.GRAINS,
                        Category.NUTS_SEEDS, Category.LEGUMES, Category.SWEETS, Category.BEVERAGES
                    ),
                    "Mahlzeiten" to listOf(
                        Category.BREAKFAST, Category.LUNCH, Category.DINNER,
                        Category.SNACK, Category.DESSERT
                    ),
                    "Zubereitung" to listOf(
                        Category.QUICK, Category.EASY, Category.MEAL_PREP
                    )
                )

                LazyColumn(
                    modifier = Modifier.height(400.dp)
                ) {
                    groupedCategories.forEach { (groupName, categories) ->
                        item {
                            // Abschnitts-Header mit besserem Design
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = groupName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        items(categories) { category ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clickable {
                                        tempSelectedCategories = if (category in tempSelectedCategories) {
                                            tempSelectedCategories - category
                                        } else {
                                            tempSelectedCategories + category
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (category in tempSelectedCategories)
                                        MaterialTheme.colorScheme.secondaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category.displayName,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (category in tempSelectedCategories)
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                    Checkbox(
                                        checked = category in tempSelectedCategories,
                                        onCheckedChange = { checked ->
                                            tempSelectedCategories = if (checked) {
                                                tempSelectedCategories + category
                                            } else {
                                                tempSelectedCategories - category
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // Abstand zwischen Gruppen
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(tempSelectedCategories) }) {
                Text("Anwenden")
            }
        },
        dismissButton = {
            Row {
                if (tempSelectedCategories.isNotEmpty()) {
                    TextButton(onClick = { tempSelectedCategories = emptyList() }) {
                        Text("Alle löschen")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(Constants.UI.CANCEL_BUTTON)
                }
            }
        }
    )
}