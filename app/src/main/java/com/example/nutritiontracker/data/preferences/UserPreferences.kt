package com.example.nutritiontracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferences(private val context: Context) {
    companion object {
        val DAILY_CALORIE_GOAL = intPreferencesKey("daily_calorie_goal")
        val DAILY_PROTEIN_GOAL = intPreferencesKey("daily_protein_goal")
        val DAILY_CARBS_GOAL = intPreferencesKey("daily_carbs_goal")
        val DAILY_FAT_GOAL = intPreferencesKey("daily_fat_goal")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_TIME = stringPreferencesKey("reminder_time")
    }

    fun getDailyCalorieGoal(): Flow<Int> = context.userDataStore.data
        .map { preferences ->
            preferences[DAILY_CALORIE_GOAL] ?: 2000
        }

    suspend fun setDailyCalorieGoal(goal: Int) {
        context.userDataStore.edit { preferences ->
            preferences[DAILY_CALORIE_GOAL] = goal
        }
    }

    fun getDailyProteinGoal(): Flow<Int> = context.userDataStore.data
        .map { preferences ->
            preferences[DAILY_PROTEIN_GOAL] ?: 50
        }

    suspend fun setDailyProteinGoal(goal: Int) {
        context.userDataStore.edit { preferences ->
            preferences[DAILY_PROTEIN_GOAL] = goal
        }
    }

    fun getDailyCarbsGoal(): Flow<Int> = context.userDataStore.data
        .map { preferences ->
            preferences[DAILY_CARBS_GOAL] ?: 250
        }

    suspend fun setDailyCarbsGoal(goal: Int) {
        context.userDataStore.edit { preferences ->
            preferences[DAILY_CARBS_GOAL] = goal
        }
    }

    fun getDailyFatGoal(): Flow<Int> = context.userDataStore.data
        .map { preferences ->
            preferences[DAILY_FAT_GOAL] ?: 65
        }

    suspend fun setDailyFatGoal(goal: Int) {
        context.userDataStore.edit { preferences ->
            preferences[DAILY_FAT_GOAL] = goal
        }
    }

    fun getReminderEnabled(): Flow<Boolean> = context.userDataStore.data
        .map { preferences ->
            preferences[REMINDER_ENABLED] ?: false
        }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.userDataStore.edit { preferences ->
            preferences[REMINDER_ENABLED] = enabled
        }
    }

    fun getReminderTime(): Flow<String> = context.userDataStore.data
        .map { preferences ->
            preferences[REMINDER_TIME] ?: "12:00"
        }

    suspend fun setReminderTime(time: String) {
        context.userDataStore.edit { preferences ->
            preferences[REMINDER_TIME] = time
        }
    }
}