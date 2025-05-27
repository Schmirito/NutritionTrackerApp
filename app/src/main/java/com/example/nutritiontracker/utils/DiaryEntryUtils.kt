package com.example.nutritiontracker.utils

import com.example.nutritiontracker.data.database.entities.DiaryEntry
import com.example.nutritiontracker.data.database.entities.Ingredient
import com.example.nutritiontracker.data.database.entities.IngredientUnit
import com.example.nutritiontracker.data.models.EntryType

object DiaryEntryUtils {

    fun getDisplayAmount(entry: DiaryEntry, ingredient: Ingredient? = null): String {
        return when (entry.entryType) {
            EntryType.INGREDIENT -> {
                ingredient?.let {
                    when (it.unit) {
                        IngredientUnit.GRAM -> "${entry.amount.toInt()}g"
                        IngredientUnit.MILLILITER -> "${entry.amount.toInt()}ml"  // NEU!
                        IngredientUnit.PIECE -> "${entry.amount.toInt()} ${if (entry.amount == 1.0) "Stück" else "Stück"}"
                    }
                } ?: "${entry.amount.toInt()}g"  // Fallback
            }
            EntryType.RECIPE -> {
                "${entry.amount.toInt()} ${if (entry.amount == 1.0) "Portion" else "Portionen"}"
            }
        }
    }

    fun getNutritionInfo(nutrition: NutritionCalculator.NutritionValues): String {
        return "${nutrition.calories.toInt()} kcal | " +
                "${nutrition.protein.toInt()}g P | " +
                "${nutrition.carbs.toInt()}g K | " +
                "${nutrition.fat.toInt()}g F"
    }

    fun isManualEntry(entryName: String): Boolean {
        return entryName.startsWith("[Manuell]")
    }

    fun formatManualEntryName(name: String): String {
        return if (name.startsWith("[Manuell]")) {
            name.replace("[Manuell] ", "")
        } else {
            name
        }
    }

    // NEU: Einheit-spezifische Formatierung
    fun getUnitDisplayName(unit: IngredientUnit): String {
        return when (unit) {
            IngredientUnit.GRAM -> "g"
            IngredientUnit.MILLILITER -> "ml"
            IngredientUnit.PIECE -> "Stück"
        }
    }

    fun getUnitDisplayNameLong(unit: IngredientUnit): String {
        return when (unit) {
            IngredientUnit.GRAM -> "Gramm"
            IngredientUnit.MILLILITER -> "Milliliter"
            IngredientUnit.PIECE -> "Stück"
        }
    }
}