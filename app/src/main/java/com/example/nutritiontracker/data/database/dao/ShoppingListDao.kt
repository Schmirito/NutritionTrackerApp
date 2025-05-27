package com.example.nutritiontracker.data.database.dao

import androidx.room.*
import com.example.nutritiontracker.data.database.entities.ShoppingListItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_list_items ORDER BY isChecked ASC, createdDate DESC")
    fun getAllItems(): Flow<List<ShoppingListItem>>

    @Insert
    suspend fun insertItem(item: ShoppingListItem): Long

    @Update
    suspend fun updateItem(item: ShoppingListItem)

    @Delete
    suspend fun deleteItem(item: ShoppingListItem)

    @Query("DELETE FROM shopping_list_items WHERE isChecked = 1")
    suspend fun deleteCheckedItems()

    @Query("UPDATE shopping_list_items SET isChecked = :checked WHERE id = :id")
    suspend fun updateCheckedStatus(id: Long, checked: Boolean)
}