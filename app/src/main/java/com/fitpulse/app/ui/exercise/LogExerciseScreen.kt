package com.fitpulse.app.ui.exercise

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fitpulse.app.data.local.AppDatabase
import com.fitpulse.app.security.SecurityManager
import com.fitpulse.app.ui.components.*
import com.fitpulse.app.ui.navigation.*
import com.fitpulse.app.ui.viewmodels.ExerciseViewModel
import com.fitpulse.app.ui.viewmodels.FitPulseViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogExerciseScreen(navController: NavController, exerciseId: String = "") {
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

    var duration by remember { mutableStateOf("30") }
    var calories by remember { mutableStateOf("250") }
    
    val exerciseName = exerciseId.replace("_", " ")
    val exerciseType = if (exerciseName.contains("lifting", true) || exerciseName.contains("strength", true)) "Strength" else "Cardio"

    LogExerciseContent(
        exerciseName = exerciseName,
        duration = duration,
        calories = calories,
        onDurationChange = { duration = it },
        onCaloriesChange = { calories = it },
        onBackClick = { navController.popBackStack() },
        onLogClick = {
            val d = duration.toIntOrNull() ?: 0
            val c = calories.toIntOrNull() ?: 0
            viewModel.logExercise(exerciseName, exerciseType, d, c)
            navController.popBackStack()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogExerciseContent(
    exerciseName: String,
    duration: String,
    calories: String,
    onDurationChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onLogClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Log $exerciseName", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SurfaceCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("DETAILS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        DarkInputField(
                            value = duration,
                            onValueChange = onDurationChange,
                            label = "Duration (minutes)",
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        DarkInputField(
                            value = calories,
                            onValueChange = onCaloriesChange,
                            label = "Calories Burned",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                PrimaryButton(
                    text = "Log Exercise",
                    onClick = onLogClick
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun LogExerciseScreenPreview() {
    com.fitpulse.app.AppTheme {
        LogExerciseContent(
            exerciseName = "Morning Run",
            duration = "45",
            calories = "380",
            onDurationChange = {},
            onCaloriesChange = {},
            onBackClick = {},
            onLogClick = {}
        )
    }
}
