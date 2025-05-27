package com.example.nutritiontracker.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nutritiontracker.data.models.Category

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val calories: Double, // pro 100g/100ml oder pro Stück
    val protein: Double, // in g pro 100g/100ml oder pro Stück
    val carbs: Double, // in g pro 100g/100ml oder pro Stück
    val fat: Double, // in g pro 100g/100ml oder pro Stück
    val fiber: Double = 0.0, // in g pro 100g/100ml oder pro Stück
    val sugar: Double = 0.0, // in g pro 100g/100ml oder pro Stück
    val salt: Double = 0.0, // in g pro 100g/100ml oder pro Stück
    val imagePath: String? = null,
    val unit: IngredientUnit = IngredientUnit.GRAM,
    val categories: List<Category> = emptyList()
)