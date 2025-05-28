package com.example.nutritiontracker.ui.screens.shopping

import MainViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.data.database.entities.ShoppingListItem
import com.example.nutritiontracker.data.database.entities.IngredientUnit
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShoppingListItemDialog(
    item: ShoppingListItem,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var amount by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var ingredientUnit by remember { mutableStateOf<IngredientUnit?>(null) }

    // Parse die aktuelle Menge
    LaunchedEffect(item) {
        if (item.ingredientId != null) {
            scope.launch {
                val ingredient = viewModel.getIngredientById(item.ingredientId)
                ingredientUnit = ingredient?.unit
            }
        }

        // Parse die vorhandene Menge
        val amountPattern = Regex("(\\d+(?:\\.\\d+)?)(\\s*)(g|ml|Stück)?")
        val match = amountPattern.find(item.amount)

        if (match != null) {
            amount = match.groupValues[1]
            unit = match.groupValues[3]
        } else {
            amount = item.amount
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Menge bearbeiten") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium
                )

                // Menge eingeben
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it.filter { char -> char.isDigit() || char == '.' }
                    },
                    label = {
                        Text(
                            when (ingredientUnit) {
                                IngredientUnit.GRAM -> "Menge (g)"
                                IngredientUnit.MILLILITER -> "Menge (ml)"
                                IngredientUnit.PIECE -> "Anzahl"
                                null -> "Menge"
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (ingredientUnit == IngredientUnit.PIECE)
                            KeyboardType.Number
                        else
                            KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Einheit auswählen (nur für manuelle Einträge)
                if (item.isManualEntry || ingredientUnit == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(
                            selected = unit == "g",
                            onClick = { unit = "g" },
                            label = { Text("Gramm") }
                        )
                        FilterChip(
                            selected = unit == "ml",
                            onClick = { unit = "ml" },
                            label = { Text("Milliliter") }
                        )
                        FilterChip(
                            selected = unit == "Stück" || unit.isEmpty(),
                            onClick = { unit = "Stück" },
                            label = { Text("Stück") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalAmount = when {
                        ingredientUnit != null -> {
                            // Für Zutaten mit definierter Einheit
                            when (ingredientUnit) {
                                IngredientUnit.GRAM -> "${amount}g"
                                IngredientUnit.MILLILITER -> "${amount}ml"
                                IngredientUnit.PIECE -> "${amount} Stück"
                                else -> amount
                            }
                        }
                        unit.isNotEmpty() -> {
                            // Für manuelle Einträge mit gewählter Einheit
                            when (unit) {
                                "g" -> "${amount}g"
                                "ml" -> "${amount}ml"
                                "Stück" -> "${amount} Stück"
                                else -> amount
                            }
                        }
                        else -> amount
                    }

                    viewModel.updateShoppingListItem(
                        item.copy(amount = finalAmount)
                    )
                    onDismiss()
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