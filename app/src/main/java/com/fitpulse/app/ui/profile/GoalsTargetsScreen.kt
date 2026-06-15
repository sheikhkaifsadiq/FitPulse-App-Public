package com.fitpulse.app.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fitpulse.app.ui.components.*
import com.fitpulse.app.ui.navigation.*
import com.fitpulse.app.data.local.entities.UserEntity
import kotlinx.coroutines.launch

@Composable
fun GoalsTargetsScreen(navController: NavController) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val database = remember { com.fitpulse.app.data.local.AppDatabase.getInstance(ctx) }
    val securityManager = remember { com.fitpulse.app.security.SecurityManager(ctx) }
    val factory = remember { 
        com.fitpulse.app.ui.viewmodels.FitPulseViewModelFactory(
            userDao = database.userDao(), 
            foodDao = database.foodDao(), 
            waterDao = database.waterDao(),
            exerciseDao = database.exerciseDao(),
            weightDao = database.weightDao(),
            securityManager = securityManager,
            authRepository = com.fitpulse.app.data.repository.AuthRepository()
        ) 
    }
    val viewModel: com.fitpulse.app.ui.viewmodels.ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val profile by viewModel.userProfile.collectAsState()

    GoalsTargetsContent(
        profile = profile,
        onSave = { updatedProfile -> 
            viewModel.updateProfile(updatedProfile) {
                kotlinx.coroutines.MainScope().launch {
                    navController.popBackStack()
                }
            }
        },
        navController = navController
    )
}

