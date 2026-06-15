package com.fitpulse.app.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
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
import com.fitpulse.app.*
import com.fitpulse.app.data.local.entities.UserEntity
import com.fitpulse.app.data.local.entities.WeightLogEntity
import com.fitpulse.app.ui.components.*
import com.fitpulse.app.ui.navigation.MainBottomNav
import com.fitpulse.app.ui.navigation.Screen
import com.fitpulse.app.ui.navigation.UserTopBar
import com.fitpulse.app.ui.viewmodels.DashboardState
import com.fitpulse.app.ui.viewmodels.FitPulseViewModelFactory
import com.fitpulse.app.ui.viewmodels.HomeViewModel
import com.fitpulse.app.ui.viewmodels.ProfileViewModel
import com.fitpulse.app.ui.viewmodels.ProgressViewModel
import com.fitpulse.app.ui.viewmodels.DailyCalorieStat

@Composable
fun ProgressScreen(navController: NavController) {
    val ctx = LocalContext.current
    val database = remember { com.fitpulse.app.data.local.AppDatabase.getInstance(ctx) }
    val securityManager = remember { com.fitpulse.app.security.SecurityManager(ctx) }
    val factory = remember { 
        FitPulseViewModelFactory(
            userDao = database.userDao(), 
            foodDao = database.foodDao(), 
            waterDao = database.waterDao(), 
            exerciseDao = database.exerciseDao(), 
            weightDao = database.weightDao(),
            securityManager = securityManager,
            authRepository = com.fitpulse.app.data.repository.AuthRepository()
        ) 
    }
    val viewModel: ProfileViewModel = viewModel(factory = factory)
    val homeViewModel: HomeViewModel = viewModel(factory = factory)
    val progressViewModel: ProgressViewModel = viewModel(factory = factory)
    
    val user by viewModel.userProfile.collectAsState()
    val dashboardState by homeViewModel.dashboardState.collectAsState()
    val weightHistory by progressViewModel.weightHistory.collectAsState()
    val calorieTrend by progressViewModel.calorieTrend.collectAsState()
    val isRefreshing by progressViewModel.isRefreshing.collectAsState()
    val globalStartWeight by progressViewModel.globalStartWeight.collectAsState()

    var weightUnit by remember { mutableStateOf(securityManager.getWeightUnit()) }
    
    LaunchedEffect(Unit) {
        weightUnit = securityManager.getWeightUnit()
    }
    
    ProgressContent(
        user = user,
        dashboardState = dashboardState,
        weightHistoryLogs = weightHistory,
        globalStartWeight = globalStartWeight,
        calorieTrendStats = calorieTrend,
        weightUnit = weightUnit,
        isRefreshing = isRefreshing,
        onRefresh = { progressViewModel.refresh(ctx) },
        onNavigate = { navController.navigate(it) },
        navController = navController
    )
}

