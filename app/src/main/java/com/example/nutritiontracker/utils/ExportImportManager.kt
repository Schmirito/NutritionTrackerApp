package com.example.nutritiontracker.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.nutritiontracker.data.database.entities.DiaryEntry
import com.example.nutritiontracker.data.database.entities.Ingredient
import com.example.nutritiontracker.data.database.entities.Recipe
import com.example.nutritiontracker.data.database.entities.RecipeIngredient
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object ExportImportManager {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    // Datenklassen für Export
    data class RecipeExport(
        val recipe: Recipe,
        val ingredients: List<RecipeIngredient>
    )

    data class NutritionExport(
        val version: Int = 1,
        val exportDate: Long = System.currentTimeMillis(),
        val ingredients: List<Ingredient>,
        val recipes: List<RecipeExport>
    )

    data class DiaryExport(
        val version: Int = 1,
        val exportDate: Long = System.currentTimeMillis(),
        val entries: List<DiaryEntry>
    )

    // Export Zutaten und Rezepte
    suspend fun exportNutritionData(
        context: Context,
        ingredients: List<Ingredient>,
        recipes: List<Recipe>,
        recipeIngredients: Map<Long, List<RecipeIngredient>>
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(context.getExternalFilesDir(null), "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "nutrition_export_$timestamp.json"
            val file = File(exportDir, fileName)

            val recipeExports = recipes.map { recipe ->
                RecipeExport(
                    recipe = recipe,
                    ingredients = recipeIngredients[recipe.id] ?: emptyList()
                )
            }

            val export = NutritionExport(
                ingredients = ingredients,
                recipes = recipeExports
            )

            file.writeText(gson.toJson(export))

            Log.d("ExportImport", "Export erstellt: ${file.absolutePath}")
            return@withContext Uri.fromFile(file)

        } catch (e: Exception) {
            Log.e("ExportImport", "Fehler beim Export", e)
            return@withContext null
        }
    }

    // Export Tagebuch
    suspend fun exportDiaryData(
        context: Context,
        entries: List<DiaryEntry>
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(context.getExternalFilesDir(null), "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "diary_export_$timestamp.json"
            val file = File(exportDir, fileName)

            val export = DiaryExport(entries = entries)

            file.writeText(gson.toJson(export))

            Log.d("ExportImport", "Tagebuch-Export erstellt: ${file.absolutePath}")
            return@withContext Uri.fromFile(file)

        } catch (e: Exception) {
            Log.e("ExportImport", "Fehler beim Tagebuch-Export", e)
            return@withContext null
        }
    }

    // Import Zutaten und Rezepte
    suspend fun importNutritionData(
        context: Context,
        uri: Uri
    ): NutritionExport? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val content = reader.use { it.readText() }

            return@withContext gson.fromJson(content, NutritionExport::class.java)

        } catch (e: Exception) {
            Log.e("ExportImport", "Fehler beim Import", e)
            return@withContext null
        }
    }

    // Import Tagebuch
    suspend fun importDiaryData(
        context: Context,
        uri: Uri
    ): DiaryExport? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val content = reader.use { it.readText() }

            return@withContext gson.fromJson(content, DiaryExport::class.java)

        } catch (e: Exception) {
            Log.e("ExportImport", "Fehler beim Tagebuch-Import", e)
            return@withContext null
        }
    }
}