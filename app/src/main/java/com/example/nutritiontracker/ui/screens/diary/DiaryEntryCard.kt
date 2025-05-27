package com.example.nutritiontracker.ui.screens.diary

import MainViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.data.database.entities.DiaryEntry
import com.example.nutritiontracker.utils.DiaryEntryUtils

import kotlinx.coroutines.launch

@Composable
fun DiaryEntryCard(
    entry: DiaryEntry,
    viewModel: MainViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var entryName by remember { mutableStateOf("Lade...") }
    var nutritionInfo by remember { mutableStateOf("") }
    var displayAmount by remember { mutableStateOf("") }
    var isManualEntry by remember { mutableStateOf(false) }

    LaunchedEffect(entry) {
        scope.launch {
            val name = viewModel.getEntryDisplayName(entry)
            entryName = name
            isManualEntry = DiaryEntryUtils.isManualEntry(name)

            // Hole die Einheit für die Anzeige
            val ingredient = entry.ingredientId?.let { viewModel.getIngredientById(it) }
            displayAmount = DiaryEntryUtils.getDisplayAmount(entry, ingredient)

            // Berechne Nährwerte
            val nutrition = viewModel.calculateDailyNutrition(listOf(entry))
            nutritionInfo = DiaryEntryUtils.getNutritionInfo(nutrition)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (isManualEntry) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
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
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = DiaryEntryUtils.formatManualEntryName(entryName),
                    style = MaterialTheme.typography.titleMedium
                )
                if (isManualEntry) {
                    Text(
                        text = "Manueller Eintrag",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Text(
                    text = displayAmount,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = nutritionInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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