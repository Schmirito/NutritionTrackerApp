package com.example.nutritiontracker.data.database.converters

import androidx.room.TypeConverter
import com.example.nutritiontracker.data.database.entities.IngredientUnit
import com.example.nutritiontracker.data.models.Category
import com.example.nutritiontracker.data.models.EntryType
import com.example.nutritiontracker.data.models.MealType

class Converters {
    @TypeConverter
    fun fromMealType(mealType: MealType): String {
        return mealType.name
    }

    @TypeConverter
    fun toMealType(mealType: String): MealType {
        return MealType.valueOf(mealType)
    }

    @TypeConverter
    fun fromEntryType(entryType: EntryType): String {
        return entryType.name
    }

    @TypeConverter
    fun toEntryType(entryType: String): EntryType {
        return EntryType.valueOf(entryType)
    }

    @TypeConverter
    fun fromIngredientUnit(unit: IngredientUnit): String {
        return unit.name
    }

    @TypeConverter
    fun toIngredientUnit(unit: String): IngredientUnit {
        return IngredientUnit.valueOf(unit)
    }

    @TypeConverter
    fun fromCategoryList(categories: List<Category>): String {
        return categories.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toCategoryList(categoriesString: String): List<Category> {
        return if (categoriesString.isEmpty()) {
            emptyList()
        } else {
            categoriesString.split(",").mapNotNull { categoryName ->
                try {
                    Category.valueOf(categoryName)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        }
    }
}