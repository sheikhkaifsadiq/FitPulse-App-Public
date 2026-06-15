package com.fitpulse.app.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fitpulse.app.*
import com.fitpulse.app.ui.components.*
import com.fitpulse.app.ui.navigation.*
import com.fitpulse.app.ui.viewmodels.HomeViewModel
import com.fitpulse.app.ui.viewmodels.WaterViewModel
import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.fitpulse.app.data.local.entities.ExerciseLogEntity

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val database = remember { com.fitpulse.app.data.local.AppDatabase.getInstance(context) }
    val securityManager = remember { com.fitpulse.app.security.SecurityManager(context) }
    val factory = remember {
        com.fitpulse.app.ui.viewmodels.FitPulseViewModelFactory(
            database.userDao(), database.foodDao(), database.waterDao(),
            database.exerciseDao(), database.weightDao(), securityManager,
            com.fitpulse.app.data.repository.AuthRepository()
        )
    }
    val viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val state by viewModel.dashboardState.collectAsStateWithLifecycle()
    val weeklySummary by viewModel.weeklySummary.collectAsStateWithLifecycle()
    val dashboardInsights by viewModel.dashboardInsights.collectAsStateWithLifecycle()
    val waterIntake by viewModel.dailyWaterIntake.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val startupEvent by viewModel.startupEvent.collectAsState()
    var showStreakPopup by remember { mutableStateOf(false) }
    var streakValue by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> /* Permissions handled */ }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    LaunchedEffect(startupEvent) {
        when (val event = startupEvent) {
            is com.fitpulse.app.ui.viewmodels.StartupEvent.ShowStreak -> {
                streakValue = event.count; showStreakPopup = true; viewModel.clearStartupEvent()
            }
            else -> {}
        }
    }

    var backPressedOnce by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        if (backPressedOnce) {
            (context as? android.app.Activity)?.finish()
        } else {
            backPressedOnce = true
            android.widget.Toast.makeText(context, "Press back again to exit", android.widget.Toast.LENGTH_SHORT).show()
            coroutineScope.launch {
                delay(2000)
                backPressedOnce = false
            }
        }
    }

    if (showStreakPopup) {
        AlertDialog(
            onDismissRequest = { showStreakPopup = false },
            confirmButton = { Button(onClick = { showStreakPopup = false }, colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = DarkBg)) { Text("Keep it up!", fontWeight = FontWeight.Bold) } },
            title = { Text("🔥 $streakValue Day Streak!", fontWeight = FontWeight.Black, fontSize = 22.sp) },
            text = { Text("Consistency is the key. You're doing amazing!") },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    HomeScreenContent(
        state = state, 
        weeklySummary = weeklySummary,
        insights = dashboardInsights,
        waterIntake = waterIntake,
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh(context) },
        onAddWater = { viewModel.addWater() },
        onRemoveWater = { viewModel.removeWater() },
        navController = navController
    )
}