@Composable
fun GoalsTargetsContent(
    profile: UserEntity?,
    onSave: (UserEntity) -> Unit,
    navController: NavController
) {
    var calorieTarget by remember { mutableStateOf("") }
    var proteinG by remember { mutableStateOf("") }
    var carbsG by remember { mutableStateOf("") }
    var fatG by remember { mutableStateOf("") }
    var selectedGoalType by remember { mutableStateOf("") }
    var selectedRate by remember { mutableStateOf(0f) }
    var targetWeightWhole by remember { mutableStateOf("70") }
    var targetWeightDecimal by remember { mutableStateOf(".0") }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val securityManager = remember { com.fitpulse.app.security.SecurityManager(context) }
    var weightUnit by remember { mutableStateOf(securityManager.getWeightUnit()) }
    
    LaunchedEffect(Unit) {
        weightUnit = securityManager.getWeightUnit()
    }

    LaunchedEffect(profile) {
        profile?.let {
            calorieTarget = it.dailyCalorieGoal.toString()
            proteinG = (it.dailyCalorieGoal * it.proteinGoalPercentage / 100 / 4).toInt().toString()
            carbsG = (it.dailyCalorieGoal * it.carbGoalPercentage / 100 / 4).toInt().toString()
            fatG = (it.dailyCalorieGoal * it.fatGoalPercentage / 100 / 9).toInt().toString()
            val weightStr = it.targetWeightKg.toString()
            if (weightStr.contains(".")) {
                val parts = weightStr.split(".")
                targetWeightWhole = parts[0]
                targetWeightDecimal = "." + parts[1].take(1)
            } else {
                targetWeightWhole = weightStr
                targetWeightDecimal = ".0"
            }
            if (selectedRate == 0f && it.weeklyWeightGoalKg > 0f) {
                selectedRate = it.weeklyWeightGoalKg
            } else if (selectedRate == 0f) {
                selectedRate = 0.5f
            }
            if (selectedGoalType.isEmpty()) {
                // Load from saved goalType field first
                selectedGoalType = if (it.goalType.isNotEmpty()) it.goalType
                else when {
                    it.weeklyWeightGoalKg < 0f -> "LOSE"
                    it.weeklyWeightGoalKg > 0f -> "BUILD"
                    else -> "MAINTAIN"
                }
            }
        }
    }

    // Auto-recalculate calories when goal type or rate changes
    LaunchedEffect(selectedGoalType, selectedRate) {
        if (selectedGoalType.isNotEmpty()) {
            profile?.let { user ->
                if (user.heightCm > 0 && user.weightKg > 0 && user.age > 0) {
                    val bmr = if (user.gender == "Male") {
                        88.362f + (13.397f * user.weightKg) + (4.799f * user.heightCm) - (5.677f * user.age)
                    } else {
                        447.593f + (9.247f * user.weightKg) + (3.098f * user.heightCm) - (4.330f * user.age)
                    }
                    val activityMultiplier = when (user.activityLevel) {
                        "Sedentary" -> 1.2f
                        "Light" -> 1.375f
                        "Active" -> 1.55f
                        "Very Active" -> 1.725f
                        else -> 1.2f
                    }
                    val maintenance = bmr * activityMultiplier
                    val newCals = when (selectedGoalType) {
                        "LOSE" -> (maintenance - (selectedRate * 1100)).toInt().coerceAtLeast(1200)
                        "BUILD" -> (maintenance + 300).toInt()
                        else -> maintenance.toInt()
                    }
                    calorieTarget = newCals.toString()
                    // Also update macros based on goal type
                    val (pPct, cPct, fPct) = when (selectedGoalType) {
                        "LOSE" -> Triple(0.35f, 0.40f, 0.25f)      // Higher protein for fat loss
                        "BUILD" -> Triple(0.30f, 0.45f, 0.25f)     // Higher carbs for muscle
                        "FITNESS" -> Triple(0.25f, 0.50f, 0.25f)   // Performance macros
                        else -> Triple(0.25f, 0.50f, 0.25f)        // Balanced maintenance
                    }
                    proteinG = (newCals * pPct / 4).toInt().toString()
                    carbsG = (newCals * cPct / 4).toInt().toString()
                    fatG = (newCals * fPct / 9).toInt().toString()
                }
            }
        }
    }

    val ranges = listOf(0.25f, 0.5f, 0.75f, 1.0f)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { UserTopBar(title = "Goals & Targets", navController = navController, showBack = true) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text("GOAL TYPE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val topRow = listOf(Triple("LOSE", "📉", "Lose Weight"), Triple("MAINTAIN", "⚖️", "Maintain"))
                    topRow.forEach { (key, emoji, label) ->
                        val sel = selectedGoalType == key
                        SurfaceCard(
                            modifier = Modifier.weight(1f),
                            bg = if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = if (sel) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            onClick = { selectedGoalType = key }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(emoji, style = MaterialTheme.typography.titleLarge)
                                Text(label, style = MaterialTheme.typography.labelSmall, color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val bottomRow = listOf(Triple("BUILD", "💪", "Build Muscle"), Triple("FITNESS", "🏃", "Improve Fitness"))
                    bottomRow.forEach { (key, emoji, label) ->
                        val sel = selectedGoalType == key
                        SurfaceCard(
                            modifier = Modifier.weight(1f),
                            bg = if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = if (sel) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            onClick = { selectedGoalType = key }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(emoji, style = MaterialTheme.typography.titleLarge)
                                Text(label, style = MaterialTheme.typography.labelSmall, color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(visible = selectedGoalType != "MAINTAIN" && selectedGoalType != "FITNESS") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("WEEKLY RATE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            ranges.forEach { rate ->
                                val sel = selectedRate == rate
                                Box(
                                    modifier = Modifier.weight(1f).height(40.dp)
                                        .background(if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                                        .clickable { selectedRate = rate },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${if(weightUnit == "kg") rate else (rate * 2.20462f).let { Math.round(it * 10) / 10f }}$weightUnit", style = MaterialTheme.typography.labelSmall, color = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("TARGET WEIGHT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        com.fitpulse.app.ui.components.WeightSelector(
                            selectedWhole = targetWeightWhole,
                            selectedDecimal = targetWeightDecimal,
                            onWeightChanged = { w, d -> 
                                targetWeightWhole = w
                                targetWeightDecimal = d
                            }
                        )
                    }
                }
            }

            item {
                ProfileMetricInput(
                    label = "DAILY CALORIE TARGET",
                    value = calorieTarget,
                    unit = "kcal",
                    onValueChange = { calorieTarget = it.filter { char -> char.isDigit() } }
                )
            }

            item { Text("MACRO TARGETS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Protein
                    ProfileMetricInput(
                        label = "Protein",
                        value = proteinG,
                        unit = "g",
                        onValueChange = { proteinG = it.filter { char -> char.isDigit() } },
                        modifier = Modifier.weight(1f)
                    )
                    // Carbs
                    ProfileMetricInput(
                        label = "Carbs",
                        value = carbsG,
                        unit = "g",
                        onValueChange = { carbsG = it.filter { char -> char.isDigit() } },
                        modifier = Modifier.weight(1f)
                    )
                    // Fat
                    ProfileMetricInput(
                        label = "Fat",
                        value = fatG,
                        unit = "g",
                        onValueChange = { fatG = it.filter { char -> char.isDigit() } },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                TextButton(onClick = { 
                    // Professional BMR calculation logic
                    profile?.let { user ->
                        val bmr = if (user.gender == "Male") {
                            88.362f + (13.397f * user.weightKg) + (4.799f * user.heightCm) - (5.677f * user.age)
                        } else {
                            447.593f + (9.247f * user.weightKg) + (3.098f * user.heightCm) - (4.330f * user.age)
                        }
                        
                        val activityMultiplier = when(user.activityLevel) {
                            "Sedentary" -> 1.2f
                            "Light" -> 1.375f
                            "Active" -> 1.55f
                            "Very Active" -> 1.725f
                            else -> 1.2f
                        }
                        
                        val maintenance = bmr * activityMultiplier
                        val target = when(selectedGoalType) {
                            "LOSE" -> maintenance - (selectedRate * 1100) // approx 7700 kcal per kg
                            "BUILD" -> maintenance + 300
                            else -> maintenance
                        }
                        
                        calorieTarget = target.toInt().coerceAtLeast(1200).toString()
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("↻ Recalculate from my stats", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                }
            }

            item { 
                PrimaryButton(
                    text = "Save Changes",
                    onClick = {
                        profile?.let { user ->
                            val cals = calorieTarget.toIntOrNull() ?: 2000
                            val p = proteinG.toFloatOrNull() ?: 150f
                            val c = carbsG.toFloatOrNull() ?: 250f
                            val f = fatG.toFloatOrNull() ?: 70f
                            
                            val totalKcalFromMacros = (p * 4) + (c * 4) + (f * 9)
                            val pPct = (p * 4 / totalKcalFromMacros) * 100
                            val cPct = (c * 4 / totalKcalFromMacros) * 100
                            val fPct = (f * 9 / totalKcalFromMacros) * 100
                            
                            val combinedTargetWeight = (targetWeightWhole + targetWeightDecimal).toFloatOrNull() ?: 70f
                            
                            onSave(user.copy(
                                dailyCalorieGoal = cals,
                                carbGoalPercentage = cPct,
                                proteinGoalPercentage = pPct,
                                fatGoalPercentage = fPct,
                                weeklyWeightGoalKg = selectedRate,
                                targetWeightKg = combinedTargetWeight,
                                goalType = selectedGoalType,
                                isGoalSet = true,
                                isWeeklyGoalSet = true
                            ))
                        }
                    }
                ) 
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun GoalsTargetsScreenPreview() {
    val mockUser = UserEntity(
        fullName = "Alex", email = "alex@example.com", role = "USER", weightKg = 80f, heightCm = 180f, age = 25, gender = "Male", activityLevel = "Active", targetWeightKg = 75f, dailyCalorieGoal = 2500, weeklyWeightGoalKg = 0.5f, isWeeklyGoalSet = true
    )
    com.fitpulse.app.AppTheme {
        GoalsTargetsContent(
            profile = mockUser,
            onSave = {},
            navController = androidx.navigation.compose.rememberNavController()
        )
    }
}
