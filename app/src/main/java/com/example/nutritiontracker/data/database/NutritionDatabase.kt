package com.example.nutritiontracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 4,
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

        fun getDatabase(context: Context): NutritionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NutritionDatabase::class.java,
                    "nutrition_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }

        // Migrations
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ingredients ADD COLUMN description TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE ingredients ADD COLUMN imagePath TEXT")
                db.execSQL("ALTER TABLE ingredients ADD COLUMN unit TEXT NOT NULL DEFAULT 'GRAM'")
                db.execSQL("ALTER TABLE recipes ADD COLUMN imagePath TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ingredients ADD COLUMN categories TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE recipes ADD COLUMN categories TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Erstelle Einkaufsliste Tabelle
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS shopping_list_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        amount TEXT NOT NULL DEFAULT '',
                        isChecked INTEGER NOT NULL DEFAULT 0,
                        ingredientId INTEGER,
                        recipeId INTEGER,
                        isManualEntry INTEGER NOT NULL DEFAULT 0,
                        createdDate INTEGER NOT NULL
                    )
                """)
            }
        }
    }
}