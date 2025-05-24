package com.example.nutritiontracker.ui.screens.recipes


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.data.database.entities.Ingredient
import com.example.nutritiontracker.data.database.entities.IngredientUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientPickerDialog(
    ingredients: List<Ingredient>,
    onSelect: (Ingredient, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedIngredient by remember { mutableStateOf<Ingredient?>(null) }
    var amount by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Gefilterte Zutaten basierend auf Suche
    val filteredIngredients = remember(ingredients, searchQuery) {
        if (searchQuery.isEmpty()) {
            ingredients
        } else {
            ingredients.filter { ingredient ->
                ingredient.name.contains(searchQuery, ignoreCase = true) ||
                        ingredient.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zutat auswählen") },
        text = {
            Column {
                // Suchfeld
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Zutat suchen...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Suchen") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Löschen")
                            }
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredIngredients.isEmpty()) {
                    Text(
                        text = if (searchQuery.isEmpty()) {
                            "Keine Zutaten verfügbar.\nBitte erst Zutaten anlegen."
                        } else {
                            "Keine Zutaten gefunden für:\n\"$searchQuery\""
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(filteredIngredients) { ingredient ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedIngredient = ingredient }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ingredient.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = when (ingredient.unit) {
                                            IngredientUnit.GRAM -> "${ingredient.calories.toInt()} kcal/100g"
                                            IngredientUnit.PIECE -> "${ingredient.calories.toInt()} kcal/Stück"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                RadioButton(
                                    selected = selectedIngredient == ingredient,
                                    onClick = { selectedIngredient = ingredient }
                                )
                            }
                        }
                    }

                    selectedIngredient?.let { ingredient ->
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = amount,
                            onValueChange = {
                                amount = it.filter { char -> char.isDigit() || (char == '.' && ingredient.unit == IngredientUnit.GRAM) }
                                amountError = false
                            },
                            label = {
                                Text(
                                    when (ingredient.unit) {
                                        IngredientUnit.GRAM -> "Menge (g)"
                                        IngredientUnit.PIECE -> "Anzahl (Stück)"
                                    }
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (ingredient.unit == IngredientUnit.GRAM)
                                    KeyboardType.Decimal
                                else
                                    KeyboardType.Number
                            ),
                            isError = amountError,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Platzhalter mit Standardmenge
                        if (amount.isEmpty()) {
                            Text(
                                text = when (ingredient.unit) {
                                    IngredientUnit.GRAM -> "Standard: 100g"
                                    IngredientUnit.PIECE -> "Standard: 1 Stück"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedIngredient == null) return@TextButton

                    val ingredient = selectedIngredient!!

                    // Validierung basierend auf Einheit
                    val amountValue = when {
                        amount.isEmpty() -> {
                            // Standardwerte
                            when (ingredient.unit) {
                                IngredientUnit.GRAM -> 100.0
                                IngredientUnit.PIECE -> 1.0
                            }
                        }
                        ingredient.unit == IngredientUnit.PIECE -> {
                            // Für Stückzahl nur ganze Zahlen erlauben
                            amount.toIntOrNull()?.toDouble()
                        }
                        else -> {
                            // Für Gramm auch Dezimalzahlen
                            amount.toDoubleOrNull()
                        }
                    }

                    if (amountValue == null || amountValue <= 0) {
                        amountError = true
                        return@TextButton
                    }

                    onSelect(ingredient, amountValue)
                },
                enabled = selectedIngredient != null
            ) {
                Text("Hinzufügen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}