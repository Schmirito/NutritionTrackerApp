package com.example.nutritiontracker.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.nutritiontracker.data.database.NutritionDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object BackupManager {

    suspend fun createBackup(context: Context): String? = withContext(Dispatchers.IO) {
        try {
            // Schließe die Datenbank
            NutritionDatabase.getDatabase(context).close()

            // Datenbankpfad
            val dbFile = context.getDatabasePath("nutrition_database")

            if (!dbFile.exists()) {
                return@withContext null
            }

            // EXTERNAL STORAGE - überlebt App-Deinstallation!
            val backupDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "NutritionTracker_Backups"
            )

            // Falls Documents nicht verfügbar, verwende Downloads
            val finalBackupDir = if (backupDir.exists() || backupDir.mkdirs()) {
                backupDir
            } else {
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "NutritionTracker_Backups").apply {
                    mkdirs()
                }
            }

            // Backup-Dateiname mit Zeitstempel
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val backupFile = File(finalBackupDir, "nutrition_backup_$timestamp.db")

            // Kopiere Datenbankdatei
            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }

            Log.d("BackupManager", "Backup erstellt: ${backupFile.absolutePath}")
            Log.d("BackupManager", "Backup überlebt App-Deinstallation: JA")

            return@withContext backupFile.absolutePath

        } catch (e: Exception) {
            Log.e("BackupManager", "Fehler beim Backup", e)
            return@withContext null
        }
    }

    suspend fun restoreBackup(context: Context, backupPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupFile = File(backupPath)
            if (!backupFile.exists()) {
                return@withContext false
            }

            // Schließe die Datenbank
            NutritionDatabase.getDatabase(context).close()

            // Datenbankpfad
            val dbFile = context.getDatabasePath("nutrition_database")

            // Lösche alte Datenbank
            if (dbFile.exists()) {
                dbFile.delete()
            }

            // Kopiere Backup
            FileInputStream(backupFile).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }

            Log.d("BackupManager", "Backup wiederhergestellt von: $backupPath")
            return@withContext true

        } catch (e: Exception) {
            Log.e("BackupManager", "Fehler beim Wiederherstellen", e)
            return@withContext false
        }
    }

    // Hilfsfunktion: Alle verfügbaren Backups finden
    fun getAvailableBackups(): List<File> {
        val backupDirs = listOf(
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "NutritionTracker_Backups"),
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "NutritionTracker_Backups")
        )

        return backupDirs.flatMap { dir ->
            if (dir.exists()) {
                dir.listFiles { file -> file.name.endsWith(".db") }?.toList() ?: emptyList()
            } else {
                emptyList()
            }
        }.sortedByDescending { it.lastModified() }
    }
}