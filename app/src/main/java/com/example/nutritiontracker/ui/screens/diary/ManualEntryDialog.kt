package com.example.nutritiontracker.ui.screens.diary

import MainViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.data.database.entities.Ingredient
import com.example.nutritiontracker.data.database.entities.IngredientUnit
import com.example.nutritiontracker.data.models.Category
import com.example.nutritiontracker.data.models.MealType
import com.example.nutritiontracker.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryDialog(
    viewModel: MainViewModel,
    mealType: MealType,
    date: Long,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var caloriesError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manueller Eintrag") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Fügen Sie einen manuellen Eintrag hinzu, z.B. für Restaurantbesuche",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Bezeichnung *") },
                    placeholder = { Text("z.B. Restaurant Pizza") },
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = calories,
                    onValueChange = {
                        calories = it.filter { char -> char.isDigit() }
                        caloriesError = false
                    },
                    label = { Text("Geschätzte Kalorien *") },
                    placeholder = { Text("z.B. 800") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = caloriesError,
                    modifier = Modifier.fillMaxWidth()
                )

                // Optionale Makronährstoffe
                Text(
                    text = "Optionale Makronährstoffe:",
                    style = MaterialTheme.typography.labelMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Protein (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Kohlenhydrate (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Fett (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "* Pflichtfelder",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    var hasError = false

                    if (name.isBlank()) {
                        nameError = true
                        hasError = true
                    }

                    if (calories.isBlank()) {
                        caloriesError = true
                        hasError = true
                    }

                    if (!hasError) {
                        // Erstelle eine temporäre Zutat für den manuellen Eintrag
                        val manualIngredient = Ingredient(
                            name = "${Constants.ManualEntry.PREFIX} $name",
                            calories = calories.toDouble(),
                            protein = protein.toDoubleOrNull() ?: 0.0,
                            carbs = carbs.toDoubleOrNull() ?: 0.0,
                            fat = fat.toDoubleOrNull() ?: 0.0,
                            fiber = 0.0,
                            sugar = 0.0,
                            salt = 0.0,
                            unit = IngredientUnit.GRAM,
                            categories = listOf(Category.QUICK), // Markiere als "Quick" für manuelle Einträge
                            description = "${Constants.ManualEntry.DESCRIPTION_TEMPLATE} ${SimpleDateFormat(Constants.DateTime.DATE_FORMAT_SHORT, Locale.GERMAN).format(Date(date))}"
                        )

                        // Füge die Zutat hinzu
                        viewModel.addIngredientAndCreateDiaryEntry(
                            ingredient = manualIngredient,
                            mealType = mealType,
                            date = date,
                            amount = Constants.ManualEntry.DEFAULT_AMOUNT // Standard 100g für manuelle Einträge
                        )

                        onConfirm()
                    }
                }
            ) {
                Text(Constants.UI.SAVE_BUTTON)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Constants.UI.CANCEL_BUTTON)
            }
        }
    )
}