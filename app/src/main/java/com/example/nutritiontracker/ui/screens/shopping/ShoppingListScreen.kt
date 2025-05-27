package com.example.nutritiontracker.ui.screens.shopping

import MainViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.data.database.entities.ShoppingListItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(viewModel: MainViewModel) {
    val shoppingListItems by viewModel.shoppingListItems.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val (checkedItems, uncheckedItems) = shoppingListItems.partition { it.isChecked }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einkaufsliste") },
                actions = {
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
            // Nicht erledigte Einträge
            if (uncheckedItems.isNotEmpty()) {
                items(uncheckedItems) { item ->
                    ShoppingListItemCard(
                        item = item,
                        onCheckedChange = { checked ->
                            viewModel.updateShoppingListItemChecked(item.id, checked)
                        },
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
                        onCheckedChange = { checked ->
                            viewModel.updateShoppingListItemChecked(item.id, checked)
                        },
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
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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