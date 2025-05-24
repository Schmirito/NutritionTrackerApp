package com.example.nutritiontracker.ui.screens.diary

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DateSelector(
    selectedDate: Long,
    onDateChange: (Long) -> Unit
) {
    val dateFormat = SimpleDateFormat("EEEE, dd. MMMM yyyy", Locale.GERMAN)

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val cal = Calendar.getInstance()
                cal.timeInMillis = selectedDate
                cal.add(Calendar.DAY_OF_MONTH, -1)
                onDateChange(cal.timeInMillis)
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Vorheriger Tag")
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when {
                        DateUtils.isToday(selectedDate) -> "Heute"
                        DateUtils.isYesterday(selectedDate) -> "Gestern"
                        else -> ""
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = dateFormat.format(Date(selectedDate)),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(
                onClick = {
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = selectedDate
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                    onDateChange(cal.timeInMillis)
                },
                enabled = !DateUtils.isToday(selectedDate)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Nächster Tag")
            }
        }
    }
}