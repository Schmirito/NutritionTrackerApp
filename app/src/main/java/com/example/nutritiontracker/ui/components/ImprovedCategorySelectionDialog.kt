package com.example.nutritiontracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.data.models.Category
import com.example.nutritiontracker.utils.CategoryUtils

@Composable
fun ImprovedCategorySelectionDialog(
    selectedCategories: List<Category>,
    automaticCategories: List<Category> = emptyList(),
    isForIngredients: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (List<Category>) -> Unit
) {
    var tempSelectedCategories by remember { mutableStateOf(selectedCategories) }
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kategorien auswählen") },
        text = {
            Column {
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

                Spacer(modifier = Modifier.height(8.dp))

                // Info über automatische Kategorien (nur für Rezepte)
                if (automaticCategories.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Text(
                            text = "Automatisch: ${automaticCategories.joinToString(", ") { it.displayName }}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val groupedCategories = if (isForIngredients) {
                    CategoryUtils.getGroupedCategoriesForIngredients()
                } else {
                    CategoryUtils.getGroupedCategories()
                }

                LazyColumn(
                    modifier = Modifier.height(400.dp)
                ) {
                    groupedCategories.forEach { (groupName, categories) ->
                        val filteredCategories = categories.filter {
                            searchQuery.isEmpty() || it.displayName.contains(searchQuery, ignoreCase = true)
                        }

                        if (filteredCategories.isNotEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Text(
                                        text = groupName,
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }

                            items(filteredCategories) { category ->
                                val isAutomatic = category in automaticCategories
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clickable(enabled = !isAutomatic) {
                                            if (!isAutomatic) {
                                                tempSelectedCategories = if (category in tempSelectedCategories) {
                                                    tempSelectedCategories - category
                                                } else {
                                                    tempSelectedCategories + category
                                                }
                                            }
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            isAutomatic -> MaterialTheme.colorScheme.tertiaryContainer
                                            category in tempSelectedCategories -> MaterialTheme.colorScheme.secondaryContainer
                                            else -> MaterialTheme.colorScheme.surface
                                        }
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
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (isAutomatic) {
                                            Text(
                                                text = "Auto",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        } else {
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
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(tempSelectedCategories) }) {
                Text("OK")
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
                    Text("Abbrechen")
                }
            }
        }
    )
}