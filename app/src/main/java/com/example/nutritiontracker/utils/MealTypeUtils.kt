package com.example.nutritiontracker.utils

import com.example.nutritiontracker.data.models.MealType

object MealTypeUtils {
    fun getMealTypeName(mealType: MealType): String {
        return when(mealType) {
            MealType.BREAKFAST -> "Frühstück"
            MealType.LUNCH -> "Mittagessen"
            MealType.DINNER -> "Abendessen"
            MealType.SNACK -> "Snack"
        }
    }
}