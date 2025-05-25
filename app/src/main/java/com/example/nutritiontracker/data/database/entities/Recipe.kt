package com.example.nutritiontracker.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nutritiontracker.data.models.Category

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val servings: Int = 1,
    val createdDate: Long = System.currentTimeMillis(),
    val imagePath: String? = null,
    val categories: List<Category> = emptyList() // NEU: Kategorien
)