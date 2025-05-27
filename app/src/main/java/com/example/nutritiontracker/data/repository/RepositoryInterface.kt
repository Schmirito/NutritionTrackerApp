package com.example.nutritiontracker.data.repository

import com.example.nutritiontracker.data.database.entities.*
import com.example.nutritiontracker.data.models.IngredientWithAmount
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    fun getAllIngredients(): Flow<List<Ingredient>>
    suspend fun getIngredientById(id: Long): Ingredient?
    suspend fun insertIngredient(ingredient: Ingredient): Long
    suspend fun updateIngredient(ingredient: Ingredient)
    suspend fun deleteIngredient(ingredient: Ingredient)
    fun searchIngredients(query: String): Flow<List<Ingredient>>
}

interface RecipeRepository {
    fun getAllRecipes(): Flow<List<Recipe>>
    suspend fun getRecipeById(id: Long): Recipe?
    suspend fun insertRecipe(recipe: Recipe): Long
    suspend fun updateRecipe(recipe: Recipe)
    suspend fun deleteRecipe(recipe: Recipe)
    suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient)
    suspend fun deleteRecipeIngredients(recipeId: Long)
    fun getIngredientsForRecipe(recipeId: Long): Flow<List<IngredientWithAmount>>
    fun searchRecipes(query: String): Flow<List<Recipe>>
}

interface DiaryRepository {
    fun getEntriesForDateRange(startDate: Long, endDate: Long): Flow<List<DiaryEntry>>
    suspend fun insertEntry(entry: DiaryEntry)
    suspend fun updateEntry(entry: DiaryEntry)
    suspend fun deleteEntry(entry: DiaryEntry)
    fun getEntriesForDate(date: Long): Flow<List<DiaryEntry>>
}

interface ShoppingListRepository {
    fun getAllItems(): Flow<List<ShoppingListItem>>
    suspend fun insertItem(item: ShoppingListItem): Long
    suspend fun updateItem(item: ShoppingListItem)
    suspend fun deleteItem(item: ShoppingListItem)
    suspend fun deleteCheckedItems()
    suspend fun updateCheckedStatus(id: Long, checked: Boolean)
}