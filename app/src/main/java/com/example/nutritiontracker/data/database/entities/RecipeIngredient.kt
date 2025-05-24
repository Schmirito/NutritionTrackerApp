package com.example.nutritiontracker.data.database.entities


import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "recipe_ingredients",
    primaryKeys = ["recipeId", "ingredientId"],
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Ingredient::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RecipeIngredient(
    val recipeId: Long,
    val ingredientId: Long,
    val amount: Double // in grams
)