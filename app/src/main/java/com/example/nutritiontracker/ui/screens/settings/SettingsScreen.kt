package com.example.nutritiontracker.ui.screens.settings

import MainViewModel
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    onThemeChange: (ThemeMode) -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showBackupDialog by remember { mutableStateOf(false) }
    var backupMessage by remember { mutableStateOf<String?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportTypeDialog by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                val success = viewModel.importNutritionData(context, it)
                backupMessage = if (success) {
                    "Import erfolgreich!"
                } else {
                    "Import fehlgeschlagen"
                }
                showBackupDialog = true
            }
        }
    }

    val diaryImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                val success = viewModel.importDiaryData(context, it)
                backupMessage = if (success) {
                    "Tagebuch-Import erfolgreich!"
                } else {
                    "Tagebuch-Import fehlgeschlagen"
                }
                showBackupDialog = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                    text = "Erstellen Sie regelmäßig Backups Ihrer kompletten Datenbank.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        scope.launch {
                            val backupPath = BackupManager.createBackup(context)
                            backupMessage = if (backupPath != null) {
                                "Backup erfolgreich erstellt!"
                            } else {
                                "Backup fehlgeschlagen"
                            }
                            showBackupDialog = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Backup erstellen")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Export/Import Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Daten teilen",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Exportieren Sie Ihre Zutaten und Rezepte, um sie mit anderen zu teilen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export")
                    }

                    OutlinedButton(
                        onClick = { showImportTypeDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weitere Funktionen
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
                    text = "Demnächst verfügbar:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Column(
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("• Wochenplaner", style = MaterialTheme.typography.bodyMedium)
                    Text("• Nährwert-Ziele", style = MaterialTheme.typography.bodyMedium)
                    Text("• Benachrichtigungen", style = MaterialTheme.typography.bodyMedium)
                    Text("• Cloud-Synchronisation", style = MaterialTheme.typography.bodyMedium)
                    Text("• Barcode-Scanner", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Platz am Ende für besseres Scrollen
        Spacer(modifier = Modifier.height(32.dp))
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Daten exportieren") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Was möchten Sie exportieren?")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val uri = viewModel.exportNutritionData(context)
                                    if (uri != null) {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/json"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Zutaten & Rezepte teilen"))
                                    }
                                    showExportDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Zutaten & Rezepte")
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    val uri = viewModel.exportDiaryData(context)
                                    if (uri != null) {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/json"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Tagebuch teilen"))
                                    }
                                    showExportDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Tagebuch")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Import Type Dialog
    if (showImportTypeDialog) {
        AlertDialog(
            onDismissRequest = { showImportTypeDialog = false },
            title = { Text("Daten importieren") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Was möchten Sie importieren?")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                importLauncher.launch("application/json")
                                showImportTypeDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Zutaten & Rezepte")
                        }

                        Button(
                            onClick = {
                                diaryImportLauncher.launch("application/json")
                                showImportTypeDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Tagebuch")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImportTypeDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Backup/Import Result Dialog
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