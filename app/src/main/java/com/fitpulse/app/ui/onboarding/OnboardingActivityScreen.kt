package com.fitpulse.app.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fitpulse.app.ui.components.PrimaryButton
import com.fitpulse.app.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingActivityScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = remember { com.fitpulse.app.data.local.AppDatabase.getInstance(context) }
    val factory = remember { 
        com.fitpulse.app.ui.viewmodels.FitPulseViewModelFactory(
            userDao = database.userDao(), 
            foodDao = database.foodDao(), 
            waterDao = database.waterDao(),
            exerciseDao = database.exerciseDao(),
            weightDao = database.weightDao(),
            securityManager = com.fitpulse.app.security.SecurityManager(context)
        ) 
    }
    val viewModel: com.fitpulse.app.ui.viewmodels.OnboardingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        viewModelStoreOwner = context as androidx.activity.ComponentActivity,
        factory = factory
    )
    val state by viewModel.onboardingState.collectAsState()

    OnboardingActivityContent(
        selectedActivity = state.activityLevel.takeIf { it.isNotEmpty() },
        onActivitySelected = { activity ->
            viewModel.updateActivityLevel(activity)
            navController.navigate(Screen.OnboardingBody.route)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingActivityContent(
    selectedActivity: String?,
    onActivitySelected: (String) -> Unit
) {
    var currentSelection by remember { mutableStateOf(selectedActivity) }
    val levels = listOf(
        "Sedentary" to "Spend most of the day sitting (e.g., office job).",
        "Lightly Active" to "Spend a good part of the day on your feet.",
        "Moderately Active" to "Active job or exercise 3-5 days/week.",
        "Very Active" to "Heavy physical job or intense exercise 6-7 days/week."
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Activity Level") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text(
                text = "How active are you on a daily basis?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(levels) { (level, desc) ->
                    ActivityCard(
                        title = level,
                        description = desc,
                        isSelected = currentSelection == level,
                        onClick = { currentSelection = level }
                    )
                }
            }

            PrimaryButton(
                text = "Next",
                enabled = currentSelection != null,
                onClick = { onActivitySelected(currentSelection!!) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun OnboardingActivityScreenPreview() {
    com.fitpulse.app.AppTheme {
        OnboardingActivityContent(
            selectedActivity = "Moderately Active",
            onActivitySelected = {}
        )
    }
}
