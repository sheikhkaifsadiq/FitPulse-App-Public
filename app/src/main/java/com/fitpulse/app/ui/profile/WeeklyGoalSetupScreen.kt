package com.fitpulse.app.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fitpulse.app.ui.components.PrimaryButton
import com.fitpulse.app.ui.components.SurfaceCard
import com.fitpulse.app.ui.components.DarkInputField
import com.fitpulse.app.ui.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun WeeklyGoalSetupScreen(navController: NavController) {
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
            securityManager = securityManager
        ) 
    }
    val viewModel: com.fitpulse.app.ui.viewmodels.ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val profile by viewModel.userProfile.collectAsState()

    var targetWeightWhole by remember { mutableStateOf("70") }
    var targetWeightDecimal by remember { mutableStateOf(".0") }
    var weeklyRate by remember { mutableStateOf("0.5") }

    LaunchedEffect(profile) {
        profile?.let {
            val weightStr = it.targetWeightKg.toString()
            if (weightStr.contains(".")) {
                val parts = weightStr.split(".")
                targetWeightWhole = parts[0]
                targetWeightDecimal = "." + parts[1].take(1)
            } else {
                targetWeightWhole = weightStr
                targetWeightDecimal = ".0"
            }
            weeklyRate = it.weeklyWeightGoalKg.toString()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Set Your Weekly Goal", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text("Let's define your journey", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(Modifier.height(32.dp))
            
            Text("Target Weight", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            com.fitpulse.app.ui.components.WeightSelector(
                selectedWhole = targetWeightWhole,
                selectedDecimal = targetWeightDecimal,
                onWeightChanged = { w, d -> 
                    targetWeightWhole = w
                    targetWeightDecimal = d
                }
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text("Weekly Progress Rate", style = MaterialTheme.typography.labelMedium)
            Slider(
                value = weeklyRate.toFloatOrNull() ?: 0.5f,
                onValueChange = { weeklyRate = String.format(java.util.Locale.US, "%.2f", it) },
                valueRange = 0.25f..1.0f,
                steps = 3
            )
            Text("${weeklyRate} kg per week", style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(32.dp))
            
            PrimaryButton(
                text = "Continue",
                onClick = {
                    val combinedTargetWeight = (targetWeightWhole + targetWeightDecimal).toFloatOrNull() ?: 70f
                    val userToUpdate = profile ?: com.fitpulse.app.data.local.entities.UserEntity(id = java.util.UUID.randomUUID().toString())
                    viewModel.updateProfile(userToUpdate.copy(
                        targetWeightKg = combinedTargetWeight,
                        weeklyWeightGoalKg = weeklyRate.toFloatOrNull() ?: 0.5f,
                        isWeeklyGoalSet = true,
                        isGoalSet = true
                    )) {
                        // Switch back to main thread for navigation
                        kotlinx.coroutines.MainScope().launch {
                            navController.navigate(Screen.Progress.route) {
                                popUpTo(0)
                            }
                        }
                    }
                }
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun WeeklyGoalSetupScreenPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    com.fitpulse.app.AppTheme {
        WeeklyGoalSetupScreen(navController = navController)
    }
}
