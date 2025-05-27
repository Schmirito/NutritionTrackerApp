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

            // Erstelle Einkaufsliste Tabelle
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS shopping_list_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    amount TEXT NOT NULL DEFAULT '',
                    isChecked INTEGER NOT NULL DEFAULT 0,
                    ingredientId INTEGER,
                    recipeId INTEGER,
                    isManualEntry INTEGER NOT NULL DEFAULT 0,
                    createdDate INTEGER NOT NULL
                )
            """)
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Prüfe ob die Felder bereits existieren (für Sicherheit)
            try {
                database.execSQL("SELECT isManualEntry FROM diary_entries LIMIT 1")
                // Felder existieren bereits - nichts zu tun
            } catch (e: Exception) {
                // Felder existieren nicht - füge sie hinzu
                database.execSQL("ALTER TABLE diary_entries ADD COLUMN isManualEntry INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE diary_entries ADD COLUMN manualEntryName TEXT")
                database.execSQL("ALTER TABLE diary_entries ADD COLUMN manualEntryCalories REAL")
                database.execSQL("ALTER TABLE diary_entries ADD COLUMN manualEntryProtein REAL")
                database.execSQL("ALTER TABLE diary_entries ADD COLUMN manualEntryCarbs REAL")
                database.execSQL("ALTER TABLE diary_entries ADD COLUMN manualEntryFat REAL")
            }
        }
    }

    // NEU: Migration für ML-Einheit (bestehende GRAM-Einträge bleiben unverändert)
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Keine Schema-Änderung nötig - MILLILITER wird nur als neuer Enum-Wert hinzugefügt
            // Bestehende Daten mit unit='GRAM' bleiben gültig
            // Neue Einträge können unit='MILLILITER' verwenden
        }
    }

    // Zentrale Liste aller Migrationen
    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6  // NEU!
    )
}