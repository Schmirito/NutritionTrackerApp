package com.example.nutritiontracker.di


import android.content.Context
import com.example.nutritiontracker.data.database.NutritionDatabase
import com.example.nutritiontracker.data.database.dao.*
import com.example.nutritiontracker.data.repository.*
import com.example.nutritiontracker.data.repository.impl.*
import com.example.nutritiontracker.viewmodel.MainViewModelFactory

/**
 * Manual Dependency Injection Module
 *
 * Diese Klasse ersetzt ein DI-Framework wie Hilt/Dagger für einfache Setup
 * und stellt alle notwendigen Dependencies zur Verfügung.
 */
object AppModule {

    @Volatile
    private var database: NutritionDatabase? = null

    @Volatile
    private var nutritionRepository: NutritionRepository? = null

    @Volatile
    private var viewModelFactory: MainViewModelFactory? = null

    private fun getDatabase(context: Context): NutritionDatabase {
        return database ?: synchronized(this) {
            val instance = NutritionDatabase.getDatabase(context.applicationContext)
            database = instance
            instance
        }
    }

    private fun getIngredientRepository(context: Context): IngredientRepository {
        return IngredientRepositoryImpl(getDatabase(context).ingredientDao())
    }

    private fun getRecipeRepository(context: Context): RecipeRepository {
        return RecipeRepositoryImpl(getDatabase(context).recipeDao())
    }

    private fun getDiaryRepository(context: Context): DiaryRepository {
        return DiaryRepositoryImpl(getDatabase(context).diaryDao())
    }

    private fun getShoppingListRepository(context: Context): ShoppingListRepository {
        return ShoppingListRepositoryImpl(getDatabase(context).shoppingListDao())
    }

    fun getNutritionRepository(context: Context): NutritionRepository {
        return nutritionRepository ?: synchronized(this) {
            val instance = NutritionRepository(
                ingredientRepository = getIngredientRepository(context),
                recipeRepository = getRecipeRepository(context),
                diaryRepository = getDiaryRepository(context),
                shoppingListRepository = getShoppingListRepository(context)
            )
            nutritionRepository = instance
            instance
        }
    }

    fun getViewModelFactory(context: Context): MainViewModelFactory {
        return viewModelFactory ?: synchronized(this) {
            val instance = MainViewModelFactory(
                repository = getNutritionRepository(context)
            )
            viewModelFactory = instance
            instance
        }
    }

    /**
     * Für Testing - ermöglicht das Ersetzen von Dependencies
     */
    fun provideMockRepository(mockRepository: NutritionRepository) {
        nutritionRepository = mockRepository
    }

    /**
     * Cleanup für Tests oder App-Neustart
     */
    fun clearInstances() {
        database?.close()
        database = null
        nutritionRepository = null
        viewModelFactory = null
    }
}