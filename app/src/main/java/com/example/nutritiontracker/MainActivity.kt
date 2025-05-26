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
import com.example.nutritiontracker.data.database.NutritionDatabase
import com.example.nutritiontracker.data.preferences.ThemeMode
import com.example.nutritiontracker.data.preferences.ThemePreferences
import com.example.nutritiontracker.ui.theme.NutritionAppTheme
import com.example.nutritiontracker.viewmodel.MainViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = NutritionDatabase.getDatabase(this)
        val themePreferences = ThemePreferences(this)
        val viewModelFactory = MainViewModelFactory(
            database.ingredientDao(),
            database.recipeDao(),
            database.diaryDao()
        )

        // Prüfe Datenbankversion beim Start (nur für Debugging)
        try {
            val dbVersion = database.openHelper.readableDatabase.version
            Log.d("MainActivity", "Datenbankversion: $dbVersion")
        } catch (e: Exception) {
            Log.e("MainActivity", "Fehler beim Prüfen der DB-Version", e)
        }

        setContent {
            var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }

            LaunchedEffect(Unit) {
                themePreferences.getThemeMode().collect { mode ->
                    themeMode = mode
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
                    NutritionApp(
                        viewModelFactory = viewModelFactory,
                        themeMode = themeMode,
                        onThemeChange = { newMode ->
                            themeMode = newMode
                            runBlocking {
                                themePreferences.setThemeMode(newMode)
                            }
                        }
                    )
                }
            }
        }
    }
}