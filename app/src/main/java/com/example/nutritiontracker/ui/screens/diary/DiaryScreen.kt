package com.example.nutritiontracker.ui.screens.diary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.data.database.entities.DiaryEntry
import com.example.nutritiontracker.data.models.MealType
import com.example.nutritiontracker.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(viewModel: MainViewModel) {
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    val diaryEntries by viewModel.getDiaryEntriesForDate(selectedDate).collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<DiaryEntry?>(null) }
    var selectedMealType by remember { mutableStateOf(MealType.BREAKFAST) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ernährungstagebuch") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Eintrag hinzufügen")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Date selector
            DateSelector(
                selectedDate = selectedDate,
                onDateChange = { selectedDate = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Meal type tabs
            ScrollableTabRow(
                selectedTabIndex = MealType.values().indexOf(selectedMealType)
            ) {
                MealType.values().forEach { mealType ->
                    Tab(
                        selected = selectedMealType == mealType,
                        onClick = { selectedMealType = mealType },
                        text = {
                            Text(
                                when(mealType) {
                                    MealType.BREAKFAST -> "Frühstück"
                                    MealType.LUNCH -> "Mittagessen"
                                    MealType.DINNER -> "Abendessen"
                                    MealType.SNACK -> "Snack"
                                }
                            )
                        }
                    )
                }
            }

            // Entries for selected meal type
            val mealEntries = diaryEntries.filter { it.mealType == selectedMealType }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(mealEntries) { entry ->
                    DiaryEntryCard(
                        entry = entry,
                        viewModel = viewModel,
                        onEdit = {
                            editingEntry = entry
                        },
                        onDelete = { viewModel.deleteDiaryEntry(entry) }
                    )
                }

                if (mealEntries.isEmpty()) {
                    item {
                        Text(
                            text = "Keine Einträge für diese Mahlzeit",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 32.dp)
                        )
                    }
                }

                // Spacer für FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddDiaryEntryDialog(
            viewModel = viewModel,
            initialMealType = selectedMealType,
            date = selectedDate,
            onDismiss = { showAddDialog = false }
        )
    }

    if (editingEntry != null) {
        EditDiaryEntryDialog(
            viewModel = viewModel,
            entry = editingEntry!!,
            onDismiss = { editingEntry = null }
        )
    }
}