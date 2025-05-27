package com.example.nutritiontracker.data.database.dao

import androidx.room.*
import com.example.nutritiontracker.data.database.entities.Recipe
import com.example.nutritiontracker.data.database.entities.RecipeIngredient
import com.example.nutritiontracker.data.models.IngredientWithAmount
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY name ASC")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Long): Recipe?

    @Query("""
        SELECT * FROM recipes 
        WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchRecipes(query: String): Flow<List<Recipe>>

    @Insert
    suspend fun insertRecipe(recipe: Recipe): Long

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Insert
    suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteRecipeIngredients(recipeId: Long)

    @Query("""
        SELECT i.id, i.name, i.calories, i.protein, i.carbs, i.fat, 
               i.fiber, i.sugar, i.salt, ri.amount, i.unit
        FROM ingredients i
        INNER JOIN recipe_ingredients ri ON i.id = ri.ingredientId
        WHERE ri.recipeId = :recipeId
    """)
    fun getIngredientsForRecipe(recipeId: Long): Flow<List<IngredientWithAmount>>
}