package com.example.nutritiontracker.ui.screens.shopping

import MainViewModel
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nutritiontracker.data.database.entities.ShoppingListItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ShoppingListScreen(
    viewModel: MainViewModel
) {
    val shoppingItems by viewModel.shoppingListItems.collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ShoppingListItem?>(null) }
    var expandedCategories by remember { mutableStateOf(setOf<String>()) }

    // Gruppiere Items nach Kategorie
    val groupedItems = remember(shoppingItems) {
        shoppingItems.groupBy { item ->
            when {
                item.ingredientId != null -> "Zutaten"
                item.name.contains("Rezept:", ignoreCase = true) -> "Rezepte"
                else -> "Sonstiges"
            }
        }
    }

    // Zähle erledigte Items
    val checkedCount = shoppingItems.count { it.isChecked }
    val totalCount = shoppingItems.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einkaufsliste") },
                actions = {
                    if (checkedCount > 0) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    viewModel.deleteCheckedShoppingListItems()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "Erledigte löschen"
                            )
                        }
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Hinzufügen")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (shoppingItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Einkaufsliste ist leer",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = { showAddDialog = true }) {
                        Text("Ersten Artikel hinzufügen")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fortschrittsanzeige
                if (totalCount > 0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
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
                                        text = "Fortschritt",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "$checkedCount / $totalCount erledigt",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { checkedCount.toFloat() / totalCount },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }

                // Gruppierte Items
                groupedItems.forEach { (category, items) ->
                    val isExpanded = expandedCategories.contains(category)
                    val categoryCheckedCount = items.count { it.isChecked }
                    val categoryTotalCount = items.size

                    stickyHeader {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedCategories = if (isExpanded) {
                                        expandedCategories - category
                                    } else {
                                        expandedCategories + category
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (isExpanded) "Einklappen" else "Ausklappen"
                                    )
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Text(
                                    text = "$categoryCheckedCount/$categoryTotalCount",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    if (isExpanded) {
                        items(
                            items = items,
                            key = { it.id }
                        ) { item ->
                            ShoppingListItemCard(
                                item = item,
                                onCheckedChange = { checked ->
                                    scope.launch {
                                        viewModel.updateShoppingListItemChecked(item.id, checked)
                                    }
                                },
                                onEdit = { editingItem = item },
                                onDelete = {
                                    scope.launch {
                                        viewModel.deleteShoppingListItem(item)
                                    }
                                },
                                modifier = Modifier.animateContentSize()
                            )
                        }
                    }
                }
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

    editingItem?.let { item ->
        EditShoppingListItemDialog(
            item = item,
            viewModel = viewModel,
            onDismiss = { editingItem = null }
        )
    }
}

@Composable
fun ShoppingListItemCard(
    item: ShoppingListItem,
    onCheckedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = onCheckedChange
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.amount.isNotEmpty()) {
                    Text(
                        text = item.amount,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

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