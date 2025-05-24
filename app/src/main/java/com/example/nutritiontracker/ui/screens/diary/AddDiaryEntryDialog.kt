package com.example.nutritiontracker.ui.screens.diary


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.nutritiontracker.data.database.entities.DiaryEntry
import com.example.nutritiontracker.data.database.entities.Ingredient
import com.example.nutritiontracker.data.database.entities.Recipe
import com.example.nutritiontracker.data.models.EntryType
import com.example.nutritiontracker.data.models.MealType
import com.example.nutritiontracker.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDiaryEntryDialog(
    viewModel: MainViewModel,
    initialMealType: MealType,
    date: Long,
    onDismiss: () -> Unit
) {
    var selectedMealType by remember { mutableStateOf(initialMealType) }
    var entryType by remember { mutableStateOf(EntryType.INGREDIENT) }
    var selectedItem by remember { mutableStateOf<Any?>(null) }
    var amount by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val ingredients by viewModel.ingredients.collectAsState(initial = emptyList())
    val recipes by viewModel.recipes.collectAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .fillMaxHeight(0.8f),
        title = { Text("Eintrag hinzufügen") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Entry type selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = entryType == EntryType.INGREDIENT,
                        onClick = {
                            entryType = EntryType.INGREDIENT
                            selectedItem = null
                        },
                        label = { Text("Zutat") }
                    )
                    FilterChip(
                        selected = entryType == EntryType.RECIPE,
                        onClick = {
                            entryType = EntryType.RECIPE
                            selectedItem = null
                        },
                        label = { Text("Rezept") }
                    )
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

                // Item selection
                Text(
                    text = if (entryType == EntryType.INGREDIENT) "Zutat auswählen" else "Rezept auswählen",
                    style = MaterialTheme.typography.titleSmall
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val items = if (entryType == EntryType.INGREDIENT) ingredients else recipes

                    if (items.isEmpty()) {
                        item {
                            Text(
                                text = if (entryType == EntryType.INGREDIENT)
                                    "Keine Zutaten vorhanden"
                                else
                                    "Keine Rezepte vorhanden",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(items) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedItem = item }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = when(item) {
                                            is Ingredient -> item.name
                                            is Recipe -> item.name
                                            else -> ""
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = when(item) {
                                            is Ingredient -> "${item.calories.toInt()} kcal/100g"
                                            is Recipe -> "${item.servings} ${if (item.servings == 1) "Portion" else "Portionen"}"
                                            else -> ""
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                RadioButton(
                                    selected = selectedItem == item,
                                    onClick = { selectedItem = item }
                                )
                            }
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
                        Text(if (entryType == EntryType.INGREDIENT) "Menge (g)" else "Portionen")
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

                    selectedItem?.let { item ->
                        amount.toDoubleOrNull()?.let { amt ->
                            val entry = DiaryEntry(
                                date = date,
                                mealType = selectedMealType,
                                entryType = entryType,
                                ingredientId = if (item is Ingredient) item.id else null,
                                recipeId = if (item is Recipe) item.id else null,
                                amount = amt
                            )
                            viewModel.addDiaryEntry(entry)
                            onDismiss()
                        }
                    }
                },
                enabled = selectedItem != null && amount.isNotEmpty()
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