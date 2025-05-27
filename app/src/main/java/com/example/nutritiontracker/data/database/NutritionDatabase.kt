package com.example.nutritiontracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.nutritiontracker.data.database.converters.Converters
import com.example.nutritiontracker.data.database.dao.DiaryDao
import com.example.nutritiontracker.data.database.dao.IngredientDao
import com.example.nutritiontracker.data.database.dao.RecipeDao
import com.example.nutritiontracker.data.database.dao.ShoppingListDao
import com.example.nutritiontracker.data.database.entities.*

@Database(
    entities = [
        Ingredient::class,
        Recipe::class,
        RecipeIngredient::class,
        DiaryEntry::class,
        ShoppingListItem::class
    ],
    version = 6,  // ERHÖHT auf Version 6 für ML-Support!
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NutritionDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
    abstract fun diaryDao(): DiaryDao
    abstract fun shoppingListDao(): ShoppingListDao

    companion object {
        @Volatile
        private var INSTANCE: NutritionDatabase? = null

        private const val DATABASE_NAME = "nutrition_database"

        fun getDatabase(context: Context): NutritionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NutritionDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}