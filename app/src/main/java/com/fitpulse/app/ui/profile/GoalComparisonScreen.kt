package com.fitpulse.app.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fitpulse.app.ui.components.SurfaceCard
import com.fitpulse.app.ui.navigation.UserTopBar

@Composable
fun GoalComparisonScreen(navController: NavController) {
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
    val viewModel: com.fitpulse.app.ui.viewmodels.HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val state by viewModel.dashboardState.collectAsState()
    val user by viewModel.userProfile.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { com.fitpulse.app.ui.navigation.UserTopBar(title = "Goals Progress", navController = navController, showBack = true) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Daily Progress", style = MaterialTheme.typography.titleMedium)
                SurfaceCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Calories: ${state.caloriesEaten} / ${state.calorieGoal}")
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                        Text("${state.caloriesRemaining} kcal remaining today", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            item {
                Text("Weekly Weight Goal", style = MaterialTheme.typography.titleMedium)
                SurfaceCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        user?.let {
                            Text("Target: ${it.targetWeightKg} kg")
                            Text("Weekly Rate: ${it.weeklyWeightGoalKg} kg/week")
                            Spacer(Modifier.height(8.dp))
                            Text("Stay consistent to reach your goal!", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun GoalComparisonScreenPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    com.fitpulse.app.AppTheme {
        GoalComparisonScreen(navController = navController)
    }
}
