package com.example.nutritiontracker.data.database


import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Füge neue Spalten zu ingredients hinzu
            database.execSQL("ALTER TABLE ingredients ADD COLUMN description TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE ingredients ADD COLUMN imagePath TEXT")
            database.execSQL("ALTER TABLE ingredients ADD COLUMN unit TEXT NOT NULL DEFAULT 'GRAM'")

            // Füge neue Spalte zu recipes hinzu
            database.execSQL("ALTER TABLE recipes ADD COLUMN imagePath TEXT")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Füge Kategorien zu ingredients und recipes hinzu
            database.execSQL("ALTER TABLE ingredients ADD COLUMN categories TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE recipes ADD COLUMN categories TEXT NOT NULL DEFAULT ''")
        }
    }
}