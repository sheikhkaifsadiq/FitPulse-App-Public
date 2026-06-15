package com.fitpulse.app.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fitpulse.app.data.local.AppDatabase
import com.fitpulse.app.security.SecurityManager
import com.fitpulse.app.ui.components.*
import com.fitpulse.app.ui.navigation.Screen
import com.fitpulse.app.ui.navigation.UserTopBar
import com.fitpulse.app.ui.viewmodels.FitPulseViewModelFactory
import com.fitpulse.app.ui.viewmodels.ProgressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalorieTrendScreen(navController: NavController) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getInstance(context) }
    val securityManager = remember { SecurityManager(context) }
    val factory = remember { 
        FitPulseViewModelFactory(
            userDao = database.userDao(), 
            foodDao = database.foodDao(), 
            waterDao = database.waterDao(), 
            exerciseDao = database.exerciseDao(), 
            weightDao = database.weightDao(),
            securityManager = securityManager
        ) 
    }
    val viewModel: ProgressViewModel = viewModel(factory = factory)

    val selectedRange by viewModel.dateRange.collectAsState()
    val calorieTrend by viewModel.calorieTrend.collectAsState()
    val ranges = listOf("1W" to 7, "1M" to 30, "3M" to 90)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { UserTopBar(title = "Progress", navController = navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TabRow(
                    selectedTabIndex = 1, containerColor = MaterialTheme.colorScheme.background,
                    indicator = { tabs -> TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabs[1]), color = MaterialTheme.colorScheme.primary) }
                ) {
                    Tab(selected = false, onClick = { navController.navigate(Screen.WeightProgress.route) { popUpTo(Screen.Progress.route) } }, text = { Text("Weight", color = MaterialTheme.colorScheme.onSurfaceVariant) })
                    Tab(selected = true, onClick = { }, text = { Text("Calories", color = MaterialTheme.colorScheme.primary) })
                    Tab(selected = false, onClick = { navController.navigate(Screen.MacroTrend.route) { popUpTo(Screen.Progress.route) } }, text = { Text("Macros", color = MaterialTheme.colorScheme.onSurfaceVariant) })
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ranges.size) { i ->
                        val r = ranges[i]
                        val sel = selectedRange == r.second
                        FilterChip(
                            selected = sel, onClick = { viewModel.setDateRange(r.second) },
                            label = { Text(r.first, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary, containerColor = MaterialTheme.colorScheme.surface, labelColor = MaterialTheme.colorScheme.onSurfaceVariant),
                            border = FilterChipDefaults.filterChipBorder(borderColor = MaterialTheme.colorScheme.outlineVariant, selectedBorderColor = MaterialTheme.colorScheme.primary, enabled = true, selected = sel)
                        )
                    }
                }
            }

            // Chart
            item {
                SurfaceCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("CALORIE INTAKE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                        if (calorieTrend.isNotEmpty()) {
                            val maxCal = calorieTrend.maxOf { it.eaten }.coerceAtLeast(2000)
                            val points = calorieTrend.map { it.eaten.toFloat() / maxCal }
                            LineChart(points = points, modifier = Modifier.fillMaxWidth().height(200.dp), lineColor = MaterialTheme.colorScheme.secondary)
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Text("No calorie data for this period", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item {
                val avgEaten = if(calorieTrend.isNotEmpty()) calorieTrend.sumOf { it.eaten } / calorieTrend.size else 0
                val avgBurned = if(calorieTrend.isNotEmpty()) calorieTrend.sumOf { it.burned } / calorieTrend.size else 0
                
                SurfaceCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.TrendingFlat, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Avg Intake: $avgEaten kcal / day", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Avg Burned: $avgBurned kcal / day", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun CalorieTrendScreenPreview() {
    androidx.compose.material3.MaterialTheme {
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Text("CalorieTrendScreen Preview Placeholder")
        }
    }
}
