package com.example.nutritiontracker.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.nutritiontracker.data.database.entities.DiaryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Query("""
        SELECT * FROM diary_entries 
        WHERE date >= :startDate AND date < :endDate
        ORDER BY date DESC, mealType ASC
    """)
    fun getEntriesForDateRange(startDate: Long, endDate: Long): Flow<List<DiaryEntry>>

    @Insert
    suspend fun insertEntry(entry: DiaryEntry)

    @Update
    suspend fun updateEntry(entry: DiaryEntry)

    @Delete
    suspend fun deleteEntry(entry: DiaryEntry)
}