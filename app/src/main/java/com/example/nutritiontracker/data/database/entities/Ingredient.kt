package com.example.nutritiontracker.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "", // NEU: Beschreibungsfeld
    val calories: Double, // pro 100g
    val protein: Double, // in g pro 100g
    val carbs: Double, // in g pro 100g
    val fat: Double, // in g pro 100g
    val fiber: Double = 0.0, // in g pro 100g
    val sugar: Double = 0.0, // in g pro 100g
    val salt: Double = 0.0, // in g pro 100g
    val imagePath: String? = null, // NEU: Pfad zum Bild
    val unit: IngredientUnit = IngredientUnit.GRAM // NEU: Einheit (Gramm oder Stück)
)

enum class IngredientUnit {
    GRAM,
    PIECE
}