package com.fitpulse.app.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fitpulse.app.ui.components.*
import com.fitpulse.app.ui.navigation.*

data class NotifItem(val label: String, val icon: ImageVector, val hasTimePicker: Boolean = true, val alwaysOn: Boolean = false, val defaultTime: String = "8:00 AM")

@Composable
fun NotificationSettingsScreen(navController: NavController) {
    val mealNotifs = listOf(
        NotifItem("Breakfast", Icons.Default.FreeBreakfast, defaultTime = "8:00 AM"),
        NotifItem("Lunch", Icons.Default.LunchDining, defaultTime = "1:00 PM"),
        NotifItem("Dinner", Icons.Default.DinnerDining, defaultTime = "7:00 PM"),
        NotifItem("Snack", Icons.Default.Cookie, defaultTime = "4:00 PM"),
    )
    val activityNotifs = listOf(
        NotifItem("Exercise", Icons.Default.FitnessCenter, hasTimePicker = false),
        NotifItem("Log Weight", Icons.Default.Scale, defaultTime = "9:00 AM"),
    )
    val updateNotifs = listOf(
        NotifItem("Weekly Summary", Icons.Default.BarChart, hasTimePicker = false),
        NotifItem("Expert Messages", Icons.AutoMirrored.Filled.Chat, hasTimePicker = false, alwaysOn = true),
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { UserTopBar(title = "Notifications", navController = navController, showBack = true) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            item { Text("MEAL REMINDERS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp)) }
            items(mealNotifs.size) { i -> NotifRow(mealNotifs[i]) }

            item { Text("ACTIVITY REMINDERS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp)) }
            items(activityNotifs.size) { i -> NotifRow(activityNotifs[i]) }

            item { Text("UPDATES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp)) }
            items(updateNotifs.size) { i -> NotifRow(updateNotifs[i]) }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun NotifRow(item: NotifItem) {
    var isEnabled by remember { mutableStateOf(true) }
    Column {
        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.height(56.dp).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(item.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(item.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                if (item.alwaysOn) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Always On", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    )
                } else {
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary, uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
        AnimatedVisibility(visible = isEnabled && item.hasTimePicker && !item.alwaysOn) {
            SurfaceCard(bg = MaterialTheme.colorScheme.surfaceVariant) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Remind me at", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    TextButton(onClick = {}) { Text(item.defaultTime, color = MaterialTheme.colorScheme.primary) }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun NotificationSettingsScreenPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    com.fitpulse.app.AppTheme {
        NotificationSettingsScreen(navController = navController)
    }
}
