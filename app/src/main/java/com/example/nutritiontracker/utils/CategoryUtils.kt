package com.example.nutritiontracker.utils

import com.example.nutritiontracker.data.database.entities.Ingredient
import com.example.nutritiontracker.data.models.Category

object CategoryUtils {

    fun calculateAutomaticCategories(ingredients: List<Ingredient>): List<Category> {
        if (ingredients.isEmpty()) return emptyList()

        val categories = mutableSetOf<Category>()

        // Nährwert-Kategorien (diese werden nur hinzugefügt, wenn ALLE Zutaten sie haben)
        val nutritionCategories = listOf(
            Category.HIGH_PROTEIN, Category.LOW_CARB, Category.LOW_FAT,
            Category.HIGH_FIBER, Category.LOW_CALORIE
        )

        nutritionCategories.forEach { category ->
            if (ingredients.all { category in it.categories }) {
                categories.add(category)
            }
        }

        // Ernährungsform-Kategorien (diese werden entfernt, wenn EINE Zutat sie nicht hat)
        val dietCategories = getDietCategoryConflicts()

        dietCategories.forEach { (dietCategory, excludedCategories) ->
            val hasExcludedIngredient = ingredients.any { ingredient ->
                ingredient.categories.any { it in excludedCategories }
            }
            if (!hasExcludedIngredient) {
                categories.add(dietCategory)
            }
        }

        return categories.toList().sortedBy { it.ordinal }
    }

    fun mergeCategories(automatic: List<Category>, manual: List<Category>): List<Category> {
        val result = mutableSetOf<Category>()
        result.addAll(automatic)
        result.addAll(manual)

        // Entferne widersprüchliche Kategorien
        val conflicts = getDietCategoryConflicts()

        conflicts.forEach { (dietCategory, conflictingCategories) ->
            if (result.contains(dietCategory) && result.any { it in conflictingCategories }) {
                result.remove(dietCategory)
            }
        }

        return result.toList().sortedBy { it.ordinal }
    }

    fun getGroupedCategories(): Map<String, List<Category>> {
        return mapOf(
            "Nährwerte" to listOf(
                Category.HIGH_PROTEIN, Category.LOW_CARB, Category.LOW_FAT,
                Category.HIGH_FIBER, Category.LOW_CALORIE
            ),
            "Ernährungsform" to listOf(
                Category.VEGAN, Category.VEGETARIAN, Category.GLUTEN_FREE,
                Category.LACTOSE_FREE, Category.KETO, Category.PALEO
            ),
            "Lebensmittel" to listOf(
                Category.MEAT, Category.FISH, Category.DAIRY, Category.CHEESE,
                Category.EGGS, Category.VEGETABLES, Category.FRUITS, Category.GRAINS,
                Category.NUTS_SEEDS, Category.LEGUMES, Category.SWEETS, Category.BEVERAGES
            ),
            "Mahlzeiten" to listOf(
                Category.BREAKFAST, Category.LUNCH, Category.DINNER,
                Category.SNACK, Category.DESSERT
            ),
            "Zubereitung" to listOf(
                Category.QUICK, Category.EASY, Category.MEAL_PREP
            )
        )
    }

    fun getGroupedCategoriesForIngredients(): Map<String, List<Category>> {
        val allCategories = getGroupedCategories()
        // Entferne "Zubereitung" für Zutaten
        return allCategories.filterKeys { it != "Zubereitung" }
    }

    private fun getDietCategoryConflicts(): Map<Category, List<Category>> {
        return mapOf(
            Category.VEGAN to listOf(Category.MEAT, Category.FISH, Category.DAIRY, Category.CHEESE, Category.EGGS),
            Category.VEGETARIAN to listOf(Category.MEAT, Category.FISH),
            Category.GLUTEN_FREE to listOf(Category.GRAINS),
            Category.LACTOSE_FREE to listOf(Category.DAIRY, Category.CHEESE)
        )
    }
}