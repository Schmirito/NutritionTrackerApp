package com.example.nutritiontracker.data.models

data class ManualEntry(
    val name: String,
    val calories: Double,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0
)