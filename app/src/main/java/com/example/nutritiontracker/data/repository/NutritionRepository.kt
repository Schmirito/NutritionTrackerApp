package com.example.nutritiontracker.data.repository

import android.content.Context
import android.net.Uri
import com.example.nutritiontracker.data.database.entities.*
import com.example.nutritiontracker.data.models.*
import com.example.nutritiontracker.utils.ExportImportManager
import com.example.nutritiontracker.utils.NutritionCalculator
import kotlinx.coroutines.flow.*

class NutritionRepository(
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository: RecipeRepository,
    private val diaryRepository: DiaryRepository,
    private val shoppingListRepository: ShoppingListRepository
) {
    // Ingredient operations
    fun getAllIngredients() = ingredientRepository.getAllIngredients()
    suspend fun getIngredientById(id: Long) = ingredientRepository.getIngredientById(id)
    suspend fun insertIngredient(ingredient: Ingredient) = ingredientRepository.insertIngredient(ingredient)
    suspend fun updateIngredient(ingredient: Ingredient) = ingredientRepository.updateIngredient(ingredient)
    suspend fun deleteIngredient(ingredient: Ingredient) = ingredientRepository.deleteIngredient(ingredient)
    fun searchIngredients(query: String) = ingredientRepository.searchIngredients(query)

    // Recipe operations
    fun getAllRecipes() = recipeRepository.getAllRecipes()
    suspend fun getRecipeById(id: Long) = recipeRepository.getRecipeById(id)
    suspend fun insertRecipe(recipe: Recipe) = recipeRepository.insertRecipe(recipe)
    suspend fun updateRecipe(recipe: Recipe) = recipeRepository.updateRecipe(recipe)
    suspend fun deleteRecipe(recipe: Recipe) = recipeRepository.deleteRecipe(recipe)
    fun getIngredientsForRecipe(recipeId: Long) = recipeRepository.getIngredientsForRecipe(recipeId)
    fun searchRecipes(query: String) = recipeRepository.searchRecipes(query)

    // Recipe ingredient operations
    suspend fun addOrUpdateRecipe(recipe: Recipe, ingredients: List<Pair<Long, Double>>) {
        val recipeId = if (recipe.id == 0L) {
            insertRecipe(recipe)
        } else {
            updateRecipe(recipe)
            recipeRepository.deleteRecipeIngredients(recipe.id)
            recipe.id
        }

        ingredients.forEach { (ingredientId, amount) ->
            recipeRepository.insertRecipeIngredient(
                RecipeIngredient(recipeId, ingredientId, amount)
            )
        }
    }

    // Diary operations
    fun getEntriesForDate(date: Long) = diaryRepository.getEntriesForDate(date)
    fun getEntriesForDateRange(startDate: Long, endDate: Long) = diaryRepository.getEntriesForDateRange(startDate, endDate)
    suspend fun insertDiaryEntry(entry: DiaryEntry) = diaryRepository.insertEntry(entry)
    suspend fun updateDiaryEntry(entry: DiaryEntry) = diaryRepository.updateEntry(entry)
    suspend fun deleteDiaryEntry(entry: DiaryEntry) = diaryRepository.deleteEntry(entry)

    // Shopping list operations
    fun getAllShoppingItems() = shoppingListRepository.getAllItems()
    suspend fun insertShoppingItem(item: ShoppingListItem) = shoppingListRepository.insertItem(item)
    suspend fun updateShoppingItem(item: ShoppingListItem) = shoppingListRepository.updateItem(item)
    suspend fun deleteShoppingItem(item: ShoppingListItem) = shoppingListRepository.deleteItem(item)
    suspend fun deleteCheckedShoppingItems() = shoppingListRepository.deleteCheckedItems()
    suspend fun updateShoppingItemChecked(id: Long, checked: Boolean) = shoppingListRepository.updateCheckedStatus(id, checked)

    // Complex operations
    suspend fun addIngredientAndCreateDiaryEntry(
        ingredient: Ingredient,
        mealType: MealType,
        date: Long,
        amount: Double
    ) {
        val ingredientId = insertIngredient(ingredient)
        val entry = DiaryEntry(
            date = date,
            mealType = mealType,
            entryType = EntryType.INGREDIENT,
            ingredientId = ingredientId,
            amount = amount
        )
        insertDiaryEntry(entry)
    }


    suspend fun addRecipeToShoppingList(recipeId: Long) {
        val ingredients = getIngredientsForRecipe(recipeId).first()

        // WICHTIG: Entferne die Prüfung auf existierende Items!
        // Jede Zutat wird einzeln hinzugefügt, auch wenn sie schon existiert

        ingredients.forEach { ingredientWithAmount ->
            val amount = when (ingredientWithAmount.unit) {
                IngredientUnit.GRAM -> "${ingredientWithAmount.amount.toInt()}g"
                IngredientUnit.MILLILITER -> "${ingredientWithAmount.amount.toInt()}ml"
                IngredientUnit.PIECE -> "${ingredientWithAmount.amount.toInt()} Stück"
            }

            insertShoppingItem(
                ShoppingListItem(
                    name = ingredientWithAmount.name,
                    amount = amount,
                    ingredientId = ingredientWithAmount.id,
                    recipeId = recipeId,
                    isManualEntry = false
                )
            )
        }
    }
    // Nutrition calculations
    suspend fun calculateDailyNutrition(entries: List<DiaryEntry>): NutritionCalculator.NutritionValues {
        val nutritionList = mutableListOf<NutritionCalculator.NutritionValues>()

        entries.forEach { entry ->
            when (entry.entryType) {
                EntryType.INGREDIENT -> {
                    if (entry.isManualEntry) {
                        // Manuelle Einträge verwenden direkte Werte
                        nutritionList.add(
                            NutritionCalculator.NutritionValues(
                                calories = entry.manualEntryCalories ?: 0.0,
                                protein = entry.manualEntryProtein ?: 0.0,
                                carbs = entry.manualEntryCarbs ?: 0.0,
                                fat = entry.manualEntryFat ?: 0.0,
                                fiber = 0.0,
                                sugar = 0.0,
                                salt = 0.0
                            )
                        )
                    } else {
                        entry.ingredientId?.let { id ->
                            getIngredientById(id)?.let { ingredient ->
                                nutritionList.add(
                                    NutritionCalculator.calculateNutritionForIngredient(ingredient, entry.amount)
                                )
                            }
                        }
                    }
                }
                EntryType.RECIPE -> {
                    entry.recipeId?.let { id ->
                        val recipe = getRecipeById(id)
                        recipe?.let {
                            val ingredients = getIngredientsForRecipe(id).first()
                            nutritionList.add(
                                NutritionCalculator.calculateNutritionForRecipe(
                                    ingredients,
                                    recipe.servings,
                                    entry.amount
                                )
                            )
                        }
                    }
                }
            }
        }

        return NutritionCalculator.sumNutritionValues(nutritionList)
    }

    fun getWeeklyStats(endDate: Long): Flow<WeeklyStats> {
        val startOfWeek = com.example.nutritiontracker.utils.DateUtils.getStartOfWeek(endDate)
        val endOfWeek = com.example.nutritiontracker.utils.DateUtils.getEndOfDay(endDate)

        return getEntriesForDateRange(startOfWeek, endOfWeek)
            .map { entries -> calculateWeeklyStats(entries) }
    }

    private suspend fun calculateWeeklyStats(entries: List<DiaryEntry>): WeeklyStats {
        val dailyNutrition = mutableMapOf<Long, MutableList<NutritionCalculator.NutritionValues>>()

        entries.forEach { entry ->
            val dayKey = com.example.nutritiontracker.utils.DateUtils.getStartOfDay(entry.date)
            val nutritionList = dailyNutrition.getOrPut(dayKey) { mutableListOf() }

            val nutrition = calculateDailyNutrition(listOf(entry))
            nutritionList.add(nutrition)
        }

        val dailyTotals = dailyNutrition.map { (_, nutritionList) ->
            NutritionCalculator.sumNutritionValues(nutritionList)
        }

        return if (dailyTotals.isEmpty()) {
            WeeklyStats()
        } else {
            val average = NutritionCalculator.averageNutritionValues(dailyTotals)
            WeeklyStats(
                avgCalories = average.calories,
                avgProtein = average.protein,
                avgCarbs = average.carbs,
                avgFat = average.fat,
                avgFiber = average.fiber,
                avgSugar = average.sugar,
                avgSalt = average.salt,
                totalDays = dailyNutrition.size
            )
        }
    }

    // Export/Import operations - EINFACHE VERSIONEN
    suspend fun exportNutritionData(context: Context): Uri? {
        val ingredientsList = getAllIngredients().first()
        val recipesList = getAllRecipes().first()

        val exportableIngredients = ingredientsList.filter {
            !it.name.startsWith(com.example.nutritiontracker.utils.Constants.ManualEntry.PREFIX)
        }

        val recipeIngredients = mutableMapOf<Long, List<RecipeIngredient>>()
        recipesList.forEach { recipe ->
            val ingredients = getIngredientsForRecipe(recipe.id).first()
            recipeIngredients[recipe.id] = ingredients.map { ing ->
                RecipeIngredient(recipe.id, ing.id, ing.amount)
            }
        }

        return ExportImportManager.exportNutritionData(
            context,
            exportableIngredients,
            recipesList,
            recipeIngredients
        )
    }

    suspend fun exportDiaryData(context: Context): Uri? {
        val entries = getEntriesForDateRange(0, Long.MAX_VALUE).first()
        return ExportImportManager.exportDiaryData(context, entries)
    }

    suspend fun importNutritionData(context: Context, uri: Uri): Boolean {
        return try {
            val data = ExportImportManager.importNutritionData(context, uri) ?: return false
            val ingredientIdMap = mutableMapOf<Long, Long>()

            // Import ingredients
            data.ingredients.forEach { ingredient ->
                val oldId = ingredient.id
                val newId = insertIngredient(ingredient.copy(id = 0))
                ingredientIdMap[oldId] = newId
            }

            // Import recipes
            data.recipes.forEach { recipeExport ->
                val newRecipeId = insertRecipe(recipeExport.recipe.copy(id = 0))
                recipeExport.ingredients.forEach { recipeIngredient ->
                    val newIngredientId = ingredientIdMap[recipeIngredient.ingredientId]
                    if (newIngredientId != null) {
                        recipeRepository.insertRecipeIngredient(
                            RecipeIngredient(
                                recipeId = newRecipeId,
                                ingredientId = newIngredientId,
                                amount = recipeIngredient.amount
                            )
                        )
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun importDiaryData(context: Context, uri: Uri): Boolean {
        return try {
            val data = ExportImportManager.importDiaryData(context, uri) ?: return false
            data.entries.forEach { entry ->
                insertDiaryEntry(entry.copy(id = 0))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // Helper methods
    suspend fun getEntryDisplayName(entry: DiaryEntry): String {
        return if (entry.isManualEntry) {
            entry.manualEntryName ?: "Unbekannter manueller Eintrag"
        } else {
            when (entry.entryType) {
                EntryType.INGREDIENT -> {
                    entry.ingredientId?.let { id ->
                        getIngredientById(id)?.name
                    } ?: "Unbekannte Zutat"
                }
                EntryType.RECIPE -> {
                    entry.recipeId?.let { id ->
                        getRecipeById(id)?.name
                    } ?: "Unbekanntes Rezept"
                }
            }
        }
    }
}