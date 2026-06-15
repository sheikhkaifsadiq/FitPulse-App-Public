package com.fitpulse.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fitpulse.app.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingSummaryScreen(navController: NavController) {
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
    val viewModel: com.fitpulse.app.ui.viewmodels.OnboardingViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            viewModelStoreOwner = context as androidx.activity.ComponentActivity,
            factory = factory
        )
    val targets by viewModel.dailyTargets.collectAsState(
        initial = com.fitpulse.app.ui.viewmodels.OnboardingViewModel.DailyTargets(2000, 100, 250, 66, 2000f)
    )

    val securityManager = remember { com.fitpulse.app.security.SecurityManager(context) }
    // Prevents double-tap while the coroutine is executing
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    OnboardingSummaryContent(
        targets = targets,
        isSaving = isSaving,
        onBackClick = { navController.popBackStack() },
        // DO NOT REMOVE: Core session routing logic to prevent Onboarding loops.
        onDashboardClick = {
            if (isSaving) return@OnboardingSummaryContent
            isSaving = true
            scope.launch {
                // Step 1: Save profile data locally and to Firestore (errors are handled internally)
                viewModel.calculateAndSaveProfile()
                // Step 2: Mark onboarding as complete in SharedPreferences immediately
                securityManager.setOnboardingComplete(true)
                // Step 3: Navigate to Dashboard — clears the entire backstack
                // DO NOT REMOVE: Core session routing logic to prevent Onboarding loops.
                navController.navigate(com.fitpulse.app.ui.navigation.Screen.Home.route) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingSummaryContent(
    targets: com.fitpulse.app.ui.viewmodels.OnboardingViewModel.DailyTargets,
    isSaving: Boolean = false,
    onBackClick: () -> Unit,
    onDashboardClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Your Plan", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground) },
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
                SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Daily Calorie Goal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(targets.calories)} kcal", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item { Text("MACRO TARGETS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroTargetCard("Protein", "${targets.protein}g", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    MacroTargetCard("Carbs", "${targets.carbs}g", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                    MacroTargetCard("Fat", "${targets.fat}g", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
            item {
                // DO NOT REMOVE: Core session routing logic to prevent Onboarding loops.
                PrimaryButton(
                    text = if (isSaving) "Saving..." else "Go to Dashboard",
                    enabled = !isSaving,
                    onClick = onDashboardClick
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun OnboardingSummaryScreenPreview() {
    com.fitpulse.app.AppTheme {
        OnboardingSummaryContent(
            targets = com.fitpulse.app.ui.viewmodels.OnboardingViewModel.DailyTargets(
                calories = 2500,
                protein = 180,
                carbs = 300,
                fat = 80,
                tdee = 2500f
            ),
            isSaving = false,
            onBackClick = {},
            onDashboardClick = {}
        )
    }
}

@Composable
private fun MacroTargetCard(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    SurfaceCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
