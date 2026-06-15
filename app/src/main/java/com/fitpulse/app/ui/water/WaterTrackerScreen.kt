package com.fitpulse.app.ui.water

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fitpulse.app.data.local.AppDatabase
import com.fitpulse.app.security.SecurityManager
import com.fitpulse.app.ui.components.*
import com.fitpulse.app.ui.navigation.*
import com.fitpulse.app.ui.viewmodels.FitPulseViewModelFactory
import com.fitpulse.app.ui.viewmodels.WaterViewModel

@Composable
fun WaterTrackerScreen(navController: NavController) {
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
    val viewModel: WaterViewModel = viewModel(factory = factory)

    val waterLog by viewModel.waterLog.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    
    val amountInMl = waterLog?.amountInMl ?: 0
    val goalInMl = (userProfile?.waterGoal ?: 8) * 250

    val waterUnit = securityManager.getWaterUnit()
    WaterTrackerContent(
        amountInMl = amountInMl,
        goalInMl = goalInMl,
        waterUnit = waterUnit,
        onAddGlass = { viewModel.addGlass() },
        onRemoveGlass = { viewModel.removeGlass() },
        onUpdateGoal = { viewModel.updateGoal(it) },
        navController = navController
    )
}

@Composable
fun WaterTrackerContent(
    amountInMl: Int,
    goalInMl: Int,
    waterUnit: String,
    onAddGlass: () -> Unit,
    onRemoveGlass: () -> Unit,
    onUpdateGoal: (Int) -> Unit,
    navController: NavController
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { UserTopBar(title = "Water Tracker", navController = navController, showBack = true) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { GlowBlob(color = Color(0xFF0891B2), alignment = Alignment.TopEnd, size = 200.dp, opacity = 0.12f) }

            // counter display
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(200.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF0891B2), modifier = Modifier.size(40.dp))
                                Text("$amountInMl / $goalInMl", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                                Text(if (waterUnit == "ml") "mL" else "oz", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                Text("(~${if (waterUnit == "ml") "250 mL per glass" else "8 oz per glass"})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            IconButton(
                                onClick = onRemoveGlass,
                                modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Remove", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(
                                onClick = onAddGlass,
                                modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                }
            }

            // Daily goal stepper
            item {
                SurfaceCard {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Daily Goal", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        
                        val goalInGlasses = goalInMl / 250
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { if (goalInGlasses > 1) onUpdateGoal(goalInGlasses - 1) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Text("$goalInMl mL", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
                            IconButton(onClick = { if (goalInGlasses < 30) onUpdateGoal(goalInGlasses + 1) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("(${goalInGlasses} glasses)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun WaterTrackerScreenPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    com.fitpulse.app.AppTheme {
        WaterTrackerContent(
            amountInMl = 1250,
            goalInMl = 3000,
            waterUnit = "ml",
            onAddGlass = {},
            onRemoveGlass = {},
            onUpdateGoal = {},
            navController = navController
        )
    }
}
