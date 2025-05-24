package com.example.nutritiontracker.ui.screens.overview


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.data.database.entities.DiaryEntry
import com.example.nutritiontracker.ui.components.NutrientInfo
import com.example.nutritiontracker.utils.NutritionCalculator
import com.example.nutritiontracker.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun DailySummaryCard(
    entries: List<DiaryEntry>,
    viewModel: MainViewModel
) {
    val scope = rememberCoroutineScope()
    var nutritionValues by remember { mutableStateOf(NutritionCalculator.NutritionValues(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)) }

    LaunchedEffect(entries) {
        scope.launch {
            nutritionValues = viewModel.calculateDailyNutrition(entries)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tagesübersicht",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutrientInfo("Kalorien", nutritionValues.calories, "kcal")
                NutrientInfo("Protein", nutritionValues.protein, "g")
                NutrientInfo("Kohlenhydrate", nutritionValues.carbs, "g")
                NutrientInfo("Fett", nutritionValues.fat, "g")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutrientInfo("Ballaststoffe", nutritionValues.fiber, "g")
                NutrientInfo("Zucker", nutritionValues.sugar, "g")
                NutrientInfo("Salz", nutritionValues.salt, "g")
            }
        }
    }
}