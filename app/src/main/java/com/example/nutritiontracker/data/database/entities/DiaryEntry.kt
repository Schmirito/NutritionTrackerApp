package com.example.nutritiontracker.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nutritiontracker.data.models.EntryType
import com.example.nutritiontracker.data.models.MealType

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long, // Date as timestamp
    val mealType: MealType,
    val entryType: EntryType,
    val ingredientId: Long? = null,
    val recipeId: Long? = null,
    val amount: Double, // in grams or servings

    // Neue Felder für manuelle Einträge
    val isManualEntry: Boolean = false,
    val manualEntryName: String? = null,
    val manualEntryCalories: Double? = null,
    val manualEntryProtein: Double? = null,
    val manualEntryCarbs: Double? = null,
    val manualEntryFat: Double? = null
)