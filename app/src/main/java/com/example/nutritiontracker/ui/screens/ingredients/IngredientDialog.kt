package com.example.nutritiontracker.ui.screens.ingredients

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.example.nutritiontracker.data.database.entities.Ingredient
import com.example.nutritiontracker.data.database.entities.IngredientUnit
import com.example.nutritiontracker.data.models.Category
import com.example.nutritiontracker.ui.components.ImprovedCategorySelectionDialog
import com.example.nutritiontracker.utils.ImageUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDialog(
    ingredient: Ingredient?,
    onDismiss: () -> Unit,
    onSave: (Ingredient) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(ingredient?.name ?: "") }
    var description by remember { mutableStateOf(ingredient?.description ?: "") }
    var calories by remember { mutableStateOf(ingredient?.calories?.toString() ?: "") }
    var protein by remember { mutableStateOf(ingredient?.protein?.takeIf { it > 0 }?.toString() ?: "") }
    var carbs by remember { mutableStateOf(ingredient?.carbs?.takeIf { it > 0 }?.toString() ?: "") }
    var fat by remember { mutableStateOf(ingredient?.fat?.takeIf { it > 0 }?.toString() ?: "") }
    var fiber by remember { mutableStateOf(ingredient?.fiber?.takeIf { it > 0 }?.toString() ?: "") }
    var sugar by remember { mutableStateOf(ingredient?.sugar?.takeIf { it > 0 }?.toString() ?: "") }
    var salt by remember { mutableStateOf(ingredient?.salt?.takeIf { it > 0 }?.toString() ?: "") }
    var selectedUnit by remember { mutableStateOf(ingredient?.unit ?: IngredientUnit.GRAM) }
    var imagePath by remember { mutableStateOf(ingredient?.imagePath) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCategories by remember { mutableStateOf(ingredient?.categories ?: emptyList()) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var caloriesError by remember { mutableStateOf(false) }

    // Bildauswahl
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        ),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .fillMaxHeight(0.95f),
        title = {
            Text(if (ingredient == null) "Zutat hinzufügen" else "Zutat bearbeiten")
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .imePadding(), // Wichtig für Tastatur-Handling
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Bild-Auswahl
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        imageUri != null -> {
                            Image(
                                painter = rememberAsyncImagePainter(imageUri),
                                contentDescription = "Ausgewähltes Bild",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        imagePath != null && File(imagePath).exists() -> {
                            Image(
                                painter = rememberAsyncImagePainter(File(imagePath)),
                                contentDescription = "Vorhandenes Bild",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription = "Bild hinzufügen",
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Bild hinzufügen",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Name *") },
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Beschreibung") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                // Kategorien
                OutlinedTextField(
                    value = if (selectedCategories.isEmpty()) "Keine Kategorien ausgewählt" else selectedCategories.joinToString { it.displayName },
                    onValueChange = { },
                    label = { Text("Kategorien") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCategoryDialog = true },
                    trailingIcon = {
                        TextButton(onClick = { showCategoryDialog = true }) {
                            Text("Auswählen")
                        }
                    }
                )

                // Einheit auswählen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = selectedUnit == IngredientUnit.GRAM,
                        onClick = { selectedUnit = IngredientUnit.GRAM },
                        label = { Text("Pro 100g") }
                    )
                    FilterChip(
                        selected = selectedUnit == IngredientUnit.MILLILITER,
                        onClick = { selectedUnit = IngredientUnit.MILLILITER },
                        label = { Text("Pro 100ml") }
                    )
                    FilterChip(
                        selected = selectedUnit == IngredientUnit.PIECE,
                        onClick = { selectedUnit = IngredientUnit.PIECE },
                        label = { Text("Pro Stück") }
                    )
                }

                Text(
                    text = when (selectedUnit) {
                        IngredientUnit.GRAM -> "Nährwerte pro 100g"
                        IngredientUnit.MILLILITER -> "Nährwerte pro 100ml"
                        IngredientUnit.PIECE -> "Nährwerte pro Stück"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )

                OutlinedTextField(
                    value = calories,
                    onValueChange = {
                        calories = it.filter { char -> char.isDigit() || char == '.' }
                        caloriesError = false
                    },
                    label = { Text("Kalorien (kcal) *") },
                    placeholder = { Text("z.B. 250") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = caloriesError,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Protein (g)") },
                        placeholder = { Text("z.B. 20") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Kohlenhydrate (g)") },
                        placeholder = { Text("z.B. 30") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Fett (g)") },
                        placeholder = { Text("z.B. 15") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = fiber,
                        onValueChange = { fiber = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Ballaststoffe (g)") },
                        placeholder = { Text("z.B. 5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sugar,
                        onValueChange = { sugar = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Zucker (g)") },
                        placeholder = { Text("z.B. 10") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = salt,
                        onValueChange = { salt = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Salz (g)") },
                        placeholder = { Text("z.B. 2") },
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
                        // Bild speichern wenn neues ausgewählt wurde
                        val finalImagePath = if (imageUri != null) {
                            ImageUtils.saveImageToInternalStorage(context, imageUri!!, "ingredient_${System.currentTimeMillis()}")
                        } else {
                            imagePath
                        }

                        val newIngredient = Ingredient(
                            id = ingredient?.id ?: 0,
                            name = name.trim(),
                            description = description.trim(),
                            calories = calories.toDoubleOrNull() ?: 0.0,
                            protein = protein.toDoubleOrNull() ?: 0.0,
                            carbs = carbs.toDoubleOrNull() ?: 0.0,
                            fat = fat.toDoubleOrNull() ?: 0.0,
                            fiber = fiber.toDoubleOrNull() ?: 0.0,
                            sugar = sugar.toDoubleOrNull() ?: 0.0,
                            salt = salt.toDoubleOrNull() ?: 0.0,
                            unit = selectedUnit,
                            imagePath = finalImagePath,
                            categories = selectedCategories
                        )
                        onSave(newIngredient)
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

    if (showCategoryDialog) {
        ImprovedCategorySelectionDialog(
            selectedCategories = selectedCategories,
            onDismiss = { showCategoryDialog = false },
            onConfirm = { categories ->
                selectedCategories = categories
                showCategoryDialog = false
            }
        )
    }
}