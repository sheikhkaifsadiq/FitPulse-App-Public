package com.fitpulse.app.ui.diary

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import com.fitpulse.app.data.local.entities.FoodLogEntity
import com.fitpulse.app.ui.components.*
import com.fitpulse.app.ui.navigation.*
import com.fitpulse.app.ui.viewmodels.DiaryViewModel
import com.fitpulse.app.ui.viewmodels.FitPulseViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * PRODUCTION-READY FOOD DIARY (SRS 3.3.2)
 * Features Dynamic Date Selection, Intelligent Meal Summaries, and Modern Aesthetic.
 */
@Composable
fun FoodDiaryScreen(navController: NavController) {
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
    val viewModel: DiaryViewModel = viewModel(factory = factory)

    val state by viewModel.diaryState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    val dateText = when {
        selectedDate == LocalDate.now() -> "Today"
        selectedDate == LocalDate.now().minusDays(1) -> "Yesterday"
        selectedDate == LocalDate.now().plusDays(1) -> "Tomorrow"
        else -> selectedDate.format(DateTimeFormatter.ofPattern("EEE, d MMM"))
    }

    FoodDiaryContent(
        state = state,
        dateText = dateText,
        onPrevDate = { viewModel.moveDate(-1) },
        onNextDate = { viewModel.moveDate(1) },
        onAddClick = { type -> navController.navigate(Screen.FoodSearch.createRoute(type)) },
        onDeleteClick = { viewModel.deleteLog(it) },
        navController = navController
    )
}

@Composable
fun FoodDiaryContent(
    state: com.fitpulse.app.ui.viewmodels.DiaryState,
    dateText: String,
    onPrevDate: () -> Unit,
    onNextDate: () -> Unit,
    onAddClick: (String) -> Unit,
    onDeleteClick: (FoodLogEntity) -> Unit,
    navController: NavController
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            UserTopBar(
                title = "Food Diary",
                navController = navController
            )
        },
        bottomBar = { MainBottomNav(navController = navController) },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AiFloatingButton { navController.navigate(Screen.AiNutrition.route) }
                FloatingActionButton(
                    onClick = { onAddClick("GENERAL") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) { Icon(Icons.Default.Add, contentDescription = "Add food") }
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

                // DATE SELECTOR
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onPrevDate) { Icon(Icons.Default.ChevronLeft, contentDescription = "Prev", tint = MaterialTheme.colorScheme.onBackground) }
                        Text(dateText, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onNextDate) { Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = MaterialTheme.colorScheme.onBackground) }
                    }
                }

                // REMAINING CALORIE HUD
                item {
                    GlassCard(opacity = 0.12f) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(12.dp))
                                Text("REMAINING BUDGET", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("${state.caloriesRemaining}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(Modifier.width(8.dp))
                                Text("KCAL", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 12.dp))
                            }
                            
                            val progress = if(state.calorieGoal > 0) state.caloriesEaten.toFloat() / state.calorieGoal else 0f
                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(0.05f)
                            )
                        }
                    }
                }

                // MEAL SECTIONS (SRS 3.3.2)
                val mealConfigs = listOf(
                    Triple("Breakfast", state.breakfastLogs, "BREAKFAST"),
                    Triple("Lunch", state.lunchLogs, "LUNCH"),
                    Triple("Dinner", state.dinnerLogs, "DINNER"),
                    Triple("Snacks", state.snackLogs, "SNACK")
                )

                mealConfigs.forEach { (name, logs, type) ->
                    item {
                        ModernMealSection(
                            title = name,
                            logs = logs,
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
fun ModernMealSection(
    title: String,
    logs: List<FoodLogEntity>,
    onAddClick: () -> Unit,
    onDeleteClick: (FoodLogEntity) -> Unit
) {
    val totalCals = logs.sumOf { it.calories }
    val totalPro = logs.sumOf { it.proteinG.toDouble() }.toInt()
    val totalCarb = logs.sumOf { it.carbsG.toDouble() }.toInt()
    val totalFat = logs.sumOf { it.fatG.toDouble() }.toInt()

    SurfaceCard(bg = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                    if (logs.isNotEmpty()) {
                        Text("P:${totalPro}g · C:${totalCarb}g · F:${totalFat}g", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text("${totalCals}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
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
                            Text(log.servingSize, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${log.calories}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { onDeleteClick(log) }) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(0.6f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            } else {
                Text("Tap '+' to log your $title", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f), modifier = Modifier.padding(16.dp))
            }
        }
    }
}
