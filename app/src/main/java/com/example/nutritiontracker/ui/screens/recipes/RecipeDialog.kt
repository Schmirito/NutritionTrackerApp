package com.example.nutritiontracker.ui.screens.recipes

import MainViewModel
import android.Manifest
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.example.nutritiontracker.data.database.entities.Ingredient
import com.example.nutritiontracker.data.database.entities.IngredientUnit
import com.example.nutritiontracker.data.database.entities.Recipe
import com.example.nutritiontracker.data.models.Category
import com.example.nutritiontracker.utils.ImageUtils
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
    var selectedCategories by remember { mutableStateOf(recipe?.categories ?: emptyList()) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var servingsError by remember { mutableStateOf(false) }

    // Automatisch berechnete Kategorien basierend auf Zutaten
    val automaticCategories by remember(selectedIngredients) {
        derivedStateOf {
            if (selectedIngredients.isEmpty()) {
                emptyList()
            } else {
                calculateAutomaticCategories(selectedIngredients.map { it.first })
            }
        }
    }

    // Kombiniere automatische und manuelle Kategorien
    val finalCategories by remember(automaticCategories, selectedCategories) {
        derivedStateOf {
            mergeCategories(automaticCategories, selectedCategories)
        }
    }

    // Bildauswahl aus Galerie
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
                try {
                    val ingredientsWithAmount = viewModel.getIngredientsForRecipe(it.id).first()
                    val ingredientsList = mutableListOf<Triple<Ingredient, Double, IngredientUnit>>()

                    ingredientsWithAmount.forEach { ing ->
                        val fullIngredient = viewModel.getIngredientById(ing.id)
                        fullIngredient?.let { ingredient ->
                            ingredientsList.add(Triple(ingredient, ing.amount, ingredient.unit))
                        }
                    }

                    selectedIngredients = ingredientsList
                } catch (e: Exception) {
                    // Handle error silently
                }
            }
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
            Text(if (recipe == null) "Rezept hinzufügen" else "Rezept bearbeiten")
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
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
                    placeholder = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Beschreibung hinzufügen...",
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    minLines = 5,
                    maxLines = 8
                )

                // Kategorien (mit automatischen Kategorien)
                OutlinedTextField(
                    value = if (finalCategories.isEmpty()) "Keine Kategorien" else finalCategories.joinToString { it.displayName },
                    onValueChange = { },
                    label = { Text("Kategorien (automatisch + manuell)") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCategoryDialog = true },
                    trailingIcon = {
                        TextButton(onClick = { showCategoryDialog = true }) {
                            Text("Bearbeiten")
                        }
                    }
                )

                // Zeige automatische Kategorien separat
                if (automaticCategories.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = "Automatisch aus Zutaten:",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = automaticCategories.joinToString(", ") { it.displayName },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

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

                // Zutaten-Liste
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
                        scope.launch {
                            try {
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
                                    imagePath = finalImagePath,
                                    categories = finalCategories
                                )
                                val ingredients = selectedIngredients.map { (ing, amount, _) ->
                                    ing.id to amount
                                }
                                viewModel.addOrUpdateRecipe(newRecipe, ingredients)
                                onDismiss()
                            } catch (e: Exception) {
                                // Handle error
                            }
                        }
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

    if (showCategoryDialog) {
        RecipeCategorySelectionDialog(
            selectedCategories = selectedCategories,
            automaticCategories = automaticCategories,
            onDismiss = { showCategoryDialog = false },
            onConfirm = { categories ->
                selectedCategories = categories
                showCategoryDialog = false
            }
        )
    }
}

// Hilfsfunktionen für automatische Kategorien
private fun calculateAutomaticCategories(ingredients: List<Ingredient>): List<Category> {
    if (ingredients.isEmpty()) return emptyList()

    val categories = mutableSetOf<Category>()

    // Prüfe Eigenschaften aller Zutaten
    val allIngredientCategories = ingredients.flatMap { it.categories }

    // Nährwert-Kategorien (diese werden nur hinzugefügt, wenn ALLE Zutaten sie haben)
    val nutritionCategories = listOf(
        Category.HIGH_PROTEIN, Category.LOW_CARB, Category.LOW_FAT,
        Category.HIGH_FIBER, Category.LOW_CALORIE
    )

    nutritionCategories.forEach { category ->
        if (ingredients.all { category in it.categories }) {
            categories.add(category)
        }
    }

    // Ernährungsform-Kategorien (diese werden entfernt, wenn EINE Zutat sie nicht hat)
    val dietCategories = mapOf(
        Category.VEGAN to listOf(Category.MEAT, Category.FISH, Category.DAIRY, Category.CHEESE, Category.EGGS),
        Category.VEGETARIAN to listOf(Category.MEAT, Category.FISH),
        Category.GLUTEN_FREE to listOf(Category.GRAINS),
        Category.LACTOSE_FREE to listOf(Category.DAIRY, Category.CHEESE)
    )

    dietCategories.forEach { (dietCategory, excludedCategories) ->
        val hasExcludedIngredient = ingredients.any { ingredient ->
            ingredient.categories.any { it in excludedCategories }
        }
        if (!hasExcludedIngredient) {
            categories.add(dietCategory)
        }
    }

    return categories.toList().sortedBy { it.ordinal }
}

