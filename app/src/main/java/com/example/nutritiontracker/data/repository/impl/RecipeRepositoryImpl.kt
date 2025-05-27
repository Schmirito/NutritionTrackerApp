package com.example.nutritiontracker.data.repository.impl

import com.example.nutritiontracker.data.database.dao.RecipeDao
import com.example.nutritiontracker.data.database.entities.Recipe
import com.example.nutritiontracker.data.database.entities.RecipeIngredient
import com.example.nutritiontracker.data.models.IngredientWithAmount
import com.example.nutritiontracker.data.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow

class RecipeRepositoryImpl(
    private val recipeDao: RecipeDao
) : RecipeRepository {

    override fun getAllRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes()
    }

    override suspend fun getRecipeById(id: Long): Recipe? {
        return recipeDao.getRecipeById(id)
    }

    override suspend fun insertRecipe(recipe: Recipe): Long {
        return recipeDao.insertRecipe(recipe)
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        recipeDao.updateRecipe(recipe)
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.deleteRecipe(recipe)
    }

    override suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient) {
        recipeDao.insertRecipeIngredient(recipeIngredient)
    }

    override suspend fun deleteRecipeIngredients(recipeId: Long) {
        recipeDao.deleteRecipeIngredients(recipeId)
    }

    override fun getIngredientsForRecipe(recipeId: Long): Flow<List<IngredientWithAmount>> {
        return recipeDao.getIngredientsForRecipe(recipeId)
    }

    override fun searchRecipes(query: String): Flow<List<Recipe>> {
        return if (query.isBlank()) {
            recipeDao.getAllRecipes()
        } else {
            recipeDao.searchRecipes(query)
        }
    }
}