package com.example.nutritiontracker.ui.screens.diary


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.nutritiontracker.data.database.entities.DiaryEntry
import com.example.nutritiontracker.data.models.EntryType
import com.example.nutritiontracker.data.models.MealType
import com.example.nutritiontracker.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDiaryEntryDialog(
    viewModel: MainViewModel,
    entry: DiaryEntry,
    onDismiss: () -> Unit
) {
    var selectedMealType by remember { mutableStateOf(entry.mealType) }
    var amount by remember { mutableStateOf(entry.amount.toString()) }
    var amountError by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var entryName by remember { mutableStateOf("Lade...") }

    val scope = rememberCoroutineScope()

    // Lade den Namen des Eintrags
    LaunchedEffect(entry) {
        scope.launch {
            entryName = viewModel.getEntryDisplayName(entry)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.9f),
        title = { Text("Eintrag bearbeiten") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Anzeige des Eintragsnamens
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = entryName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (entry.entryType == EntryType.INGREDIENT) "Zutat" else "Rezept",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Meal type selection
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = when(selectedMealType) {
                            MealType.BREAKFAST -> "Frühstück"
                            MealType.LUNCH -> "Mittagessen"
                            MealType.DINNER -> "Abendessen"
                            MealType.SNACK -> "Snack"
                        },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Mahlzeit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        MealType.values().forEach { mealType ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when(mealType) {
                                            MealType.BREAKFAST -> "Frühstück"
                                            MealType.LUNCH -> "Mittagessen"
                                            MealType.DINNER -> "Abendessen"
                                            MealType.SNACK -> "Snack"
                                        }
                                    )
                                },
                                onClick = {
                                    selectedMealType = mealType
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Amount input
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it.filter { char -> char.isDigit() || char == '.' }
                        amountError = false
                    },
                    label = {
                        Text(if (entry.entryType == EntryType.INGREDIENT) "Menge (g)" else "Portionen")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = amountError,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (amount.isEmpty()) {
                        amountError = true
                        return@TextButton
                    }

                    amount.toDoubleOrNull()?.let { amt ->
                        val updatedEntry = entry.copy(
                            mealType = selectedMealType,
                            amount = amt
                        )
                        viewModel.updateDiaryEntry(updatedEntry)
                        onDismiss()
                    }
                }
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}