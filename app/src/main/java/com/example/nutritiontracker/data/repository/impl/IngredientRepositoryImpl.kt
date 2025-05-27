package com.example.nutritiontracker.data.repository.impl

import com.example.nutritiontracker.data.database.dao.IngredientDao
import com.example.nutritiontracker.data.database.entities.Ingredient
import com.example.nutritiontracker.data.repository.IngredientRepository
import com.example.nutritiontracker.utils.Constants
import kotlinx.coroutines.flow.Flow

class IngredientRepositoryImpl(
    private val ingredientDao: IngredientDao
) : IngredientRepository {

    override fun getAllIngredients(): Flow<List<Ingredient>> {
        return ingredientDao.getAllIngredients()
    }

    override suspend fun getIngredientById(id: Long): Ingredient? {
        return ingredientDao.getIngredientById(id)
    }

    override suspend fun insertIngredient(ingredient: Ingredient): Long {
        return ingredientDao.insertIngredient(ingredient)
    }

    override suspend fun updateIngredient(ingredient: Ingredient) {
        ingredientDao.updateIngredient(ingredient)
    }

    override suspend fun deleteIngredient(ingredient: Ingredient) {
        ingredientDao.deleteIngredient(ingredient)
    }

    override fun searchIngredients(query: String): Flow<List<Ingredient>> {
        return if (query.isBlank()) {
            ingredientDao.getAllIngredients()
        } else {
            ingredientDao.searchIngredients(query)
        }
    }
}