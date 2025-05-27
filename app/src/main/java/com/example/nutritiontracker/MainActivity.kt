package com.example.nutritiontracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.nutritiontracker.data.preferences.ThemeMode
import com.example.nutritiontracker.data.preferences.ThemePreferences
import com.example.nutritiontracker.di.AppModule
import com.example.nutritiontracker.ui.theme.NutritionAppTheme
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "onCreate - Schritt 3: Vollständige App")

        try {
            // Dependency Injection Setup
            val themePreferences = ThemePreferences(this)
            val viewModelFactory = AppModule.getViewModelFactory(this)

            Log.d("MainActivity", "All dependencies initialized")

            setContent {
                var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }

                LaunchedEffect(Unit) {
                    try {
                        themePreferences.getThemeMode().collect { mode ->
                            themeMode = mode
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error collecting theme mode", e)
                    }
                }

                val isDarkTheme = when (themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }

                NutritionAppTheme(darkTheme = isDarkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // KEIN TRY-CATCH um Composables!
                        NutritionApp(
                            viewModelFactory = viewModelFactory,
                            themeMode = themeMode,
                            onThemeChange = { newMode ->
                                themeMode = newMode
                                runBlocking {
                                    try {
                                        themePreferences.setThemeMode(newMode)
                                    } catch (e: Exception) {
                                        Log.e("MainActivity", "Error setting theme mode", e)
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Log.d("MainActivity", "Full app launched successfully")

        } catch (e: Exception) {
            Log.e("MainActivity", "Fatal error in onCreate", e)
            throw e
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy called")
        if (isFinishing) {
            try {
                AppModule.clearInstances()
                Log.d("MainActivity", "AppModule cleared")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error clearing AppModule", e)
            }
        }
    }
}