package com.example.nutritiontracker.ui.screens.overview


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.data.models.WeeklyStats
import com.example.nutritiontracker.ui.components.NutrientInfo

@Composable
fun WeeklySummaryCard(stats: WeeklyStats) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Wochendurchschnitt",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (stats.totalDays > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NutrientInfo("Kalorien", stats.avgCalories, "kcal")
                    NutrientInfo("Protein", stats.avgProtein, "g")
                    NutrientInfo("Kohlenhydrate", stats.avgCarbs, "g")
                    NutrientInfo("Fett", stats.avgFat, "g")
                }

                Text(
                    text = "Basierend auf ${stats.totalDays} Tagen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Text(
                    text = "Noch keine Daten für diese Woche vorhanden",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}