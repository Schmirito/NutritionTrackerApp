package com.example.nutritiontracker.utils

import com.example.nutritiontracker.data.database.entities.Ingredient
import com.example.nutritiontracker.data.database.entities.Recipe

object ValidationUtils {

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList()
    ) {
        companion object {
            fun success() = ValidationResult(true)
            fun error(vararg messages: String) = ValidationResult(false, messages.toList())
        }
    }

    fun validateIngredient(ingredient: Ingredient): ValidationResult {
        val errors = mutableListOf<String>()

        // Name validation
        if (ingredient.name.isBlank()) {
            errors.add("Name darf nicht leer sein")
        }

        if (ingredient.name.length > 100) {
            errors.add("Name darf maximal 100 Zeichen lang sein")
        }

        // Calories validation
        if (ingredient.calories < Constants.Nutrition.MIN_CALORIES) {
            errors.add("Kalorien müssen mindestens ${Constants.Nutrition.MIN_CALORIES} betragen")
        }

        if (ingredient.calories > Constants.Nutrition.MAX_CALORIES) {
            errors.add("Kalorien dürfen maximal ${Constants.Nutrition.MAX_CALORIES} betragen")
        }

        // Macronutrients validation
        if (ingredient.protein < 0) {
            errors.add("Protein-Wert darf nicht negativ sein")
        }

        if (ingredient.carbs < 0) {
            errors.add("Kohlenhydrat-Wert darf nicht negativ sein")
        }

        if (ingredient.fat < 0) {
            errors.add("Fett-Wert darf nicht negativ sein")
        }

        // Micronutrients validation
        if (ingredient.fiber < 0) {
            errors.add("Ballaststoff-Wert darf nicht negativ sein")
        }

        if (ingredient.sugar < 0) {
            errors.add("Zucker-Wert darf nicht negativ sein")
        }

        if (ingredient.salt < 0) {
            errors.add("Salz-Wert darf nicht negativ sein")
        }

        // Logic validation
        if (ingredient.sugar > ingredient.carbs && ingredient.carbs > 0) {
            errors.add("Zucker-Wert kann nicht höher als Kohlenhydrat-Wert sein")
        }

        return if (errors.isEmpty()) {
            ValidationResult.success()
        } else {
            ValidationResult.error(*errors.toTypedArray())
        }
    }

    fun validateRecipe(recipe: Recipe): ValidationResult {
        val errors = mutableListOf<String>()

        // Name validation
        if (recipe.name.isBlank()) {
            errors.add("Name darf nicht leer sein")
        }

        if (recipe.name.length > 100) {
            errors.add("Name darf maximal 100 Zeichen lang sein")
        }

        // Servings validation
        if (recipe.servings < 1) {
            errors.add("Portionen müssen mindestens 1 betragen")
        }

        if (recipe.servings > 50) {
            errors.add("Portionen dürfen maximal 50 betragen")
        }

        // Description validation
        if (recipe.description.length > 500) {
            errors.add("Beschreibung darf maximal 500 Zeichen lang sein")
        }

        return if (errors.isEmpty()) {
            ValidationResult.success()
        } else {
            ValidationResult.error(*errors.toTypedArray())
        }
    }

    fun validateAmount(amount: String, isDecimalAllowed: Boolean = true): ValidationResult {
        if (amount.isBlank()) {
            return ValidationResult.error("Menge darf nicht leer sein")
        }

        val amountValue = if (isDecimalAllowed) {
            amount.toDoubleOrNull()
        } else {
            amount.toIntOrNull()?.toDouble()
        }

        return when {
            amountValue == null -> ValidationResult.error("Ungültige Mengenangabe")
            amountValue <= 0 -> ValidationResult.error("Menge muss größer als 0 sein")
            amountValue > 10000 -> ValidationResult.error("Menge darf maximal 10000 betragen")
            else -> ValidationResult.success()
        }
    }

    fun validateManualEntry(name: String, calories: String): ValidationResult {
        val errors = mutableListOf<String>()

        // Name validation
        if (name.isBlank()) {
            errors.add("Bezeichnung darf nicht leer sein")
        }

        if (name.length > 100) {
            errors.add("Bezeichnung darf maximal 100 Zeichen lang sein")
        }

        // Calories validation
        val caloriesValue = calories.toDoubleOrNull()
        when {
            calories.isBlank() -> errors.add("Kalorien müssen angegeben werden")
            caloriesValue == null -> errors.add("Ungültiger Kalorienwert")
            caloriesValue < 0 -> errors.add("Kalorien dürfen nicht negativ sein")
            caloriesValue > Constants.Nutrition.MAX_CALORIES -> {
                errors.add("Kalorien dürfen maximal ${Constants.Nutrition.MAX_CALORIES} betragen")
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.success()
        } else {
            ValidationResult.error(*errors.toTypedArray())
        }
    }
}