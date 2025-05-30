package com.example.nutritiontracker.ui.screens.overview

import MainViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.data.database.entities.DiaryEntry
import com.example.nutritiontracker.data.database.entities.IngredientUnit
import com.example.nutritiontracker.data.models.EntryType
import com.example.nutritiontracker.data.models.MealType
import kotlinx.coroutines.launch

@Composable
fun MealSection(
    mealType: MealType,
    entries: List<DiaryEntry>,
    viewModel: MainViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = when(mealType) {
                    MealType.BREAKFAST -> "Frühstück"
                    MealType.LUNCH -> "Mittagessen"
                    MealType.DINNER -> "Abendessen"
                    MealType.SNACK -> "Snack"
                },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            entries.forEach { entry ->
                DiaryEntryRow(entry, viewModel)
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Meal total
            MealTotalRow(entries, viewModel)
        }
    }
}

@Composable
private fun DiaryEntryRow(
    entry: DiaryEntry,
    viewModel: MainViewModel
) {
    val scope = rememberCoroutineScope()
    var entryName by remember { mutableStateOf("") }
    var displayAmount by remember { mutableStateOf("") }

    LaunchedEffect(entry) {
        scope.launch {
            entryName = viewModel.getEntryDisplayName(entry)

            // Hole die korrekte Einheit für die Anzeige
            when (entry.entryType) {
                EntryType.INGREDIENT -> {
                    entry.ingredientId?.let { id ->
                        val ingredient = viewModel.getIngredientById(id)
                        displayAmount = ingredient?.let {
                            when (it.unit) {
                                IngredientUnit.GRAM -> "${entry.amount.toInt()}g"
                                IngredientUnit.MILLILITER -> "${entry.amount.toInt()}ml"
                                IngredientUnit.PIECE -> "${entry.amount.toInt()} ${if (entry.amount == 1.0) "Stück" else "Stück"}"
                            }
                        } ?: "${entry.amount.toInt()}g"
                    }
                }
                EntryType.RECIPE -> {
                    displayAmount = "${entry.amount.toInt()} ${if (entry.amount == 1.0) "Port." else "Port."}"
                }
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = entryName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = displayAmount,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun MealTotalRow(
    entries: List<DiaryEntry>,
    viewModel: MainViewModel
) {
    val scope = rememberCoroutineScope()
    var totalCalories by remember { mutableStateOf(0.0) }

    LaunchedEffect(entries) {
        scope.launch {
            totalCalories = viewModel.calculateMealCalories(entries)
        }
    }

    if (entries.isNotEmpty()) {
        Divider(modifier = Modifier.padding(vertical = 4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Gesamt",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${totalCalories.toInt()} kcal",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}