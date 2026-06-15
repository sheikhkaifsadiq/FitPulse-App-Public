package com.fitpulse.app.ui.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
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
import com.fitpulse.app.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroTrendScreen(navController: NavController) {
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
    val macroTrend by viewModel.macroTrend.collectAsState()
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
                    selectedTabIndex = 2, containerColor = MaterialTheme.colorScheme.background,
                    indicator = { tabs -> TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabs[2]), color = MaterialTheme.colorScheme.primary) }
                ) {
                    Tab(selected = false, onClick = { navController.navigate(Screen.WeightProgress.route) { popUpTo(Screen.Progress.route) } }, text = { Text("Weight", color = MaterialTheme.colorScheme.onSurfaceVariant) })
                    Tab(selected = false, onClick = { navController.navigate(Screen.CalorieTrend.route) { popUpTo(Screen.Progress.route) } }, text = { Text("Calories", color = MaterialTheme.colorScheme.onSurfaceVariant) })
                    Tab(selected = true, onClick = { }, text = { Text("Macros", color = MaterialTheme.colorScheme.primary) })
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

            // Protein Chart
            item {
                SectionLabel("Protein Trend")
                SurfaceCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (macroTrend.isNotEmpty()) {
                            val proteinPoints = macroTrend.map { it.protein / 200f } // Normalize roughly
                            LineChart(points = proteinPoints, modifier = Modifier.fillMaxWidth().height(150.dp), lineColor = LimeGreen)
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                                Text("No data for this period", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Carbs Chart
            item {
                SectionLabel("Carbs Trend")
                SurfaceCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (macroTrend.isNotEmpty()) {
                            val carbPoints = macroTrend.map { it.carbs / 400f }
                            LineChart(points = carbPoints, modifier = Modifier.fillMaxWidth().height(150.dp), lineColor = AccentPurple)
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                                Text("No data for this period", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Fat Chart
            item {
                SectionLabel("Fat Trend")
                SurfaceCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (macroTrend.isNotEmpty()) {
                            val fatPoints = macroTrend.map { it.fat / 100f }
                            LineChart(points = fatPoints, modifier = Modifier.fillMaxWidth().height(150.dp), lineColor = AccentOrange)
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                                Text("No data for this period", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
@androidx.compose.runtime.Composable
fun MacroTrendScreenPreview() {
    androidx.compose.material3.MaterialTheme {
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Text("MacroTrendScreen Preview Placeholder")
        }
    }
}
