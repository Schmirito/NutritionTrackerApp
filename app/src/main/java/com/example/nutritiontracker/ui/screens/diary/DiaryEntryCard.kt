package com.example.nutritiontracker.ui.screens.diary


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
import com.example.nutritiontracker.data.models.EntryType
import com.example.nutritiontracker.viewmodel.MainViewModel
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

    LaunchedEffect(entry) {
        scope.launch {
            entryName = viewModel.getEntryDisplayName(entry)
            // Calculate nutrition info
            val nutrition = viewModel.calculateDailyNutrition(listOf(entry))
            nutritionInfo = "${nutrition.calories.toInt()} kcal | " +
                    "${nutrition.protein.toInt()}g P | " +
                    "${nutrition.carbs.toInt()}g K | " +
                    "${nutrition.fat.toInt()}g F"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth()
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
                    text = entryName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${entry.amount.toInt()}${if (entry.entryType == EntryType.INGREDIENT) "g" else " ${if (entry.amount == 1.0) "Portion" else "Portionen"}"}",
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