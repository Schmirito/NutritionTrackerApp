package com.example.nutritiontracker.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE ingredients ADD COLUMN description TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE ingredients ADD COLUMN imagePath TEXT")
            database.execSQL("ALTER TABLE ingredients ADD COLUMN unit TEXT NOT NULL DEFAULT 'GRAM'")
            database.execSQL("ALTER TABLE recipes ADD COLUMN imagePath TEXT")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE ingredients ADD COLUMN categories TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE recipes ADD COLUMN categories TEXT NOT NULL DEFAULT ''")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Füge Felder für manuelle Einträge hinzu
            database.execSQL("ALTER TABLE diary_entries ADD COLUMN isManualEntry INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE diary_entries ADD COLUMN manualEntryName TEXT")
            database.execSQL("ALTER TABLE diary_entries ADD COLUMN manualEntryCalories REAL")
            database.execSQL("ALTER TABLE diary_entries ADD COLUMN manualEntryProtein REAL")
            database.execSQL("ALTER TABLE diary_entries ADD COLUMN manualEntryCarbs REAL")
            database.execSQL("ALTER TABLE diary_entries ADD COLUMN manualEntryFat REAL")
        }
    }
}