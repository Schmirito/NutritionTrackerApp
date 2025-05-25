package com.example.nutritiontracker


import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nutritiontracker.data.preferences.ThemeMode
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
fun NutritionApp(
    viewModelFactory: MainViewModelFactory,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel(factory = viewModelFactory)
    var showThemeMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (navController.currentBackStackEntry?.destination?.route) {
                            NavigationItem.Overview.route -> "Ernährungsübersicht"
                            NavigationItem.Ingredients.route -> "Zutaten"
                            NavigationItem.Recipes.route -> "Rezepte"
                            NavigationItem.Diary.route -> "Tagebuch"
                            else -> "Ernährungs-Tagebuch"
                        }
                    )
                },
                actions = {
                    IconButton(onClick = { showThemeMenu = true }) {
                        Icon(
                            imageVector = when (themeMode) {
                                ThemeMode.LIGHT -> Icons.Default.Brightness7
                                ThemeMode.DARK -> Icons.Default.Brightness4
                                ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                            },
                            contentDescription = "Theme wechseln"
                        )
                    }

                    DropdownMenu(
                        expanded = showThemeMenu,
                        onDismissRequest = { showThemeMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Hell") },
                            onClick = {
                                onThemeChange(ThemeMode.LIGHT)
                                showThemeMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Brightness7, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Dunkel") },
                            onClick = {
                                onThemeChange(ThemeMode.DARK)
                                showThemeMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Brightness4, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("System") },
                            onClick = {
                                onThemeChange(ThemeMode.SYSTEM)
                                showThemeMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.BrightnessAuto, contentDescription = null)
                            }
                        )
                    }
                }
            )
        },
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