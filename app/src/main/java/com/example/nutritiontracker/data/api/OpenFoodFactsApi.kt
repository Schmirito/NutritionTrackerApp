package com.example.nutritiontracker.data.api

import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApi {
    @GET("api/v2/product/{barcode}")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): OpenFoodFactsResponse
}

data class OpenFoodFactsResponse(
    val status: Int,
    val product: Product?
)

data class Product(
    val product_name: String?,
    val brands: String?,
    val image_url: String?,
    val nutriments: Nutriments?,
    val quantity: String?
)

data class Nutriments(
    val energy_kcal_100g: Double?,
    val proteins_100g: Double?,
    val carbohydrates_100g: Double?,
    val fat_100g: Double?,
    val fiber_100g: Double?,
    val sugars_100g: Double?,
    val salt_100g: Double?
)