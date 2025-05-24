package com.example.nutritiontracker.ui.screens.overview


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.data.models.MealType
import com.example.nutritiontracker.utils.DateUtils
import com.example.nutritiontracker.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(viewModel: MainViewModel) {
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)

    val diaryEntries by viewModel.getDiaryEntriesForDate(selectedDate).collectAsState(initial = emptyList())
    val weeklyStats by viewModel.getWeeklyStats(selectedDate).collectAsState(initial = com.example.nutritiontracker.data.models.WeeklyStats())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Ernährungsübersicht",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Date selector
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = selectedDate
                    cal.add(Calendar.DAY_OF_MONTH, -1)
                    selectedDate = cal.timeInMillis
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Vorheriger Tag")
                }

                Text(
                    text = dateFormat.format(Date(selectedDate)),
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(
                    onClick = {
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = selectedDate
                        cal.add(Calendar.DAY_OF_MONTH, 1)
                        selectedDate = cal.timeInMillis
                    },
                    enabled = !DateUtils.isToday(selectedDate)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Nächster Tag")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Daily summary
            item {
                DailySummaryCard(diaryEntries, viewModel)
            }

            // Weekly summary
            item {
                WeeklySummaryCard(weeklyStats)
            }

            // Meal entries
            item {
                Text(
                    text = "Heutige Mahlzeiten",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            MealType.values().forEach { mealType ->
                val mealEntries = diaryEntries.filter { it.mealType == mealType }
                if (mealEntries.isNotEmpty()) {
                    item {
                        MealSection(mealType, mealEntries, viewModel)
                    }
                }
            }

            if (diaryEntries.isEmpty()) {
                item {
                    Text(
                        text = "Noch keine Einträge für heute.\nGehen Sie zum Tagebuch, um Mahlzeiten hinzuzufügen.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                }
            }
        }
    }
}