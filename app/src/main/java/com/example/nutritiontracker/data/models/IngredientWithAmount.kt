package com.example.nutritiontracker.data.models

import com.example.nutritiontracker.data.database.entities.IngredientUnit

data class IngredientWithAmount(
    val id: Long,
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val sugar: Double,
    val salt: Double,
    val amount: Double,
    val unit: IngredientUnit = IngredientUnit.GRAM // Standardwert für Kompatibilität
)