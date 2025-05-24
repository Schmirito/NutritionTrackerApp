package com.example.nutritiontracker


import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nutritiontracker.ui.components.BottomNavigationBar
import com.example.nutritiontracker.ui.navigation.NavigationItem
import com.example.nutritiontracker.ui.screens.diary.DiaryScreen
import com.example.nutritiontracker.ui.screens.ingredients.IngredientsScreen
import com.example.nutritiontracker.ui.screens.overview.OverviewScreen
import com.example.nutritiontracker.ui.screens.recipes.RecipesScreen
import com.example.nutritiontracker.viewmodel.MainViewModel
import com.example.nutritiontracker.viewmodel.MainViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionApp(viewModelFactory: MainViewModelFactory) {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel(factory = viewModelFactory)

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavigationItem.Overview.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NavigationItem.Overview.route) {
                OverviewScreen(viewModel)
            }
            composable(NavigationItem.Ingredients.route) {
                IngredientsScreen(viewModel)
            }
            composable(NavigationItem.Recipes.route) {
                RecipesScreen(viewModel)
            }
            composable(NavigationItem.Diary.route) {
                DiaryScreen(viewModel)
            }
        }
    }
}