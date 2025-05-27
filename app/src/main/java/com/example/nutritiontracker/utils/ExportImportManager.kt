package com.example.nutritiontracker.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
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

    private const val TAG = "ExportImportManager"

    // Datenklassen für Export
    data class RecipeExport(
        val recipe: Recipe,
        val ingredients: List<RecipeIngredient>
    )

    data class NutritionExport(
        val version: Int = Constants.Export.EXPORT_VERSION,
        val exportDate: Long = System.currentTimeMillis(),
        val ingredients: List<Ingredient>,
        val recipes: List<RecipeExport>
    )

    data class DiaryExport(
        val version: Int = Constants.Export.EXPORT_VERSION,
        val exportDate: Long = System.currentTimeMillis(),
        val entries: List<DiaryEntry>
    )

    // Export Zutaten und Rezepte - EINFACHE VERSION (für Kompatibilität)
    suspend fun exportNutritionData(
        context: Context,
        ingredients: List<Ingredient>,
        recipes: List<Recipe>,
        recipeIngredients: Map<Long, List<RecipeIngredient>>
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(context.getExternalFilesDir(null), Constants.Export.EXPORT_DIRECTORY)
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val timestamp = SimpleDateFormat(Constants.DateTime.DATE_FORMAT_EXPORT, Locale.getDefault()).format(Date())
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

            // Verwende FileProvider für sicheres Teilen
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}${Constants.Export.FILE_PROVIDER_AUTHORITY}",
                file
            )

            Log.d(TAG, "Nutrition export created: ${file.absolutePath}")
            uri
        } catch (e: Exception) {
            Log.e(TAG, "Error creating nutrition export", e)
            null
        }
    }

    // Export Tagebuch - EINFACHE VERSION
    suspend fun exportDiaryData(
        context: Context,
        entries: List<DiaryEntry>
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(context.getExternalFilesDir(null), Constants.Export.EXPORT_DIRECTORY)
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }

            val timestamp = SimpleDateFormat(Constants.DateTime.DATE_FORMAT_EXPORT, Locale.getDefault()).format(Date())
            val fileName = "diary_export_$timestamp.json"
            val file = File(exportDir, fileName)
            val export = DiaryExport(entries = entries)

            file.writeText(gson.toJson(export))

            // Verwende FileProvider für sicheres Teilen
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}${Constants.Export.FILE_PROVIDER_AUTHORITY}",
                file
            )

            Log.d(TAG, "Diary export created: ${file.absolutePath}")
            uri
        } catch (e: Exception) {
            Log.e(TAG, "Error creating diary export", e)
            null
        }
    }

    // Import Zutaten und Rezepte - EINFACHE VERSION
    suspend fun importNutritionData(
        context: Context,
        uri: Uri
    ): NutritionExport? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext null

            val reader = BufferedReader(InputStreamReader(inputStream))
            val content = reader.use { it.readText() }

            val export = gson.fromJson(content, NutritionExport::class.java)
                ?: return@withContext null

            // Validierung der Daten
            if (export.version > Constants.Export.EXPORT_VERSION) {
                Log.w(TAG, "Export version ${export.version} is newer than supported version")
            }

            Log.d(TAG, "Nutrition import successful: ${export.ingredients.size} ingredients, ${export.recipes.size} recipes")
            export
        } catch (e: Exception) {
            Log.e(TAG, "Error importing nutrition data", e)
            null
        }
    }

    // Import Tagebuch - EINFACHE VERSION
    suspend fun importDiaryData(
        context: Context,
        uri: Uri
    ): DiaryExport? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext null

            val reader = BufferedReader(InputStreamReader(inputStream))
            val content = reader.use { it.readText() }

            val export = gson.fromJson(content, DiaryExport::class.java)
                ?: return@withContext null

            // Validierung der Daten
            if (export.version > Constants.Export.EXPORT_VERSION) {
                Log.w(TAG, "Export version ${export.version} is newer than supported version")
            }

            Log.d(TAG, "Diary import successful: ${export.entries.size} entries")
            export
        } catch (e: Exception) {
            Log.e(TAG, "Error importing diary data", e)
            null
        }
    }
}