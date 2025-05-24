package com.example.nutritiontracker.utils


import com.example.nutritiontracker.data.database.entities.Ingredient
import com.example.nutritiontracker.data.database.entities.IngredientUnit
import com.example.nutritiontracker.data.models.IngredientWithAmount

object NutritionCalculator {

    data class NutritionValues(
        val calories: Double,
        val protein: Double,
        val carbs: Double,
        val fat: Double,
        val fiber: Double,
        val sugar: Double,
        val salt: Double
    )

    fun calculateNutritionForIngredient(ingredient: Ingredient, amount: Double): NutritionValues {
        val factor = when (ingredient.unit) {
            IngredientUnit.GRAM -> amount / 100.0  // amount ist in Gramm, Nährwerte sind pro 100g
            IngredientUnit.PIECE -> amount  // amount ist Stückzahl, Nährwerte sind pro Stück
        }

        return NutritionValues(
            calories = ingredient.calories * factor,
            protein = ingredient.protein * factor,
            carbs = ingredient.carbs * factor,
            fat = ingredient.fat * factor,
            fiber = ingredient.fiber * factor,
            sugar = ingredient.sugar * factor,
            salt = ingredient.salt * factor
        )
    }

    fun calculateNutritionForRecipe(
        ingredients: List<IngredientWithAmount>,
        servings: Int,
        consumedServings: Double
    ): NutritionValues {
        var totalCalories = 0.0
        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFat = 0.0
        var totalFiber = 0.0
        var totalSugar = 0.0
        var totalSalt = 0.0

        ingredients.forEach { ingredientWithAmount ->
            // Die amount in der Datenbank ist bereits die korrekte Menge (Gramm oder Stückzahl)
            // Wir müssen prüfen, welche Einheit die Zutat hat
            val factor = ingredientWithAmount.amount / 100.0

            totalCalories += ingredientWithAmount.calories * factor
            totalProtein += ingredientWithAmount.protein * factor
            totalCarbs += ingredientWithAmount.carbs * factor
            totalFat += ingredientWithAmount.fat * factor
            totalFiber += ingredientWithAmount.fiber * factor
            totalSugar += ingredientWithAmount.sugar * factor
            totalSalt += ingredientWithAmount.salt * factor
        }

        val servingFactor = consumedServings / servings

        return NutritionValues(
            calories = totalCalories * servingFactor,
            protein = totalProtein * servingFactor,
            carbs = totalCarbs * servingFactor,
            fat = totalFat * servingFactor,
            fiber = totalFiber * servingFactor,
            sugar = totalSugar * servingFactor,
            salt = totalSalt * servingFactor
        )
    }

    fun sumNutritionValues(values: List<NutritionValues>): NutritionValues {
        return NutritionValues(
            calories = values.sumOf { it.calories },
            protein = values.sumOf { it.protein },
            carbs = values.sumOf { it.carbs },
            fat = values.sumOf { it.fat },
            fiber = values.sumOf { it.fiber },
            sugar = values.sumOf { it.sugar },
            salt = values.sumOf { it.salt }
        )
    }

    fun averageNutritionValues(values: List<NutritionValues>): NutritionValues {
        if (values.isEmpty()) return NutritionValues(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

        val sum = sumNutritionValues(values)
        val count = values.size

        return NutritionValues(
            calories = sum.calories / count,
            protein = sum.protein / count,
            carbs = sum.carbs / count,
            fat = sum.fat / count,
            fiber = sum.fiber / count,
            sugar = sum.sugar / count,
            salt = sum.salt / count
        )
    }
}