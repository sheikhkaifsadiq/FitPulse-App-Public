package com.fitpulse.app.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import com.fitpulse.app.*
import com.fitpulse.app.ui.components.*
import com.fitpulse.app.ui.navigation.*

@Composable
fun AppPreferencesScreen(navController: NavController) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val securityManager = remember { com.fitpulse.app.security.SecurityManager(ctx) }
    
    var weightUnit by remember { mutableStateOf(securityManager.getWeightUnit()) }
    var heightUnit by remember { mutableStateOf(securityManager.getHeightUnit()) }
    var waterUnit by remember { mutableStateOf(securityManager.getWaterUnit()) }
    val isDark = LocalIsDarkTheme.current
    val themeSwitcher = LocalThemeSwitcher.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { UserTopBar(title = "Preferences", navController = navController, showBack = true) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            item { Text("UNITS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp)) }
            item {
                PrefRow(
                    icon = Icons.Default.MonitorWeight,
                    label = "Weight Unit",
                    trailing = {
                        UnitToggle(
                            option1 = "kg", option2 = "lbs", 
                            selected = weightUnit, 
                            onSelect = { weightUnit = it; securityManager.setWeightUnit(it) }
                        )
                    }
                )
            }
            item {
                PrefRow(
                    icon = Icons.Default.Height,
                    label = "Height Unit",
                    trailing = {
                        UnitToggle(
                            option1 = "cm", option2 = "ft", 
                            selected = heightUnit, 
                            onSelect = { heightUnit = it; securityManager.setHeightUnit(it) }
                        )
                    }
                )
            }
            item {
                PrefRow(
                    icon = Icons.Default.WaterDrop,
                    label = "Water Unit",
                    trailing = {
                        UnitToggle(
                            option1 = "ml", option2 = "oz", 
                            selected = waterUnit, 
                            onSelect = { waterUnit = it; securityManager.setWaterUnit(it) }
                        )
                    }
                )
            }

            item { Text("CALENDAR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp)) }
            item {
                PrefRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Week Starts On",
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Monday", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                )
            }

            item { Text("REGION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp)) }
            item {
                PrefRow(
                    icon = Icons.Default.Language,
                    label = "Language",
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("English", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                )
            }

            item { Text("DISPLAY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp)) }
            item {
                PrefRow(
                    icon = Icons.Default.DarkMode,
                    label = "Dark Mode",
                    supportingText = "Dark mode is enabled by default",
                    trailing = {
                        Switch(
                            checked = isDark, 
                            onCheckedChange = { themeSwitcher(it) }, 
                            enabled = true, 
                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary, uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun PrefRow(icon: ImageVector, label: String, trailing: @Composable () -> Unit, supportingText: String? = null) {
    SurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                if (supportingText != null) Text(supportingText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            trailing()
        }
    }
}

@Composable
fun UnitToggle(option1: String, option2: String, selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(50)).padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
            .background(if (selected == option1) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
            .clickable { onSelect(option1) }
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(option1, style = MaterialTheme.typography.labelMedium, color = if (selected == option1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
            .background(if (selected == option2) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
            .clickable { onSelect(option2) }
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(option2, style = MaterialTheme.typography.labelMedium, color = if (selected == option2) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AppPreferencesScreenPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    com.fitpulse.app.AppTheme {
        AppPreferencesScreen(navController = navController)
    }
}