private fun mergeCategories(automatic: List<Category>, manual: List<Category>): List<Category> {
    val result = mutableSetOf<Category>()
    result.addAll(automatic)
    result.addAll(manual)

    // Entferne widersprüchliche Kategorien
    val conflicts = mapOf(
        Category.VEGAN to listOf(Category.MEAT, Category.FISH, Category.DAIRY, Category.CHEESE, Category.EGGS),
        Category.VEGETARIAN to listOf(Category.MEAT, Category.FISH),
        Category.GLUTEN_FREE to listOf(Category.GRAINS),
        Category.LACTOSE_FREE to listOf(Category.DAIRY, Category.CHEESE)
    )

    conflicts.forEach { (dietCategory, conflictingCategories) ->
        if (result.contains(dietCategory) && result.any { it in conflictingCategories }) {
            result.remove(dietCategory)
        }
    }

    return result.toList().sortedBy { it.ordinal }
}

@Composable
fun RecipeCategorySelectionDialog(
    selectedCategories: List<Category>,
    automaticCategories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (List<Category>) -> Unit
) {
    var tempSelectedCategories by remember { mutableStateOf(selectedCategories) }
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kategorien bearbeiten") },
        text = {
            Column {
                // Suchfeld
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Suchen...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Info über automatische Kategorien
                if (automaticCategories.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Text(
                            text = "Automatisch: ${automaticCategories.joinToString(", ") { it.displayName }}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Gruppierte Kategorien
                val groupedCategories = mapOf(
                    "Nährwerte" to listOf(
                        Category.HIGH_PROTEIN, Category.LOW_CARB, Category.LOW_FAT,
                        Category.HIGH_FIBER, Category.LOW_CALORIE
                    ),
                    "Ernährungsform" to listOf(
                        Category.VEGAN, Category.VEGETARIAN, Category.GLUTEN_FREE,
                        Category.LACTOSE_FREE, Category.KETO, Category.PALEO
                    ),
                    "Mahlzeiten" to listOf(
                        Category.BREAKFAST, Category.LUNCH, Category.DINNER,
                        Category.SNACK, Category.DESSERT
                    ),
                    "Zubereitung" to listOf(
                        Category.QUICK, Category.EASY, Category.MEAL_PREP
                    )
                )

                LazyColumn(
                    modifier = Modifier.height(400.dp)
                ) {
                    groupedCategories.forEach { (groupName, categories) ->
                        val filteredCategories = categories.filter {
                            searchQuery.isEmpty() || it.displayName.contains(searchQuery, ignoreCase = true)
                        }

                        if (filteredCategories.isNotEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Text(
                                        text = groupName,
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }

                            items(filteredCategories) { category ->
                                val isAutomatic = category in automaticCategories
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clickable(enabled = !isAutomatic) {
                                            if (!isAutomatic) {
                                                tempSelectedCategories = if (category in tempSelectedCategories) {
                                                    tempSelectedCategories - category
                                                } else {
                                                    tempSelectedCategories + category
                                                }
                                            }
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            isAutomatic -> MaterialTheme.colorScheme.tertiaryContainer
                                            category in tempSelectedCategories -> MaterialTheme.colorScheme.secondaryContainer
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = category.displayName,
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (isAutomatic) {
                                            Text(
                                                text = "Auto",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        } else {
                                            Checkbox(
                                                checked = category in tempSelectedCategories,
                                                onCheckedChange = { checked ->
                                                    tempSelectedCategories = if (checked) {
                                                        tempSelectedCategories + category
                                                    } else {
                                                        tempSelectedCategories - category
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(tempSelectedCategories) }) {
                Text("Anwenden")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}