package com.example.nutritiontracker.data.models

data class WeeklyStats(
    val avgCalories: Double = 0.0,
    val avgProtein: Double = 0.0,
    val avgCarbs: Double = 0.0,
    val avgFat: Double = 0.0,
    val avgFiber: Double = 0.0,
    val avgSugar: Double = 0.0,
    val avgSalt: Double = 0.0,
    val totalDays: Int = 0
)