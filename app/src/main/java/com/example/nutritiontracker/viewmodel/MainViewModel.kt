package com.example.nutritiontracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritiontracker.data.database.dao.DiaryDao
import com.example.nutritiontracker.data.database.dao.IngredientDao
import com.example.nutritiontracker.data.database.dao.RecipeDao
import com.example.nutritiontracker.data.database.entities.DiaryEntry
import com.example.nutritiontracker.data.database.entities.Ingredient
import com.example.nutritiontracker.data.database.entities.Recipe
import com.example.nutritiontracker.data.database.entities.RecipeIngredient
import com.example.nutritiontracker.data.models.EntryType
import com.example.nutritiontracker.data.models.IngredientWithAmount
import com.example.nutritiontracker.data.models.WeeklyStats
import com.example.nutritiontracker.utils.DateUtils
import com.example.nutritiontracker.utils.NutritionCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(
    private val ingredientDao: IngredientDao,
    private val recipeDao: RecipeDao,
    private val diaryDao: DiaryDao
) : ViewModel() {

    val ingredients = ingredientDao.getAllIngredients()
    val recipes = recipeDao.getAllRecipes()

    fun getDiaryEntriesForDate(date: Long): Flow<List<DiaryEntry>> {
        val startOfDay = DateUtils.getStartOfDay(date)
        val endOfDay = DateUtils.getEndOfDay(date) + 1
        return diaryDao.getEntriesForDateRange(startOfDay, endOfDay)
    }

    fun getWeeklyStats(endDate: Long): Flow<WeeklyStats> {
        val endOfWeek = DateUtils.getEndOfDay(endDate)
        val startOfWeek = DateUtils.getStartOfWeek(endDate)

        return diaryDao.getEntriesForDateRange(startOfWeek, endOfWeek)
            .map { entries ->
                calculateWeeklyStats(entries)
            }
    }

    private suspend fun calculateWeeklyStats(entries: List<DiaryEntry>): WeeklyStats {
        val dailyNutrition = mutableMapOf<Long, MutableList<NutritionCalculator.NutritionValues>>()

        entries.forEach { entry ->
            val dayKey = DateUtils.getStartOfDay(entry.date)
            val nutritionList = dailyNutrition.getOrPut(dayKey) { mutableListOf() }

            when (entry.entryType) {
                EntryType.INGREDIENT -> {
                    entry.ingredientId?.let { id ->
                        ingredientDao.getIngredientById(id)?.let { ingredient ->
                            nutritionList.add(
                                NutritionCalculator.calculateNutritionForIngredient(ingredient, entry.amount)
                            )
                        }
                    }
                }
                EntryType.RECIPE -> {
                    entry.recipeId?.let { id ->
                        val recipe = recipeDao.getRecipeById(id)
                        recipe?.let {
                            val ingredients = recipeDao.getIngredientsForRecipe(id).first()
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

    fun addIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            ingredientDao.insertIngredient(ingredient)
        }
    }

    fun updateIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            ingredientDao.updateIngredient(ingredient)
        }
    }

    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            ingredientDao.deleteIngredient(ingredient)
        }
    }

    fun addRecipe(recipe: Recipe, ingredients: List<Pair<Long, Double>>) {
        viewModelScope.launch {
            val recipeId = recipeDao.insertRecipe(recipe)
            ingredients.forEach { (ingredientId, amount) ->
                recipeDao.insertRecipeIngredient(
                    RecipeIngredient(recipeId, ingredientId, amount)
                )
            }
        }
    }

    fun addOrUpdateRecipe(recipe: Recipe, ingredients: List<Pair<Long, Double>>) {
        viewModelScope.launch {
            if (recipe.id == 0L) {
                addRecipe(recipe, ingredients)
            } else {
                recipeDao.updateRecipe(recipe)
                recipeDao.deleteRecipeIngredients(recipe.id)
                ingredients.forEach { (ingredientId, amount) ->
                    recipeDao.insertRecipeIngredient(
                        RecipeIngredient(recipe.id, ingredientId, amount)
                    )
                }
            }
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            recipeDao.deleteRecipe(recipe)
        }
    }

    fun addDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            diaryDao.insertEntry(entry)
        }
    }

    fun updateDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            diaryDao.updateEntry(entry)
        }
    }

    fun deleteDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            diaryDao.deleteEntry(entry)
        }
    }

    fun getIngredientsForRecipe(recipeId: Long): Flow<List<IngredientWithAmount>> {
        return recipeDao.getIngredientsForRecipe(recipeId)
    }

    suspend fun getIngredientById(id: Long): Ingredient? {
        return ingredientDao.getIngredientById(id)
    }

    suspend fun getEntryDisplayName(entry: DiaryEntry): String {
        return when (entry.entryType) {
            EntryType.INGREDIENT -> {
                entry.ingredientId?.let { id ->
                    ingredientDao.getIngredientById(id)?.name
                } ?: "Unbekannte Zutat"
            }
            EntryType.RECIPE -> {
                entry.recipeId?.let { id ->
                    recipeDao.getRecipeById(id)?.name
                } ?: "Unbekanntes Rezept"
            }
        }
    }

    suspend fun calculateDailyNutrition(entries: List<DiaryEntry>): NutritionCalculator.NutritionValues {
        val nutritionList = mutableListOf<NutritionCalculator.NutritionValues>()

        entries.forEach { entry ->
            when (entry.entryType) {
                EntryType.INGREDIENT -> {
                    entry.ingredientId?.let { id ->
                        ingredientDao.getIngredientById(id)?.let { ingredient ->
                            nutritionList.add(
                                NutritionCalculator.calculateNutritionForIngredient(ingredient, entry.amount)
                            )
                        }
                    }
                }
                EntryType.RECIPE -> {
                    entry.recipeId?.let { id ->
                        val recipe = recipeDao.getRecipeById(id)
                        recipe?.let {
                            val ingredients = recipeDao.getIngredientsForRecipe(id).first()
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

    suspend fun calculateMealCalories(entries: List<DiaryEntry>): Double {
        val nutrition = calculateDailyNutrition(entries)
        return nutrition.calories
    }
}