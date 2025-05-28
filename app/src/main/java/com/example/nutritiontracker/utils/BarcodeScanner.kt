package com.example.nutritiontracker.utils

import android.content.Context
import android.util.Log
import com.example.nutritiontracker.data.api.Nutriments
import com.example.nutritiontracker.data.api.OpenFoodFactsApi
import com.example.nutritiontracker.data.api.Product
import com.example.nutritiontracker.data.database.entities.Ingredient
import com.example.nutritiontracker.data.database.entities.IngredientUnit
import com.example.nutritiontracker.data.models.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL

object BarcodeScanner {

    private const val BASE_URL = "https://world.openfoodfacts.org/"
    private const val TAG = "BarcodeScanner"

    private val api: OpenFoodFactsApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodFactsApi::class.java)
    }

    suspend fun scanBarcode(barcode: String): ScanResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Scanning barcode: $barcode")

            val response = api.getProductByBarcode(barcode)

            if (response.status != 1 || response.product == null) {
                Log.w(TAG, "Product not found for barcode: $barcode")
                return@withContext ScanResult.NotFound
            }

            val product = response.product
            val nutriments = product.nutriments

            if (product.product_name.isNullOrBlank()) {
                Log.w(TAG, "Product has no name")
                return@withContext ScanResult.InvalidData("Produkt hat keinen Namen")
            }

            // Bestimme die Einheit basierend auf der Menge
            val unit = when {
                product.quantity?.contains("ml", ignoreCase = true) == true -> IngredientUnit.MILLILITER
                product.quantity?.contains("l", ignoreCase = true) == true -> IngredientUnit.MILLILITER
                else -> IngredientUnit.GRAM
            }

            // Erstelle Ingredient aus den Daten
            val ingredient = Ingredient(
                name = buildString {
                    append(product.product_name)
                    if (!product.brands.isNullOrBlank()) {
                        append(" (${product.brands})")
                    }
                },
                description = product.quantity ?: "",
                calories = nutriments?.energy_kcal_100g ?: 0.0,
                protein = nutriments?.proteins_100g ?: 0.0,
                carbs = nutriments?.carbohydrates_100g ?: 0.0,
                fat = nutriments?.fat_100g ?: 0.0,
                fiber = nutriments?.fiber_100g ?: 0.0,
                sugar = nutriments?.sugars_100g ?: 0.0,
                salt = nutriments?.salt_100g ?: 0.0,
                unit = unit,
                categories = determineCategories(product, nutriments),
                imagePath = null // Wird später gesetzt wenn Bild heruntergeladen wurde
            )

            Log.d(TAG, "Successfully scanned product: ${ingredient.name}")

            return@withContext ScanResult.Success(
                ingredient = ingredient,
                imageUrl = product.image_url
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error scanning barcode", e)
            return@withContext ScanResult.Error(e.message ?: "Unbekannter Fehler")
        }
    }

    private fun determineCategories(product: Product, nutriments: Nutriments?): List<Category> {
        val categories = mutableListOf<Category>()

        // Nährwert-basierte Kategorien
        nutriments?.let { n ->
            // High Protein (> 20g pro 100g)
            if ((n.proteins_100g ?: 0.0) > 20.0) {
                categories.add(Category.HIGH_PROTEIN)
            }

            // Low Carb (< 5g pro 100g)
            if ((n.carbohydrates_100g ?: 0.0) < 5.0) {
                categories.add(Category.LOW_CARB)
            }

            // Low Fat (< 3g pro 100g)
            if ((n.fat_100g ?: 0.0) < 3.0) {
                categories.add(Category.LOW_FAT)
            }

            // High Fiber (> 6g pro 100g)
            if ((n.fiber_100g ?: 0.0) > 6.0) {
                categories.add(Category.HIGH_FIBER)
            }

            // Low Calorie (< 100 kcal pro 100g)
            if ((n.energy_kcal_100g ?: 0.0) < 100.0) {
                categories.add(Category.LOW_CALORIE)
            }
        }

        // Produkt-basierte Kategorien (basierend auf dem Namen)
        val name = product.product_name?.lowercase() ?: ""
        val brand = product.brands?.lowercase() ?: ""
        val combined = "$name $brand"

        when {
            combined.contains("milch") || combined.contains("milk") ||
                    combined.contains("käse") || combined.contains("cheese") ||
                    combined.contains("joghurt") || combined.contains("yogurt") -> {
                categories.add(Category.DAIRY)
            }
            combined.contains("fleisch") || combined.contains("meat") ||
                    combined.contains("wurst") || combined.contains("sausage") -> {
                categories.add(Category.MEAT)
            }
            combined.contains("fisch") || combined.contains("fish") -> {
                categories.add(Category.FISH)
            }
            combined.contains("gemüse") || combined.contains("vegetable") -> {
                categories.add(Category.VEGETABLES)
            }
            combined.contains("obst") || combined.contains("fruit") -> {
                categories.add(Category.FRUITS)
            }
            combined.contains("getränk") || combined.contains("drink") ||
                    combined.contains("saft") || combined.contains("juice") -> {
                categories.add(Category.BEVERAGES)
            }
        }

        return categories
    }

    suspend fun downloadProductImage(context: Context, imageUrl: String?, fileName: String): String? {
        if (imageUrl.isNullOrBlank()) return null

        return withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection()
                connection.connect()

                val inputStream = connection.getInputStream()
                val tempFile = java.io.File.createTempFile("temp_image", ".jpg", context.cacheDir)

                tempFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }

                val uri = android.net.Uri.fromFile(tempFile)
                val savedPath = ImageUtils.saveImageToInternalStorage(context, uri, fileName)

                tempFile.delete()

                savedPath
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading image", e)
                null
            }
        }
    }

    sealed class ScanResult {
        data class Success(
            val ingredient: Ingredient,
            val imageUrl: String?
        ) : ScanResult()

        object NotFound : ScanResult()

        data class InvalidData(val message: String) : ScanResult()

        data class Error(val message: String) : ScanResult()
    }
}