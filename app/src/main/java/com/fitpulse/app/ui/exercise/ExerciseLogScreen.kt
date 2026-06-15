package com.fitpulse.app.ui.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fitpulse.app.data.local.AppDatabase
import com.fitpulse.app.security.SecurityManager
import com.fitpulse.app.ui.components.*
import com.fitpulse.app.ui.navigation.Screen
import com.fitpulse.app.ui.navigation.UserTopBar
import com.fitpulse.app.ui.navigation.MainBottomNav
import com.fitpulse.app.ui.viewmodels.ExerciseViewModel
import com.fitpulse.app.ui.viewmodels.FitPulseViewModelFactory
import com.fitpulse.app.data.local.entities.ExerciseLogEntity

@Composable
fun ExerciseLogScreen(navController: NavController) {
    val ctx = LocalContext.current
    val database = remember { AppDatabase.getInstance(ctx) }
    val securityManager = remember { SecurityManager(ctx) }
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
    val viewModel: ExerciseViewModel = viewModel(factory = factory)

    val logs by viewModel.exerciseLogs.collectAsState()
    val totalBurned by viewModel.totalBurned.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    @OptIn(ExperimentalMaterial3Api::class)
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh(ctx) },
        modifier = Modifier.fillMaxSize()
    ) {
        ExerciseLogContent(
            logs = logs,
            totalBurned = totalBurned,
            onAddClick = { category -> navController.navigate(Screen.ExerciseSearch.createRoute(category)) },
            onDeleteClick = { viewModel.deleteLog(it) },
            navController = navController
        )
    }
}

@Composable
fun ExerciseLogContent(
    logs: List<ExerciseLogEntity>,
    totalBurned: Int,
    onAddClick: (String) -> Unit,
    onDeleteClick: (ExerciseLogEntity) -> Unit,
    navController: NavController
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { UserTopBar(title = "Exercise Log", navController = navController, showBack = true) },
        bottomBar = { MainBottomNav(navController = navController) },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AiFloatingButton { navController.navigate(Screen.AiNutrition.route) }
                FloatingActionButton(
                    onClick = { onAddClick("Strength") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add exercise")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            GlowBlob(color = MaterialTheme.colorScheme.tertiary, alignment = Alignment.TopStart, size = 300.dp, opacity = 0.1f)

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                // ACTIVE ENERGY HUD
                item {
                    GlassCard(opacity = 0.12f) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFACC15))
                                Spacer(Modifier.width(12.dp))
                                Text("ACTIVE ENERGY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("$totalBurned", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(Modifier.width(8.dp))
                                Text("KCAL BURNED", style = MaterialTheme.typography.labelLarge, color = Color(0xFFFACC15), modifier = Modifier.padding(bottom = 12.dp))
                            }
                            
                            val progress = (totalBurned / 500f).coerceIn(0f, 1f) 
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)),
                                color = Color(0xFFFACC15),
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(0.05f)
                            )
                        }
                    }
                }

                // CATEGORY SECTIONS
                val categories = listOf(
                    Pair("Cardio", "Cardio"),
                    Pair("Strength & Reps", "Strength"),
                    Pair("Sports & Recreation", "Sports")
                )

                categories.forEach { (title, type) ->
                    item {
                        ModernExerciseSection(
                            title = title,
                            type = type,
                            logs = logs.filter { it.type == type },
                            onAddClick = { onAddClick(type) },
                            onDeleteClick = onDeleteClick
                        )
                    }
                }

                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun ModernExerciseSection(
    title: String,
    type: String,
    logs: List<ExerciseLogEntity>,
    onAddClick: () -> Unit,
    onDeleteClick: (ExerciseLogEntity) -> Unit
) {
    val totalBurned = logs.sumOf { it.caloriesBurned }
    val totalDuration = logs.sumOf { it.durationMin }

    SurfaceCard(bg = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                    if (logs.isNotEmpty()) {
                        Text("$totalDuration min total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text("$totalBurned", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                Text(" KCAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(12.dp))
                IconButton(onClick = onAddClick, modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary.copy(0.15f), CircleShape)) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            
            if (logs.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.05f))
                logs.forEach { log ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(log.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                            Text("${log.durationMin} min", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${log.caloriesBurned}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { onDeleteClick(log) }) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(0.6f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            } else {
                Text("Tap '+' to log $title", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f), modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ExerciseLogScreenPreview() {
    val mockLogs = listOf(
        ExerciseLogEntity(name = "Morning Run", type = "Cardio", durationMin = 30, caloriesBurned = 350, date = ""),
        ExerciseLogEntity(name = "Upper Body", type = "Strength", durationMin = 45, caloriesBurned = 280, date = "")
    )
    val navController = androidx.navigation.compose.rememberNavController()
    com.fitpulse.app.AppTheme {
        ExerciseLogContent(
            logs = mockLogs,
            totalBurned = 630,
            onAddClick = {},
            onDeleteClick = {},
            navController = navController
        )
    }
}
