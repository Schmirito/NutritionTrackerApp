package com.example.nutritiontracker

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nutritiontracker.data.preferences.ThemeMode
import com.example.nutritiontracker.ui.components.BottomNavigationBar
import com.example.nutritiontracker.ui.components.NavigationDrawer
import com.example.nutritiontracker.ui.navigation.NavigationItem
import com.example.nutritiontracker.ui.screens.diary.DiaryScreen
import com.example.nutritiontracker.ui.screens.ingredients.IngredientsScreen
import com.example.nutritiontracker.ui.screens.overview.OverviewScreen
import com.example.nutritiontracker.ui.screens.recipes.RecipesScreen
import com.example.nutritiontracker.ui.screens.settings.SettingsScreen
import com.example.nutritiontracker.viewmodel.MainViewModel
import com.example.nutritiontracker.viewmodel.MainViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionApp(
    viewModelFactory: MainViewModelFactory,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel(factory = viewModelFactory)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Beobachte die aktuelle Route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawer(
                onNavigate = { route ->
                    scope.launch {
                        drawerState.close()
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onClose = {
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentRoute) {
                                NavigationItem.Overview.route -> "Ernährungsübersicht"
                                NavigationItem.Ingredients.route -> "Zutaten"
                                NavigationItem.Recipes.route -> "Rezepte"
                                NavigationItem.Diary.route -> "Tagebuch"
                                "settings" -> "Einstellungen"
                                else -> "Ernährungs-Tagebuch"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menü öffnen")
                        }
                    }
                )
            },
            bottomBar = {
                // Zeige BottomBar nur bei Hauptscreens
                if (currentRoute in listOf(
                        NavigationItem.Overview.route,
                        NavigationItem.Ingredients.route,
                        NavigationItem.Recipes.route,
                        NavigationItem.Diary.route
                    )) {
                    BottomNavigationBar(navController)
                }
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
                composable("settings") {
                    SettingsScreen(
                        currentThemeMode = themeMode,
                        onThemeChange = onThemeChange,
                        //viewModel = viewModel
                    )
                }
            }
        }
    }
}