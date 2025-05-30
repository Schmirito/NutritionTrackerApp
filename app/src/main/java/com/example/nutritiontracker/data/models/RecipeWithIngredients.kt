package com.example.nutritiontracker.data.models


import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.nutritiontracker.data.database.entities.Ingredient
import com.example.nutritiontracker.data.database.entities.Recipe
import com.example.nutritiontracker.data.database.entities.RecipeIngredient

data class RecipeWithIngredients(
    @Embedded val recipe: Recipe,
    @Relation(
        parentColumn = "recipe_id",
        entityColumn = "ingredient_id",
        associateBy = Junction(
            value = RecipeIngredient::class,
            parentColumn = "recipe_id",
            entityColumn = "ingredient_id"
        )
    )
    val ingredients: List<Ingredient>
)