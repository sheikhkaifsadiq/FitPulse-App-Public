package com.fitpulse.app.ui.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.fitpulse.app.ui.navigation.Screen
import com.fitpulse.app.ui.viewmodels.ExerciseViewModel
import com.fitpulse.app.ui.viewmodels.FitPulseViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSearchScreen(navController: NavController, category: String) {
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

    LaunchedEffect(category) {
        viewModel.updateCategory(category)
    }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val recentExercises by viewModel.recentExercises.collectAsState()

    ExerciseSearchContent(
        query = searchQuery,
        onQueryChange = { viewModel.updateSearchQuery(it) },
        category = category,
        searchResults = searchResults,
        recentExercises = recentExercises,
        onExerciseClick = { name, cat -> navController.navigate(Screen.LogExercise.createRoute(name, cat)) },
        onBackClick = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSearchContent(
    query: String,
    onQueryChange: (String) -> Unit,
    category: String,
    searchResults: List<com.fitpulse.app.data.repository.ExerciseModel>,
    recentExercises: List<com.fitpulse.app.data.local.entities.ExerciseLogEntity>,
    onExerciseClick: (String, String) -> Unit,
    onBackClick: () -> Unit
) {
    val defaultSuggestions = when (category) {
        "Strength" -> listOf("Pushups", "Pullups", "Squats", "Deadlifts", "Bench Press")
        "Cardio" -> listOf("Running", "Cycling", "Swimming", "Jump Rope", "Rowing")
        else -> listOf("Basketball", "Tennis", "Soccer", "Yoga", "Pilates")
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Search $category", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search exercises...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }

                if (query.isBlank()) {
                    val recentForCategory = recentExercises.filter { it.type == category }
                    if (recentForCategory.isNotEmpty()) {
                        item {
                            Text("RECENT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        items(recentForCategory) { exercise ->
                            ExerciseResultItem(
                                name = exercise.name,
                                type = exercise.type,
                                onClick = { onExerciseClick(exercise.name, exercise.type) }
                            )
                        }
                    }
                    
                    item {
                        Text("POPULAR IN ${category.uppercase()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(defaultSuggestions) { suggestion ->
                        ExerciseResultItem(
                            name = suggestion,
                            type = category,
                            onClick = { onExerciseClick(suggestion, category) }
                        )
                    }
                } else if (searchResults.isNotEmpty()) {
                    val filteredResults = searchResults.filter { it.type == category }
                    item {
                        Text("RESULTS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    if (filteredResults.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No $category exercises found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(filteredResults) { model ->
                            ExerciseResultItem(
                                name = model.name,
                                type = model.type,
                                onClick = { onExerciseClick(model.name, model.type) }
                            )
                        }
                    }
                } else {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No exercises found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun ExerciseResultItem(name: String, type: String, onClick: () -> Unit) {
    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val icon = if(type == "Strength") Icons.Default.FitnessCenter else Icons.AutoMirrored.Filled.DirectionsRun
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ExerciseSearchScreenPreview() {
    com.fitpulse.app.AppTheme {
        ExerciseSearchContent(
            query = "",
            onQueryChange = {},
            category = "Strength",
            searchResults = listOf(
                com.fitpulse.app.data.repository.ExerciseModel("Running", "Cardio", 9.8)
            ),
            recentExercises = emptyList(),
            onExerciseClick = { _, _ -> },
            onBackClick = {}
        )
    }
}
