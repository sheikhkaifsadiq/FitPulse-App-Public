package com.fitpulse.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fitpulse.app.ui.components.*
import com.fitpulse.app.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingBodyScreen(navController: NavController) {
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

    val securityManager = remember { com.fitpulse.app.security.SecurityManager(context) }
    var gender by remember { mutableStateOf(state.gender.takeIf { it.isNotEmpty() } ?: "Male") }
    
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    var dobDay by remember { mutableStateOf("01") }
    var dobMonth by remember { mutableStateOf("01") }
    var dobYear by remember { mutableStateOf((currentYear - 25).toString()) }
    
    var heightFeet by remember { mutableStateOf("5") }
    var heightInches by remember { mutableStateOf("7") }
    
    var weightWhole by remember { mutableStateOf("70") }
    var weightDecimal by remember { mutableStateOf(".0") }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("About You", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
            item { Text("Biological Sex", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Male", "Female").forEach { g ->
                        val isSelected = gender == g
                        FilterChip(
                            selected = isSelected,
                            onClick = { gender = g },
                            label = { Text(g, style = MaterialTheme.typography.bodyMedium) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer, selectedLabelColor = MaterialTheme.colorScheme.primary, containerColor = MaterialTheme.colorScheme.surface, labelColor = MaterialTheme.colorScheme.onSurfaceVariant),
                            border = FilterChipDefaults.filterChipBorder(borderColor = MaterialTheme.colorScheme.outlineVariant, selectedBorderColor = MaterialTheme.colorScheme.primary, enabled = true, selected = isSelected)
                        )
                    }
                }
            }
            
            item { Text("Date of Birth", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item {
                DateOfBirthSelector(
                    selectedDay = dobDay,
                    selectedMonth = dobMonth,
                    selectedYear = dobYear,
                    onDateChanged = { d, m, y -> 
                        dobDay = d
                        dobMonth = m
                        dobYear = y
                    }
                )
            }
            item { Text("Height", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item {
                HeightSelector(
                    selectedFeet = heightFeet,
                    selectedInches = heightInches,
                    onHeightChanged = { f, i ->
                        heightFeet = f
                        heightInches = i
                    }
                )
            }
            item { Text("Current Weight", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item {
                WeightSelector(
                    selectedWhole = weightWhole,
                    selectedDecimal = weightDecimal,
                    onWeightChanged = { w, d ->
                        weightWhole = w
                        weightDecimal = d
                    }
                )
            }
            
            item { Spacer(Modifier.height(24.dp)) }
            item { 
                PrimaryButton(
                    text = "Next", 
                    onClick = { 
                        val dobAge = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) - (dobYear.toIntOrNull() ?: 2000)
                        val totalInches = (heightFeet.toFloatOrNull() ?: 5f) * 12 + (heightInches.toFloatOrNull() ?: 7f)
                        val heightInCm = totalInches * 2.54f
                        val weightInKg = (weightWhole + weightDecimal).toFloatOrNull() ?: 70f
                        
                        viewModel.updateBodyMetrics(
                            age = dobAge,
                            gender = gender,
                            weightKg = weightInKg,
                            heightCm = heightInCm
                        )
                        navController.navigate(Screen.OnboardingTarget.route) 
                    } 
                ) 
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun OnboardingBodyScreenPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    com.fitpulse.app.AppTheme {
        OnboardingBodyScreen(navController = navController)
    }
}
