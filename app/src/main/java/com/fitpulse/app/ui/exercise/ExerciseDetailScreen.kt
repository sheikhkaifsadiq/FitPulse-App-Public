package com.fitpulse.app.ui.exercise

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fitpulse.app.data.local.AppDatabase
import com.fitpulse.app.security.SecurityManager
import com.fitpulse.app.ui.components.PrimaryButton
import com.fitpulse.app.ui.components.ProfileMetricInput
import com.fitpulse.app.ui.components.SurfaceCard
import androidx.compose.material.icons.filled.AutoAwesome
import com.fitpulse.app.ui.viewmodels.ExerciseViewModel
import com.fitpulse.app.ui.viewmodels.FitPulseViewModelFactory
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    navController: NavController,
    exerciseName: String,
    category: String
) {
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
    val viewModel: ExerciseViewModel = viewModel(factory = factory)

    val aiGuide by viewModel.aiGuide.collectAsState()
    val userWeight by viewModel.userWeight.collectAsState()
    // UI handles single-event state transitions driven by the ViewModel's Channel
    var isCalculating by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    var inputText1 by remember { mutableStateOf("") }
    var inputText2 by remember { mutableStateOf("") }
    
    val label1 = if (category == "Strength") "Sets" else "Duration"
    val label2 = when (category) {
        "Strength" -> "Reps"
        "Cardio" -> "Distance"
        else -> ""
    }
    val unit1 = if (category == "Strength") "" else "minutes"
    val unit2 = when (category) {
        "Strength" -> ""
        "Cardio" -> "km"
        else -> ""
    }
    
    val showSecondInput = category != "Sports"
    
    val motivationalQuotes = listOf(
        "Push your pace!",
        "Make every rep count!",
        "Every session gets you closer!",
        "Embrace the burn, earn the gain!"
    )
    val quote = remember { motivationalQuotes.random() }

    LaunchedEffect(exerciseName, category) {
        viewModel.fetchAiGuideForExercise(exerciseName, category)
    }

    // Observe the ViewModel's event channel to drive side effects safely
    LaunchedEffect(Unit) {
        viewModel.saveEvents.collect { event ->
            when (event) {
                is com.fitpulse.app.utils.ApiResult.Loading -> {
                    isCalculating = true
                }
                is com.fitpulse.app.utils.ApiResult.Success -> {
                    isCalculating = false
                    navController.popBackStack()
                    navController.popBackStack()
                }
                is com.fitpulse.app.utils.ApiResult.Error -> {
                    isCalculating = false
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Log $exerciseName", color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            Text(
                text = "Track your $exerciseName",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = quote,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // AI Guide Card
            SurfaceCard(bg = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.AutoAwesome, 
                        contentDescription = "AI Guide", 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp).padding(top = 2.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("AI Measurement Guide", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        if (aiGuide == null) {
                            Text("Fetching guide...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        } else {
                            Text(aiGuide ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            ProfileMetricInput(
                label = label1,
                value = inputText1,
                onValueChange = { inputText1 = it },
                unit = unit1,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (showSecondInput) {
                ProfileMetricInput(
                    label = label2,
                    value = inputText2,
                    onValueChange = { inputText2 = it },
                    unit = unit2,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(Modifier.weight(1f))

            if (isCalculating) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                PrimaryButton(
                    text = "Calculate & Add to Log",
                    onClick = {
                        val input1Valid = inputText1.isNotBlank()
                        val input2Valid = !showSecondInput || inputText2.isNotBlank()
                        if (input1Valid && input2Valid && !isCalculating) {
                            val fullInput = when (category) {
                                "Strength" -> "$inputText1 sets of $inputText2 reps of $exerciseName"
                                "Cardio" -> "$inputText1 $unit1 and $inputText2 $unit2 of $exerciseName"
                                else -> "$inputText1 $unit1 of $exerciseName"
                            }
                            
                            val duration = if (category == "Strength") {
                                val sets = inputText1.toIntOrNull() ?: 1
                                val reps = inputText2.toIntOrNull() ?: 10
                                val totalSeconds = (sets * reps * 3) + ((sets - 1) * 60)
                                Math.max(1, totalSeconds / 60)
                            } else {
                                inputText1.toIntOrNull() ?: 30
                            }
                            
                            viewModel.calculateAndSaveExerciseEvent(fullInput, category, duration)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun ExerciseDetailScreenPreview() {
    androidx.compose.material3.MaterialTheme {
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Text("ExerciseDetailScreen Preview Placeholder")
        }
    }
}
