package com.example.nutritiontracker.ui.screens.shopping

import MainViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.data.database.entities.ShoppingListItem
import com.example.nutritiontracker.data.database.entities.IngredientUnit
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(viewModel: MainViewModel) {
    val shoppingListItems by viewModel.shoppingListItems.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ShoppingListItem?>(null) }
    var showSummary by remember { mutableStateOf(true) }

    val (checkedItems, uncheckedItems) = shoppingListItems.partition { it.isChecked }

    // Berechne die Zusammenfassung
    val ingredientSummary = remember(uncheckedItems) {
        calculateIngredientSummary(uncheckedItems)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einkaufsliste") },
                actions = {
                    IconButton(onClick = { showSummary = !showSummary }) {
                        Icon(
                            if (showSummary) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (showSummary) "Zusammenfassung ausblenden" else "Zusammenfassung anzeigen"
                        )
                    }
                    if (checkedItems.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Erledigte löschen")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Hinzufügen")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Zusammenfassung (wenn aktiviert und Einträge vorhanden)
            if (showSummary && ingredientSummary.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Zusammenfassung",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${ingredientSummary.size} Zutaten",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            ingredientSummary.forEach { (name, amount) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = amount,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }

            // Nicht erledigte Einträge
            if (uncheckedItems.isNotEmpty()) {
                item {
                    Text(
                        text = "Einzukaufen",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(uncheckedItems) { item ->
                    ShoppingListItemCard(
                        item = item,
                        viewModel = viewModel,
                        onCheckedChange = { checked ->
                            viewModel.updateShoppingListItemChecked(item.id, checked)
                        },
                        onEdit = { editingItem = item },
                        onDelete = {
                            viewModel.deleteShoppingListItem(item)
                        }
                    )
                }
            }

            // Trennlinie
            if (uncheckedItems.isNotEmpty() && checkedItems.isNotEmpty()) {
                item {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }

            // Erledigte Einträge
            if (checkedItems.isNotEmpty()) {
                item {
                    Text(
                        text = "Erledigt",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(checkedItems) { item ->
                    ShoppingListItemCard(
                        item = item,
                        viewModel = viewModel,
                        onCheckedChange = { checked ->
                            viewModel.updateShoppingListItemChecked(item.id, checked)
                        },
                        onEdit = { editingItem = item },
                        onDelete = {
                            viewModel.deleteShoppingListItem(item)
                        }
                    )
                }
            }

            // Leere Liste
            if (shoppingListItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ihre Einkaufsliste ist leer.\nTippen Sie auf + um Einträge hinzuzufügen.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

    if (showAddDialog) {
        AddShoppingListItemDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }

    if (editingItem != null) {
        EditShoppingListItemDialog(
            item = editingItem!!,
            viewModel = viewModel,
            onDismiss = { editingItem = null }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Erledigte Einträge löschen?") },
            text = { Text("Alle als erledigt markierten Einträge werden gelöscht.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCheckedShoppingListItems()
                        showClearDialog = false
                    }
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
fun ShoppingListItemCard(
    item: ShoppingListItem,
    viewModel: MainViewModel,
    onCheckedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var recipeName by remember { mutableStateOf<String?>(null) }

    // Lade Rezeptnamen wenn vorhanden
    LaunchedEffect(item.recipeId) {
        if (item.recipeId != null) {
            scope.launch {
                val recipe = viewModel.getRecipeById(item.recipeId)
                recipeName = recipe?.name
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (item.isChecked) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = onCheckedChange
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                        color = if (item.isChecked) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (item.amount.isNotEmpty()) {
                            Text(
                                text = item.amount,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Zeige Herkunft
                        when {
                            item.isManualEntry -> {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text("Manuell", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            recipeName != null -> {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text("Rezept: $recipeName", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            item.ingredientId != null -> {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text("Zutat", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Bearbeiten",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Löschen",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// Hilfsfunktion für die Zusammenfassung
private fun calculateIngredientSummary(items: List<ShoppingListItem>): List<Pair<String, String>> {
    val summary = mutableMapOf<String, MutableMap<String, Double>>()

    items.forEach { item ->
        if (!item.isManualEntry && item.ingredientId != null) {
            val amounts = summary.getOrPut(item.name) { mutableMapOf() }

            // Parse die Menge
            val amountPattern = Regex("(\\d+(?:\\.\\d+)?)(\\s*)(g|ml|Stück)?")
            val match = amountPattern.find(item.amount)

            if (match != null) {
                val value = match.groupValues[1].toDoubleOrNull() ?: 0.0
                val unit = match.groupValues[3].ifEmpty { "Stück" }

                amounts[unit] = amounts.getOrDefault(unit, 0.0) + value
            }
        }
    }

    return summary.map { (name, amounts) ->
        val amountString = amounts.entries.joinToString(", ") { (unit, value) ->
            when (unit) {
                "g" -> "${value.toInt()}g"
                "ml" -> "${value.toInt()}ml"
                "Stück" -> "${value.toInt()} Stück"
                else -> "${value.toInt()} $unit"
            }
        }
        name to amountString
    }.sortedBy { it.first }
}