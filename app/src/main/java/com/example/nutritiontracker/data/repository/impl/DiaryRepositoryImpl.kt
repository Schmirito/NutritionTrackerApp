package com.example.nutritiontracker.data.repository.impl

import com.example.nutritiontracker.data.database.dao.DiaryDao
import com.example.nutritiontracker.data.database.entities.DiaryEntry
import com.example.nutritiontracker.data.repository.DiaryRepository
import com.example.nutritiontracker.utils.DateUtils
import kotlinx.coroutines.flow.Flow

class DiaryRepositoryImpl(
    private val diaryDao: DiaryDao
) : DiaryRepository {

    override fun getEntriesForDateRange(startDate: Long, endDate: Long): Flow<List<DiaryEntry>> {
        return diaryDao.getEntriesForDateRange(startDate, endDate)
    }

    override suspend fun insertEntry(entry: DiaryEntry) {
        diaryDao.insertEntry(entry)
    }

    override suspend fun updateEntry(entry: DiaryEntry) {
        diaryDao.updateEntry(entry)
    }

    override suspend fun deleteEntry(entry: DiaryEntry) {
        diaryDao.deleteEntry(entry)
    }

    override fun getEntriesForDate(date: Long): Flow<List<DiaryEntry>> {
        val startOfDay = DateUtils.getStartOfDay(date)
        val endOfDay = DateUtils.getEndOfDay(date) + 1
        return getEntriesForDateRange(startOfDay, endOfDay)
    }
}