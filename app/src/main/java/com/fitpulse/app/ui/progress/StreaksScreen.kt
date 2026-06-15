package com.fitpulse.app.ui.progress

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fitpulse.app.ui.components.*
import com.fitpulse.app.ui.navigation.*

@Composable
fun StreaksScreen(navController: NavController) {
    val context = LocalContext.current
    val database = remember { com.fitpulse.app.data.local.AppDatabase.getInstance(context) }
    val factory = remember { 
        com.fitpulse.app.ui.viewmodels.FitPulseViewModelFactory(
            userDao = database.userDao(), 
            foodDao = database.foodDao(), 
            waterDao = database.waterDao(), 
            exerciseDao = database.exerciseDao(), 
            weightDao = database.weightDao(),
            securityManager = com.fitpulse.app.security.SecurityManager(context)
        ) 
    }
    val viewModel: com.fitpulse.app.ui.viewmodels.ProfileViewModel = viewModel(factory = factory)
    val profile by viewModel.userProfile.collectAsState()

    val currentStreak = profile?.streakCount ?: 0
    val bestStreak = profile?.bestStreak ?: 0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { UserTopBar(title = "Streaks", navController = navController) },
        bottomBar = { MainBottomNav(navController = navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current streak hero card
            item {
                SurfaceCard {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        GlowBlob(color = MaterialTheme.colorScheme.secondary, alignment = Alignment.Center, size = 200.dp, opacity = 0.15f)
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🔥", style = MaterialTheme.typography.displayMedium)
                            Text(currentStreak.toString(), style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.secondary)
                            Text("Day Streak", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(8.dp))
                            Text("Best ever: $bestStreak days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Calendar heatmap
            item {
                SurfaceCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("THIS MONTH", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
                                Text(d, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        // Simplified calendar grid
                        val loggedDays = setOf(1, 2, 3, 5, 6, 7, 8, 10, 12, 13, 14, 15)
                        (0..4).forEach { week ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                (0..6).forEach { dayOfWeek ->
                                    val dayNum = week * 7 + dayOfWeek + 1
                                    if (dayNum <= 31) {
                                        Box(
                                            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp))
                                                .background(if (dayNum in loggedDays) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(dayNum.toString(), style = MaterialTheme.typography.labelSmall, color = if (dayNum in loggedDays) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    } else Box(modifier = Modifier.size(32.dp))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }

            // Badges
            item { Text("BADGES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 0.dp)) {
                    val badges = listOf(Triple("7 Days", "🏅", true), Triple("14 Days", "🥈", true), Triple("30 Days", "🥇", false), Triple("60 Days", "💎", false), Triple("100 Days", "👑", false))
                    items(badges.size) { i ->
                        val (label, emoji, earned) = badges[i]
                        if (earned) {
                            SurfaceCard(modifier = Modifier.size(80.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary), bg = MaterialTheme.colorScheme.primaryContainer) {
                                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text(emoji, style = MaterialTheme.typography.titleLarge)
                                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                                }
                            }
                        } else {
                            SurfaceCard(modifier = Modifier.size(80.dp), bg = MaterialTheme.colorScheme.surfaceVariant) {
                                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun StreaksScreenPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    com.fitpulse.app.AppTheme {
        StreaksScreen(navController = navController)
    }
}
