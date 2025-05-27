package com.example.nutritiontracker.data.repository.impl

import com.example.nutritiontracker.data.database.dao.ShoppingListDao
import com.example.nutritiontracker.data.database.entities.ShoppingListItem
import com.example.nutritiontracker.data.repository.ShoppingListRepository
import kotlinx.coroutines.flow.Flow

class ShoppingListRepositoryImpl(
    private val shoppingListDao: ShoppingListDao
) : ShoppingListRepository {

    override fun getAllItems(): Flow<List<ShoppingListItem>> {
        return shoppingListDao.getAllItems()
    }

    override suspend fun insertItem(item: ShoppingListItem): Long {
        return shoppingListDao.insertItem(item)
    }

    override suspend fun updateItem(item: ShoppingListItem) {
        shoppingListDao.updateItem(item)
    }

    override suspend fun deleteItem(item: ShoppingListItem) {
        return shoppingListDao.deleteItem(item)
    }

    override suspend fun deleteCheckedItems() {
        shoppingListDao.deleteCheckedItems()
    }

    override suspend fun updateCheckedStatus(id: Long, checked: Boolean) {
        shoppingListDao.updateCheckedStatus(id, checked)
    }
}