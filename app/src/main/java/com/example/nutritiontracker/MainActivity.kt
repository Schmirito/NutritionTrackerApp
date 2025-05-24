package com.example.nutritiontracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.nutritiontracker.data.database.NutritionDatabase
import com.example.nutritiontracker.ui.theme.NutritionAppTheme
import com.example.nutritiontracker.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = NutritionDatabase.getDatabase(this)
        val viewModelFactory = MainViewModelFactory(
            database.ingredientDao(),
            database.recipeDao(),
            database.diaryDao()
        )

        setContent {
            NutritionAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NutritionApp(viewModelFactory)
                }
            }
        }
    }
}