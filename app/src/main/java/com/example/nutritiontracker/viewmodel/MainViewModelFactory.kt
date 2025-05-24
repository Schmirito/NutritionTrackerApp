package com.example.nutritiontracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nutritiontracker.data.database.dao.DiaryDao
import com.example.nutritiontracker.data.database.dao.IngredientDao
import com.example.nutritiontracker.data.database.dao.RecipeDao

class MainViewModelFactory(
    private val ingredientDao: IngredientDao,
    private val recipeDao: RecipeDao,
    private val diaryDao: DiaryDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(ingredientDao, recipeDao, diaryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}