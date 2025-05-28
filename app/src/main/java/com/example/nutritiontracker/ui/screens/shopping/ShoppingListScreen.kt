package com.example.nutritiontracker.ui.screens.shopping

import MainViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.data.database.entities.ShoppingListItem
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(viewModel: MainViewModel) {
    val shoppingListItems by viewModel.shoppingListItems.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ShoppingListItem?>(null) }
    var showSummary by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val allRecipes by viewModel.recipes.collectAsState(initial = emptyList())

    val (checkedItems, uncheckedItems) = shoppingListItems.partition { it.isChecked }

    // Gruppiere Einträge nach Quelle
    val groupedItems = remember(uncheckedItems) {
        GroupedShoppingItems(
            manualItems = uncheckedItems.filter { it.isManualEntry },
            ingredientItems = uncheckedItems.filter { !it.isManualEntry && it.recipeId == null },
            recipeGroups = uncheckedItems
                .filter { it.recipeId != null }
                .groupBy { it.recipeId!! }
        )
    }

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
            // Zusammenfassung
            if (showSummary && ingredientSummary.isNotEmpty()) {
                item {
                    SummaryCard(ingredientSummary)
                }

                item {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }

            // Manuelle Einträge
            if (groupedItems.manualItems.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Manuell hinzugefügt",
                        icon = Icons.Default.Edit,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                }

                items(groupedItems.manualItems, key = { it.id }) { item ->
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

            // Einzelne Zutaten
            if (groupedItems.ingredientItems.isNotEmpty()) {
                if (groupedItems.manualItems.isNotEmpty()) {
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                item {
                    SectionHeader(
                        title = "Aus Zutaten",
                        icon = Icons.Default.Kitchen,
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                }

                items(groupedItems.ingredientItems, key = { it.id }) { item ->
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

            // Rezept-Gruppen
            groupedItems.recipeGroups.forEach { (recipeId, items) ->
                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Lade Rezeptnamen
                val recipeName = remember(recipeId) {
                    var name by mutableStateOf("Rezept lädt...")
                    scope.launch {
                        val recipe = allRecipes.find { it.id == recipeId }
                        name = recipe?.name ?: "Unbekanntes Rezept"
                    }
                    name
                }

                item {
                    SectionHeader(
                        title = "Aus Rezept: $recipeName",
                        icon = Icons.Default.Restaurant,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        subtitle = "${items.size} Zutaten"
                    )
                }

                items(items, key = { it.id }) { item ->
                    ShoppingListItemCard(
                        item = item,
                        viewModel = viewModel,
                        onCheckedChange = { checked ->
                            viewModel.updateShoppingListItemChecked(item.id, checked)
                        },
                        onEdit = { editingItem = item },
                        onDelete = {
                            viewModel.deleteShoppingListItem(item)
                        },
                        showRecipeBadge = false // Badge nicht nötig, da schon im Header
                    )
                }
            }

            // Trennlinie vor erledigten Einträgen
            if (uncheckedItems.isNotEmpty() && checkedItems.isNotEmpty()) {
                item {
                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }

            // Erledigte Einträge
            if (checkedItems.isNotEmpty()) {
                item {
                    Text(
                        text = "Erledigt (${checkedItems.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(checkedItems, key = { it.id }) { item ->
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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

    // Dialoge
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
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    subtitle: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(summary: List<Triple<String, String, Boolean>>) {
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
                    text = "${summary.size} verschiedene Artikel",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            summary.forEach { (name, amount, isManual) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (isManual) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.height(16.dp)
                            ) {
                                Text("M", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
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

@Composable
fun ShoppingListItemCard(
    item: ShoppingListItem,
    viewModel: MainViewModel,
    onCheckedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    showRecipeBadge: Boolean = true
) {
    val scope = rememberCoroutineScope()
    var recipeName by remember { mutableStateOf<String?>(null) }

    // Lade Rezeptnamen wenn vorhanden und Badge angezeigt werden soll
    LaunchedEffect(item.recipeId) {
        if (item.recipeId != null && showRecipeBadge) {
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

                    if (item.amount.isNotEmpty()) {
                        Text(
                            text = item.amount,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Zeige Badge nur wenn aktiviert und nicht in Rezept-Sektion
                    if (showRecipeBadge && recipeName != null) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text("Rezept: $recipeName", style = MaterialTheme.typography.labelSmall)
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

// Datenklasse für gruppierte Items
private data class GroupedShoppingItems(
    val manualItems: List<ShoppingListItem>,
    val ingredientItems: List<ShoppingListItem>,
    val recipeGroups: Map<Long, List<ShoppingListItem>>
)

// Hilfsfunktion für die Zusammenfassung
private fun calculateIngredientSummary(items: List<ShoppingListItem>): List<Triple<String, String, Boolean>> {
    val summary = mutableMapOf<String, MutableMap<String, Double>>()
    val isManualEntry = mutableMapOf<String, Boolean>()

    items.forEach { item ->
        val amounts = summary.getOrPut(item.name) { mutableMapOf() }
        isManualEntry[item.name] = item.isManualEntry

        // Parse die Menge
        val amountPattern = Regex("(\\d+(?:\\.\\d+)?)(\\s*)(g|ml|Stück)?")
        val match = amountPattern.find(item.amount)

        if (match != null) {
            val value = match.groupValues[1].toDoubleOrNull() ?: 0.0
            val unit = match.groupValues[3].ifEmpty { "Stück" }

            amounts[unit] = amounts.getOrDefault(unit, 0.0) + value
        } else if (item.amount.isNotEmpty()) {
            // Für Einträge ohne erkennbare Einheit
            amounts[""] = amounts.getOrDefault("", 0.0) + 1
        }
    }

    return summary.map { (name, amounts) ->
        val amountString = if (amounts.containsKey("") && amounts.size == 1) {
            // Für Einträge ohne Einheit
            "${amounts[""]?.toInt() ?: 1}x"
        } else {
            amounts.entries
                .filter { it.key.isNotEmpty() }
                .joinToString(", ") { (unit, value) ->
                    when (unit) {
                        "g" -> "${value.toInt()}g"
                        "ml" -> "${value.toInt()}ml"
                        "Stück" -> "${value.toInt()} Stück"
                        else -> "${value.toInt()} $unit"
                    }
                }
        }
        Triple(name, amountString, isManualEntry[name] ?: false)
    }.sortedBy { it.first }
}