@Composable
fun HomeScreenContent(
    state: com.fitpulse.app.ui.viewmodels.DashboardState,
    weeklySummary: List<com.fitpulse.app.utils.DailySummary>,
    insights: com.fitpulse.app.utils.DashboardInsights,
    waterIntake: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onAddWater: () -> Unit,
    onRemoveWater: () -> Unit,
    navController: NavController
) {
    val firstName = state.userName.split(" ").firstOrNull() ?: "Athlete"
    val today = java.text.SimpleDateFormat("EEEE, dd MMM", java.util.Locale.getDefault()).format(java.util.Date())

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { UserTopBar(title = "Today", navController = navController) },
        bottomBar = { MainBottomNav(navController = navController) },
        floatingActionButton = {
            AiFloatingButton { navController.navigate(Screen.AiNutrition.route) }
        }
    ) { pv ->
        @OptIn(ExperimentalMaterial3Api::class)
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(pv)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            // ── GREETING + DATE ──────────────────────────────────
            item {
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Your Day,", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("$firstName+", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
                            if (state.isPremium) {
                                Box(modifier = Modifier.background(AccentOrange.copy(0.15f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text("PRO", style = MaterialTheme.typography.labelSmall, color = AccentOrange, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Box(modifier = Modifier.background(MaterialTheme.colorScheme.onSurfaceVariant.copy(0.15f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text("FREE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Box(modifier = Modifier.background(LimeGreen, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                            Text(today, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = DarkBg)
                        }
                    }
                    // Streak badge
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔥", fontSize = 20.sp)
                            Text("${state.streakCount}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = LimeGreen)
                        }
                    }
                }
            }

            // ── AI INSIGHT CARD (Top) ─────────────────────────────
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth(), bg = LimeGreen.copy(0.08f), border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(0.3f))) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(LimeGreen.copy(0.2f)), contentAlignment = Alignment.Center) {
                            Text("🔥", fontSize = 22.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("INSIGHTS", style = MaterialTheme.typography.labelSmall, color = LimeGreen, letterSpacing = 1.sp)
                            Text(insights.primaryMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // ── MACRO RINGS (Middle) ──────────────────────────────
            item {
                MacroRingsCard(
                    calories = state.caloriesEaten,
                    calorieGoal = state.calorieGoal,
                    proteinG = state.proteinG,
                    proteinGoal = state.proteinGoal,
                    carbsG = state.carbsG,
                    carbGoal = state.carbGoal,
                    fatG = state.fatG,
                    fatGoal = state.fatGoal
                )
            }

            // ── WATER TRACKER ─────────────────────────────────────
            item {
                WaterTrackerCard(
                    amountInMl = waterIntake,
                    onAddWater = onAddWater,
                    onRemoveWater = onRemoveWater
                )
            }

            // ── WEEKLY TREND CHART (Bottom) ───────────────────────
            item {
                WeeklyTrendChart(weeklyData = weeklySummary)
            }

            // ── ACTIVITY CARDS (Meditation / Steps / Exercise) ────
            item {
                SectionLabel("Activities", "See All") { navController.navigate(Screen.ExerciseLog.route) }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (state.todayExercises.isEmpty()) {
                        SurfaceCard(modifier = Modifier.fillMaxWidth(), bg = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f)) {
                            Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("No activities logged today", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        state.todayExercises.forEach { exercise ->
                            ActivityRowCard(
                                title = exercise.name,
                                subtitle = "${exercise.durationMin} min | ${exercise.caloriesBurned} kcal",
                                progress = 1f,
                                icon = Icons.Default.FitnessCenter,
                                color = LimeGreen
                            ) { navController.navigate(Screen.ExerciseLog.route) }
                        }
                    }
                }
            }

            // ── MEAL CHIPS ───────────────────────────────────────
            item { SectionLabel("Log Food", "Diary") { navController.navigate(Screen.FoodDiary.route) } }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val meals = listOf(Triple("Breakfast", Icons.Default.WbSunny, LimeGreen), Triple("Lunch", Icons.Default.LightMode, AccentOrange), Triple("Dinner", Icons.Default.Nightlight, AccentPurple), Triple("Snacks", Icons.Default.Cookie, AccentBlue))
                    items(meals.size) { i ->
                        val (name, icon, color) = meals[i]
                        MealChip(name, icon, color) { navController.navigate(Screen.FoodSearch.createRoute(name.uppercase())) }
                    }
                }
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
    }
}

@Composable
private fun MealChip(name: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    SurfaceCard(modifier = Modifier.width(110.dp), onClick = onClick) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(color.copy(0.15f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = name, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ── PREVIEW ──────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F, name = "Home — Dark")
@Composable
fun HomeScreenPreviewDark() {
    AppTheme(useDarkTheme = true) {
        HomeScreenContent(
            state = com.fitpulse.app.ui.viewmodels.DashboardState(
                userName = "Alex Johnson", caloriesEaten = 1450, calorieGoal = 2200,
                proteinG = 98f, carbsG = 180f, fatG = 52f,
                proteinGoal = 150, carbGoal = 250, fatGoal = 70,
                streakCount = 12
            ),
            weeklySummary = emptyList(),
            insights = com.fitpulse.app.utils.DashboardInsights(12, "Incredible! 12 days in a row."),
            waterIntake = 750,
            isRefreshing = false,
            onRefresh = {},
            onAddWater = {},
            onRemoveWater = {},
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F4F4, name = "Home — Light")
@Composable
fun HomeScreenPreviewLight() {
    AppTheme(useDarkTheme = false) {
        HomeScreenContent(
            state = com.fitpulse.app.ui.viewmodels.DashboardState(
                userName = "Alex Johnson", caloriesEaten = 1820, calorieGoal = 2200,
                proteinG = 130f, carbsG = 220f, fatG = 60f,
                proteinGoal = 150, carbGoal = 250, fatGoal = 70,
                streakCount = 7
            ),
            weeklySummary = emptyList(),
            insights = com.fitpulse.app.utils.DashboardInsights(7, "You're on fire! 7-day logging streak."),
            waterIntake = 1500,
            isRefreshing = false,
            onRefresh = {},
            onAddWater = {},
            onRemoveWater = {},
            navController = rememberNavController()
        )
    }
}
