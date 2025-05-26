package com.example.nutritiontracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Overview : NavigationItem("overview", "Übersicht", Icons.Default.Home)
    object Ingredients : NavigationItem("ingredients", "Zutaten", Icons.Default.ShoppingCart)
    object Recipes : NavigationItem("recipes", "Rezepte", Icons.Default.Restaurant)
    object Diary : NavigationItem("diary", "Tagebuch", Icons.Default.DateRange)
}