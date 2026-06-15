package com.fitpulse.app.ui.diary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fitpulse.app.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomFoodScreen(navController: NavController) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Create Custom Food", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground) },
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
            item { DarkInputField(label = "Brand Name (Optional)") }
            item { DarkInputField(label = "Food Description *") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DarkInputField(label = "Serving Size", modifier = Modifier.weight(1f))
                    DarkInputField(label = "Unit (e.g., g, ml)", modifier = Modifier.weight(1f))
                }
            }
            item { Text("NUTRITION FACTS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { DarkInputField(label = "Calories (kcal) *") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DarkInputField(label = "Carbs (g) *", modifier = Modifier.weight(1f))
                    DarkInputField(label = "Protein (g) *", modifier = Modifier.weight(1f))
                    DarkInputField(label = "Fat (g) *", modifier = Modifier.weight(1f))
                }
            }
            item { PrimaryButton(text = "Save Custom Food", onClick = { navController.popBackStack() }) }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CustomFoodScreenPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    com.fitpulse.app.AppTheme {
        CustomFoodScreen(navController = navController)
    }
}
