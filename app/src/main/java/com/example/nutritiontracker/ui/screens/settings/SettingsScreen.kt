package com.example.nutritiontracker.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.data.preferences.ThemeMode
import com.example.nutritiontracker.utils.BackupManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    currentThemeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showBackupDialog by remember { mutableStateOf(false) }
    var backupMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Einstellungen",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Theme Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Erscheinungsbild",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ThemeOption(
                    title = "Hell",
                    description = "Helles Design verwenden",
                    icon = Icons.Default.LightMode,
                    selected = currentThemeMode == ThemeMode.LIGHT,
                    onClick = { onThemeChange(ThemeMode.LIGHT) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ThemeOption(
                    title = "Dunkel",
                    description = "Dunkles Design verwenden",
                    icon = Icons.Default.DarkMode,
                    selected = currentThemeMode == ThemeMode.DARK,
                    onClick = { onThemeChange(ThemeMode.DARK) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ThemeOption(
                    title = "System",
                    description = "Systemeinstellung folgen",
                    icon = Icons.Default.Brightness6,
                    selected = currentThemeMode == ThemeMode.SYSTEM,
                    onClick = { onThemeChange(ThemeMode.SYSTEM) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Backup Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Datensicherung",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Erstellen Sie regelmäßig Backups Ihrer Daten, um sie bei Bedarf wiederherstellen zu können.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                val backupPath = BackupManager.createBackup(context)
                                backupMessage = if (backupPath != null) {
                                    "Backup erfolgreich erstellt!\n\nGespeichert unter:\n$backupPath"
                                } else {
                                    "Backup fehlgeschlagen. Bitte versuchen Sie es erneut."
                                }
                                showBackupDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Backup erstellen")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Backup-Verzeichnis: Android/data/com.example.nutritiontracker/files/Documents/NutritionBackups",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weitere Einstellungen
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Weitere Funktionen",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Demnächst verfügbar:\n• Backup wiederherstellen\n• Einkaufsliste\n• Export/Import von Daten\n• Benachrichtigungen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Backup Dialog
    if (showBackupDialog && backupMessage != null) {
        AlertDialog(
            onDismissRequest = {
                showBackupDialog = false
                backupMessage = null
            },
            title = {
                Text(
                    text = if (backupMessage!!.contains("erfolgreich")) "Erfolg" else "Fehler"
                )
            },
            text = { Text(backupMessage!!) },
            confirmButton = {
                TextButton(onClick = {
                    showBackupDialog = false
                    backupMessage = null
                }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun ThemeOption(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}