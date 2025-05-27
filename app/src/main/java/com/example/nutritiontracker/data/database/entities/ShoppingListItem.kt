package com.example.nutritiontracker.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_list_items")
data class ShoppingListItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: String = "",
    val isChecked: Boolean = false,
    val ingredientId: Long? = null,
    val recipeId: Long? = null,
    val isManualEntry: Boolean = false,
    val createdDate: Long = System.currentTimeMillis()
)