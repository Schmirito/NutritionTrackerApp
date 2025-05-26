package com.example.nutritiontracker.utils

import com.example.nutritiontracker.data.database.entities.Ingredient
import com.example.nutritiontracker.data.models.Category

object CategoryUtils {

    fun calculateAutomaticCategories(ingredients: List<Ingredient>): List<Category> {
        if (ingredients.isEmpty()) return emptyList()

        val categories = mutableSetOf<Category>()

        // Sammle alle Kategorien von allen Zutaten
        val allIngredientCategories = ingredients.flatMap { it.categories }.toSet()

        // Füge Lebensmittel-Kategorien hinzu, wenn mindestens eine Zutat sie hat
        val foodCategories = listOf(
            Category.MEAT, Category.FISH, Category.DAIRY, Category.CHEESE,
            Category.EGGS, Category.VEGETABLES, Category.FRUITS, Category.GRAINS,
            Category.NUTS_SEEDS, Category.LEGUMES, Category.SWEETS, Category.BEVERAGES
        )

        foodCategories.forEach { category ->
            if (allIngredientCategories.contains(category)) {
                categories.add(category)
            }
        }

        // Prüfe Ernährungsform-Kategorien (diese werden nur hinzugefügt, wenn KEINE konfliktierenden Zutaten vorhanden sind)
        val dietConflicts = getDietCategoryConflicts()

        // Prüfe ob das Rezept vegan sein könnte
        val hasNonVeganIngredient = ingredients.any { ingredient ->
            ingredient.categories.any { it in dietConflicts[Category.VEGAN]!! }
        }
        if (!hasNonVeganIngredient) {
            // Nur hinzufügen wenn alle Zutaten explizit vegan sind
            if (ingredients.all { ingredient ->
                    Category.VEGAN in ingredient.categories ||
                            ingredient.categories.none { it in dietConflicts[Category.VEGAN]!! }
                }) {
                categories.add(Category.VEGAN)
            }
        }

        // Prüfe ob das Rezept vegetarisch sein könnte
        val hasNonVegetarianIngredient = ingredients.any { ingredient ->
            ingredient.categories.any { it in dietConflicts[Category.VEGETARIAN]!! }
        }
        if (!hasNonVegetarianIngredient) {
            // Nur hinzufügen wenn alle Zutaten explizit vegetarisch sind
            if (ingredients.all { ingredient ->
                    Category.VEGETARIAN in ingredient.categories ||
                            ingredient.categories.none { it in dietConflicts[Category.VEGETARIAN]!! }
                }) {
                categories.add(Category.VEGETARIAN)
            }
        }

        // Glutenfrei und Laktosefrei
        if (!ingredients.any { it.categories.contains(Category.GRAINS) }) {
            if (ingredients.all { Category.GLUTEN_FREE in it.categories || !it.categories.contains(Category.GRAINS) }) {
                categories.add(Category.GLUTEN_FREE)
            }
        }

        if (!ingredients.any { it.categories.any { cat -> cat in listOf(Category.DAIRY, Category.CHEESE) } }) {
            if (ingredients.all { Category.LACTOSE_FREE in it.categories ||
                        !it.categories.any { cat -> cat in listOf(Category.DAIRY, Category.CHEESE) } }) {
                categories.add(Category.LACTOSE_FREE)
            }
        }

        return categories.toList().sortedBy { it.ordinal }
    }

    fun mergeCategories(automatic: List<Category>, manual: List<Category>): List<Category> {
        val result = mutableSetOf<Category>()
        result.addAll(automatic)
        result.addAll(manual)

        // Entferne widersprüchliche Ernährungsform-Kategorien
        val conflicts = getDietCategoryConflicts()

        conflicts.forEach { (dietCategory, conflictingCategories) ->
            // Wenn eine konflikthafte Kategorie vorhanden ist, entferne die Diät-Kategorie
            if (result.any { it in conflictingCategories }) {
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