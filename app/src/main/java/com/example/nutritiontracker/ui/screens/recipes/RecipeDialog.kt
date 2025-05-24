package com.example.nutritiontracker.ui.screens.recipes

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.example.nutritiontracker.data.database.entities.Ingredient
import com.example.nutritiontracker.data.database.entities.IngredientUnit
import com.example.nutritiontracker.data.database.entities.Recipe
import com.example.nutritiontracker.utils.ImageUtils
import com.example.nutritiontracker.viewmodel.MainViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDialog(
    recipe: Recipe?,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(recipe?.name ?: "") }
    var description by remember { mutableStateOf(recipe?.description ?: "") }
    var servings by remember { mutableStateOf(recipe?.servings?.toString() ?: "1") }
    var selectedIngredients by remember { mutableStateOf<List<Triple<Ingredient, Double, IngredientUnit>>>(emptyList()) }
    val availableIngredients by viewModel.ingredients.collectAsState(initial = emptyList())
    var showIngredientPicker by remember { mutableStateOf(false) }
    var imagePath by remember { mutableStateOf(recipe?.imagePath) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var nameError by remember { mutableStateOf(false) }
    var servingsError by remember { mutableStateOf(false) }

    // Bildauswahl
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
        }
    }

    // Load existing ingredients if editing
    LaunchedEffect(recipe) {
        recipe?.let {
            scope.launch {
                val ingredientsWithAmount = viewModel.getIngredientsForRecipe(it.id).first()
                val ingredientsList = mutableListOf<Triple<Ingredient, Double, IngredientUnit>>()

                ingredientsWithAmount.forEach { ing ->
                    // Hole die vollständige Ingredient-Entity mit Unit-Information
                    val fullIngredient = viewModel.getIngredientById(ing.id)
                    fullIngredient?.let { ingredient ->
                        ingredientsList.add(Triple(ingredient, ing.amount, ingredient.unit))
                    }
                }

                selectedIngredients = ingredientsList
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .fillMaxHeight(0.9f),
        title = {
            Text(if (recipe == null) "Rezept hinzufügen" else "Rezept bearbeiten")
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
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

                // Scrollbare Beschreibung mit fester Höhe
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Beschreibung") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    minLines = 3,
                    maxLines = 5
                )

                OutlinedTextField(
                    value = servings,
                    onValueChange = {
                        servings = it.filter { char -> char.isDigit() }
                        servingsError = false
                    },
                    label = { Text("Portionen *") },
                    isError = servingsError,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Zutaten",
                        style = MaterialTheme.typography.titleSmall
                    )

                    TextButton(
                        onClick = { showIngredientPicker = true },
                        enabled = availableIngredients.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("Hinzufügen")
                    }
                }

                // Zutaten-Liste mit fester Höhe
                if (selectedIngredients.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Noch keine Zutaten hinzugefügt",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(selectedIngredients) { (ingredient, amount, unit) ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ingredient.name,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = when (unit) {
                                                    IngredientUnit.GRAM -> "${amount.toInt()}g"
                                                    IngredientUnit.PIECE -> "${amount.toInt()} Stück"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = when (unit) {
                                                    IngredientUnit.GRAM -> "${ingredient.calories.toInt()} kcal/100g"
                                                    IngredientUnit.PIECE -> "${ingredient.calories.toInt()} kcal/Stück"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            selectedIngredients = selectedIngredients.filter { it.first.id != ingredient.id }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Entfernen",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
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

                    val servingsInt = servings.toIntOrNull()
                    if (servingsInt == null || servingsInt < 1) {
                        servingsError = true
                        hasError = true
                    }

                    if (!hasError) {
                        // Bild speichern wenn neues ausgewählt wurde
                        val finalImagePath = if (imageUri != null) {
                            ImageUtils.saveImageToInternalStorage(context, imageUri!!, "recipe_${System.currentTimeMillis()}")
                        } else {
                            imagePath
                        }

                        val newRecipe = Recipe(
                            id = recipe?.id ?: 0,
                            name = name.trim(),
                            description = description.trim(),
                            servings = servingsInt ?: 1,
                            imagePath = finalImagePath
                        )
                        val ingredients = selectedIngredients.map { (ing, amount, _) ->
                            ing.id to amount
                        }
                        viewModel.addOrUpdateRecipe(newRecipe, ingredients)
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

    if (showIngredientPicker) {
        IngredientPickerDialog(
            ingredients = availableIngredients.filter { ingredient ->
                selectedIngredients.none { it.first.id == ingredient.id }
            },
            onSelect = { ingredient, amount ->
                selectedIngredients = selectedIngredients + Triple(ingredient, amount, ingredient.unit)
                showIngredientPicker = false
            },
            onDismiss = { showIngredientPicker = false }
        )
    }
}