@Composable
fun ProgressContent(
    user: UserEntity?, 
    dashboardState: DashboardState?, 
    weightHistoryLogs: List<WeightLogEntity>,
    globalStartWeight: Float?,
    calorieTrendStats: List<DailyCalorieStat>,
    weightUnit: String,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onNavigate: (String) -> Unit, 
    navController: NavController
) {
    // Weight history for chart (normalize to 0..1 range)
    val weightPoints = if (weightHistoryLogs.isNotEmpty()) {
        val maxWeight = weightHistoryLogs.maxOf { it.weightKg }
        val minWeight = weightHistoryLogs.minOf { it.weightKg }
        val range = (maxWeight - minWeight).coerceAtLeast(1f)
        val pts = weightHistoryLogs.reversed().map { (it.weightKg - minWeight) / range }
        if (pts.size == 1 || maxWeight == minWeight) listOf(0.5f, 0.5f) else pts
    } else listOf(0.5f, 0.5f)
    
    val calorieProgress = if (dashboardState != null && dashboardState.calorieGoal > 0) dashboardState.caloriesEaten.toFloat() / dashboardState.calorieGoal.coerceAtLeast(1) else 0f
    
    // Calorie week for chart
    val calorieWeek = if (calorieTrendStats.isNotEmpty()) {
        val maxCal = calorieTrendStats.maxOf { it.eaten }.coerceAtLeast(2000)
        calorieTrendStats.map { it.eaten.toFloat() / maxCal }
    } else listOf(0f, 0f, 0f, 0f, 0f, 0f, calorieProgress.coerceIn(0f, 1f))
    
    val weekDays = listOf("M", "T", "W", "T", "F", "S", "S")
    val currentKg = user?.weightKg ?: 0f
    val targetKg = user?.targetWeightKg ?: 0f
    val startKg = globalStartWeight ?: currentKg
    
    val current = if (weightUnit == "kg") currentKg else (currentKg * 2.20462f)
    val target = if (weightUnit == "kg") targetKg else (targetKg * 2.20462f)
    val start = if (weightUnit == "kg") startKg else (startKg * 2.20462f)
    
    val progress = if (startKg != targetKg && startKg != 0f) ((startKg - currentKg) / (startKg - targetKg)).coerceIn(0f, 1f) else 0f
    val streak = dashboardState?.streakCount ?: user?.streakCount ?: 0

    val bmi = if (user != null && user.heightCm > 0 && user.weightKg > 0) {
        user.weightKg / ((user.heightCm / 100) * (user.heightCm / 100))
    } else 0f
    
    val bodyFat = if (bmi > 0 && user != null && user.age > 0) {
        val genderFactor = if (user.gender.equals("Male", ignoreCase = true)) 1 else 0
        ((1.2f * bmi) + (0.23f * user.age) - (10.8f * genderFactor) - 5.4f).coerceAtLeast(5f)
    } else 0f

    val avgCalorieTrend = if (calorieTrendStats.isNotEmpty()) calorieTrendStats.sumOf { it.eaten } / calorieTrendStats.size else dashboardState?.caloriesEaten ?: 0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { UserTopBar(title = "Progress", navController = navController) },
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
                item { Spacer(Modifier.height(4.dp)) }

            // ── METRIC HEADER PAIR ─────────────────────────────
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        MetricPair(
                            label1 = "Current Weight",
                            val1 = if(current > 0) "${current.toInt()} $weightUnit" else "--",
                            label2 = "Target Weight",
                            val2 = if(target > 0) "${target.toInt()} $weightUnit" else "--"
                        )
                        // Weight line chart
                        if (weightHistoryLogs.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                                Text("No data yet — log your first entry", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            Text("WEIGHT TREND", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                            LineChart(points = weightPoints, modifier = Modifier.fillMaxWidth().height(100.dp), lineColor = LimeGreen)
                        }
                        // Progress bar
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Start: ${start.toInt()}$weightUnit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Target: ${target.toInt()}$weightUnit", style = MaterialTheme.typography.labelSmall, color = LimeGreen, fontWeight = FontWeight.Bold)
                        }
                        ModernProgressBar(progress = progress, color = LimeGreen)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                            Text(" TO GOAL", style = MaterialTheme.typography.labelLarge, color = LimeGreen, modifier = Modifier.padding(bottom = 4.dp, start = 6.dp))
                        }
                        Spacer(Modifier.height(4.dp))
                        PrimaryButton(
                            text = "Log Weight",
                            onClick = { onNavigate(Screen.WeightProgress.route) }
                        )
                    }
                }
            }

            // ── CONSISTENCY SCORE ──────────────────────────────
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth(), bg = LimeGreen.copy(0.08f), border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(0.3f))) {
                    Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("CONSISTENCY SCORE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("$streak Day Streak 🔥", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                            Text("Keep it up!", style = MaterialTheme.typography.bodySmall, color = LimeGreen)
                        }
                        Text("🏆", fontSize = 44.sp)
                    }
                }
            }

            // ── CALORIE TREND BAR CHART ───────────────────────
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth(), onClick = { onNavigate(Screen.CalorieTrend.route) }) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (calorieTrendStats.isEmpty() || calorieTrendStats.all { it.eaten == 0 && it.burned == 0 }) {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                                Text("No data yet — log your first entry", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("CALORIE TREND", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                                    Text("This Week", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(LimeGreen.copy(0.15f)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                                    Text("Avg: $avgCalorieTrend kcal", style = MaterialTheme.typography.labelMedium, color = LimeGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                            BarChart(values = calorieWeek, labels = weekDays, highlightIndex = calorieTrendStats.size - 1, modifier = Modifier.fillMaxWidth().height(120.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                weekDays.forEach { d ->
                                    Text(d, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }

            // ── MACRO NUTRITION DONUT ─────────────────────────
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth(), onClick = { onNavigate(Screen.MacroTrend.route) }) {
                    Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        val totalMacros = (dashboardState?.proteinG ?: 0f) + (dashboardState?.carbsG ?: 0f) + (dashboardState?.fatG ?: 0f)
                        val pPct = if (totalMacros > 0) (dashboardState?.proteinG ?: 0f) / totalMacros * 100 else 0f
                        val cPct = if (totalMacros > 0) (dashboardState?.carbsG ?: 0f) / totalMacros * 100 else 0f
                        val fPct = if (totalMacros > 0) (dashboardState?.fatG ?: 0f) / totalMacros * 100 else 0f
                        
                        DonutChart(
                            segments = listOf(Pair(pPct.coerceAtLeast(1f), LimeGreen), Pair(cPct.coerceAtLeast(1f), AccentPurple), Pair(fPct.coerceAtLeast(1f), AccentOrange)),
                            modifier = Modifier.size(130.dp), strokeWidth = 30f
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Nutrient", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Nutrient Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            NutrientLegendItem("Protein", "${pPct.toInt()}%", LimeGreen)
                            NutrientLegendItem("Carbs", "${cPct.toInt()}%", AccentPurple)
                            NutrientLegendItem("Fat", "${fPct.toInt()}%", AccentOrange)
                        }
                    }
                }
            }

            // ── BODY METRICS PREMIUM SECTION ──────────────────
            item {
                SectionLabel("Body Metrics")
                Spacer(Modifier.height(8.dp))
                
                // BMI + Body Fat - Wide hero cards
                val bmiString = if (bmi > 0) String.format("%.1f", bmi) else "--"
                val bfString = if (bodyFat > 0) String.format("%.1f", bodyFat) else "--"
                val muscleString = if (bodyFat > 0) String.format("%.1f", (100f - bodyFat - 10f).coerceAtLeast(0f)) else "--"
                val bmiProgress = if (bmi > 0) (bmi / 40f).coerceIn(0f, 1f) else 0f
                val bfProgress = if (bodyFat > 0) (bodyFat / 35f).coerceIn(0f, 1f) else 0f
                val muscleProgress = if (bodyFat > 0) ((100f - bodyFat - 10f).coerceAtLeast(0f) / 60f).coerceIn(0f, 1f) else 0f

                val bmiCategory = when {
                    bmi <= 0 -> "No data"
                    bmi < 18.5f -> "Underweight"
                    bmi < 25f -> "Healthy"
                    bmi < 30f -> "Overweight"
                    else -> "Obese"
                }
                val bmiColor = when {
                    bmi <= 0 -> MaterialTheme.colorScheme.onSurfaceVariant
                    bmi < 18.5f -> AccentBlue
                    bmi < 25f -> LimeGreen
                    bmi < 30f -> AccentOrange
                    else -> ErrorRed
                }
                val bfCategory = when {
                    bodyFat <= 0 -> "No data"
                    bodyFat < 10f -> "Athletic"
                    bodyFat < 20f -> "Fit"
                    bodyFat < 28f -> "Average"
                    else -> "High"
                }
                val bfColor = when {
                    bodyFat <= 0 -> MaterialTheme.colorScheme.onSurfaceVariant
                    bodyFat < 10f -> AccentBlue
                    bodyFat < 20f -> LimeGreen
                    bodyFat < 28f -> AccentOrange
                    else -> ErrorRed
                }

                // Top row: BMI + Body Fat
                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // BMI Card - with arc gauge
                    SurfaceCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { navigateToMetric(MetricType.BMI, onNavigate) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(bmiColor.copy(0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) { Text("⚖️", fontSize = 16.sp) }
                                    Column {
                                        Text("BMI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                                        Text(bmiCategory, style = MaterialTheme.typography.labelSmall, color = bmiColor, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(bmiString, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(6.dp)) {
                                    val trackColor = bmiColor.copy(alpha = 0.15f)
                                    drawRoundRect(color = trackColor, cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f))
                                    drawRoundRect(color = bmiColor, size = androidx.compose.ui.geometry.Size(size.width * bmiProgress, size.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f))
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("15", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f))
                                    Text("40+", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f))
                                }
                            }
                        }
                    }

                    // Body Fat Card
                    SurfaceCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { navigateToMetric(MetricType.BODY_FAT, onNavigate) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(bfColor.copy(0.15f)),
                                    contentAlignment = Alignment.Center
                                ) { Text("🔥", fontSize = 16.sp) }
                                Text("Body Fat", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                                Text(if (bodyFat > 0) "$bfString%" else "--", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(10.dp)).background(bfColor.copy(0.12f))
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth(bfProgress).fillMaxHeight().clip(RoundedCornerShape(10.dp)).background(bfColor))
                                }
                                Text(bfCategory, style = MaterialTheme.typography.labelSmall, color = bfColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Bottom row: Muscle Mass + Calorie Trend
                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Muscle Mass Card
                    SurfaceCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        bg = LimeGreen.copy(0.06f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(0.2f)),
                        onClick = { navigateToMetric(MetricType.MUSCLE, onNavigate) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(LimeGreen.copy(0.2f)),
                                    contentAlignment = Alignment.Center
                                ) { Text("💪", fontSize = 16.sp) }
                                Text("Muscle Mass", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                                Text(if (bodyFat > 0) "$muscleString%" else "--", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(10.dp)).background(LimeGreen.copy(0.15f))
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth(muscleProgress).fillMaxHeight().clip(RoundedCornerShape(10.dp)).background(LimeGreen))
                                }
                                Text("Est. lean mass", style = MaterialTheme.typography.labelSmall, color = LimeGreen.copy(0.8f))
                            }
                        }
                    }

                    // Calorie Trend Shortcut Card
                    SurfaceCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        bg = AccentPurple.copy(0.06f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentPurple.copy(0.2f)),
                        onClick = { navigateToMetric(MetricType.CALORIES, onNavigate) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(AccentPurple.copy(0.18f)),
                                        contentAlignment = Alignment.Center
                                    ) { Text("📊", fontSize = 16.sp) }
                                    Column {
                                        Text("CALORIES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                                        Text("Trend", style = MaterialTheme.typography.labelSmall, color = AccentPurple, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text("$avgCalorieTrend", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                                Text("avg kcal / day", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(AccentPurple.copy(0.12f)).padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("View full trend →", style = MaterialTheme.typography.labelSmall, color = AccentPurple, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}
}


enum class MetricType { BODY_FAT, CALORIES, BMI, MUSCLE }

fun navigateToMetric(type: MetricType, onNavigate: (String) -> Unit) {
    when (type) {
        MetricType.BODY_FAT -> onNavigate(Screen.BodyMetrics.route)
        MetricType.CALORIES  -> onNavigate(Screen.CalorieTrend.route)
        MetricType.BMI       -> onNavigate(Screen.BodyMetrics.route)
        MetricType.MUSCLE    -> onNavigate(Screen.BodyMetrics.route)
    }
}
