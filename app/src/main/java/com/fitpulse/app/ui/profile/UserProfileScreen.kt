package com.fitpulse.app.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fitpulse.app.ui.components.*
import com.fitpulse.app.ui.navigation.*
import androidx.compose.foundation.border

@Composable
fun UserProfileScreen(navController: NavController) {
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
    val authViewModel: com.fitpulse.app.ui.viewmodels.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    
    val profile by viewModel.userProfile.collectAsState()
    var showLogoutSheet by remember { mutableStateOf(false) }
    var weightUnit by remember { mutableStateOf(securityManager.getWeightUnit()) }
    var heightUnit by remember { mutableStateOf(securityManager.getHeightUnit()) }
    
    LaunchedEffect(Unit) {
        weightUnit = securityManager.getWeightUnit()
        heightUnit = securityManager.getHeightUnit()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { UserTopBar(title = "Profile", navController = navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar + name
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(2.dp, androidx.compose.ui.graphics.Color.Green, CircleShape),
                        contentAlignment = Alignment.Center
                    ) { 
                        val imgUri = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.photoUrl?.toString() ?: profile?.profileImageUri
                        if (imgUri != null) {
                            coil.compose.AsyncImage(
                                model = imgUri,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            val initials = profile?.fullName?.split(" ")?.joinToString("") { it.take(1) }?.take(2) ?: "U"
                            Text(initials.uppercase(), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimary) 
                        }
                    }
                    Text(profile?.fullName ?: "User", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text(profile?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Stat cards
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val weightStr = if (weightUnit == "kg") "${profile?.weightKg ?: 0}kg" else "${((profile?.weightKg ?: 0f) * 2.20462).toInt()}lbs"
                    val heightStr = if (heightUnit == "cm") "${profile?.heightCm ?: 0}cm" else {
                        val totalInches = ((profile?.heightCm ?: 0f) / 2.54).toInt()
                        "${totalInches / 12}'${totalInches % 12}\""
                    }
                    val targetStr = if (weightUnit == "kg") "${profile?.targetWeightKg ?: 0}kg" else "${((profile?.targetWeightKg ?: 0f) * 2.20462).toInt()}lbs"

                    StatCard("Weight", weightStr, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    StatCard("Height", heightStr, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                    StatCard("Goal", targetStr, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                    StatCard("Cals", profile?.dailyCalorieGoal?.toString() ?: "2000", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                }
            }

            // Account section
            item { Text("ACCOUNT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    ProfileMenuItem(Icons.Default.PersonOutline, "Edit Profile") { navController.navigate("edit_profile") }
                    ProfileMenuItem(Icons.Default.TrackChanges, "Goals & Targets") { navController.navigate("goals_targets") }
                    ProfileMenuItem(Icons.Default.NotificationsNone, "Notifications") { navController.navigate("notification_settings") }
                    ProfileMenuItem(Icons.Default.Tune, "App Preferences") { navController.navigate("app_preferences") }
                    ProfileMenuItem(Icons.Default.StarOutline, "Premium") { navController.navigate("premium_plans") }
                }
            }

            item { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }

            item {
                SurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showLogoutSheet = true }
                ) {
                    Row(
                        modifier = Modifier.height(56.dp).padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Log Out", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showLogoutSheet) {
        LogoutConfirmationSheet(
            onConfirm = {
                showLogoutSheet = false
                authViewModel.logout()
                navController.navigate(Screen.Welcome.route) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onDismiss = { showLogoutSheet = false }
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun UserProfileScreenPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    com.fitpulse.app.AppTheme {
        UserProfileScreen(navController = navController)
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    SurfaceCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.height(56.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun StatCard(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    SurfaceCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
