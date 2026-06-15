package com.fitpulse.app.ui.progress

import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fitpulse.app.data.local.AppDatabase
import com.fitpulse.app.data.local.entities.WeightLogEntity
import com.fitpulse.app.security.SecurityManager
import com.fitpulse.app.ui.components.*
import com.fitpulse.app.ui.navigation.Screen
import com.fitpulse.app.ui.navigation.UserTopBar
import com.fitpulse.app.ui.viewmodels.FitPulseViewModelFactory
import com.fitpulse.app.ui.viewmodels.ProgressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightProgressScreen(navController: NavController) {
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

    val weightHistory by viewModel.weightHistory.collectAsState()
    val userProfile by database.userDao().getCurrentUser().collectAsState(initial = null)
    
    var showSheet by remember { mutableStateOf(false) }
    var weightInput by remember { mutableStateOf("") }
    val selectedRange by viewModel.dateRange.collectAsState()
    val ranges = listOf("1W" to 7, "1M" to 30, "3M" to 90, "All" to 365)
    
    val weightUnit = securityManager.getWeightUnit()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { UserTopBar(title = "Progress", navController = navController) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showSheet = true },
                containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Log Weight", style = MaterialTheme.typography.labelLarge) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TabRow(
                    selectedTabIndex = 0, containerColor = MaterialTheme.colorScheme.background,
                    indicator = { tabs -> TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabs[0]), color = MaterialTheme.colorScheme.primary) }
                ) {
                    Tab(selected = true, onClick = {}, text = { Text("Weight", color = MaterialTheme.colorScheme.primary) })
                    Tab(selected = false, onClick = { navController.navigate(Screen.CalorieTrend.route) { popUpTo(Screen.Progress.route) } }, text = { Text("Calories", color = MaterialTheme.colorScheme.onSurfaceVariant) })
                    Tab(selected = false, onClick = { navController.navigate(Screen.MacroTrend.route) { popUpTo(Screen.Progress.route) } }, text = { Text("Macros", color = MaterialTheme.colorScheme.onSurfaceVariant) })
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ranges.size) { i ->
                        val r = ranges[i]; val sel = selectedRange == r.second
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
                        Text("WEIGHT CHART", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                        if (weightHistory.isNotEmpty()) {
                            val maxW = weightHistory.maxOf { it.weightKg }
                            val minW = weightHistory.minOf { it.weightKg }
                            val range = (maxW - minW).coerceAtLeast(1f)
                            val pts = weightHistory.reversed().map { (it.weightKg - minW) / range }
                            val points = if (pts.size == 1) listOf(pts[0], pts[0]) else pts
                            LineChart(points = points, modifier = Modifier.fillMaxWidth().height(180.dp), lineColor = MaterialTheme.colorScheme.primary)
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                                Text("No weight logs yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Stat row
            item {
                val currentKg = userProfile?.weightKg ?: 0f
                val targetKg = userProfile?.targetWeightKg ?: 0f
                val startKg = if (weightHistory.isNotEmpty()) weightHistory.last().weightKg else currentKg
                val changeKg = currentKg - startKg
                
                val current = if (weightUnit == "kg") currentKg else (currentKg * 2.20462f)
                val target = if (weightUnit == "kg") targetKg else (targetKg * 2.20462f)
                val start = if (weightUnit == "kg") startKg else (startKg * 2.20462f)
                val change = if (weightUnit == "kg") changeKg else (changeKg * 2.20462f)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val stats = listOf(
                        Triple("Current", "${current.toInt()}$weightUnit", MaterialTheme.colorScheme.primary),
                        Triple("Start", "${start.toInt()}$weightUnit", MaterialTheme.colorScheme.onSurfaceVariant),
                        Triple("Target", "${target.toInt()}$weightUnit", MaterialTheme.colorScheme.secondary),
                        Triple("Change", "${if(change >= 0) "+" else ""}${change.toInt()}$weightUnit", if(change <= 0) Color(0xFF22C55E) else MaterialTheme.colorScheme.error)
                    )
                    stats.forEach { (label, value, color) ->
                        SurfaceCard(modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(value, style = MaterialTheme.typography.titleSmall, color = color)
                                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // History
            item {
                SurfaceCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("HISTORY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                        if (weightHistory.isEmpty()) {
                            Text("No history recorded", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
                        }
                        weightHistory.forEach { log ->
                            Row(modifier = Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(log.date, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                }
                                val displayWeight = if (weightUnit == "kg") log.weightKg else (log.weightKg * 2.20462f)
                                Text("${String.format("%.1f", displayWeight)} $weightUnit", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(Modifier.width(16.dp))
                                IconButton(onClick = { viewModel.deleteWeight(log) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(0.6f), modifier = Modifier.size(20.dp))
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }, containerColor = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Log Weight", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                DarkInputField(
                    label = "Weight ($weightUnit)",
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                PrimaryButton(text = "Save", onClick = { 
                    weightInput.toFloatOrNull()?.let { w ->
                        val kg = if (weightUnit == "kg") w else (w / 2.20462f)
                        viewModel.logWeight(kg)
                    }
                    showSheet = false 
                    weightInput = ""
                })
            }
        }
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun WeightProgressScreenPreview() {
    androidx.compose.material3.MaterialTheme {
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Text("WeightProgressScreen Preview Placeholder")
        }
    }
}
