package com.example.nutritiontracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nutritiontracker.ui.navigation.NavigationItem

@Composable
fun NavigationDrawer(
    onNavigate: (String) -> Unit,
    onClose: () -> Unit
) {
    ModalDrawerSheet {
        Spacer(modifier = Modifier.height(24.dp))

        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Ernährungs-Tracker",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Divider(modifier = Modifier.padding(horizontal = 28.dp))
        Spacer(modifier = Modifier.height(8.dp))

        // Navigation Items
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Übersicht") },
            selected = false,
            onClick = { onNavigate(NavigationItem.Overview.route) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
            label = { Text("Zutaten") },
            selected = false,
            onClick = { onNavigate(NavigationItem.Ingredients.route) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Restaurant, contentDescription = null) },
            label = { Text("Rezepte") },
            selected = false,
            onClick = { onNavigate(NavigationItem.Recipes.route) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            label = { Text("Tagebuch") },
            selected = false,
            onClick = { onNavigate(NavigationItem.Diary.route) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Spacer(modifier = Modifier.height(8.dp))
        Divider(modifier = Modifier.padding(horizontal = 28.dp))
        Spacer(modifier = Modifier.height(8.dp))

        // Settings
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Einstellungen") },
            selected = false,
            onClick = { onNavigate("settings") },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Footer
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            label = { Text("Über") },
            selected = false,
            onClick = { /* TODO: Über-Dialog */ },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}