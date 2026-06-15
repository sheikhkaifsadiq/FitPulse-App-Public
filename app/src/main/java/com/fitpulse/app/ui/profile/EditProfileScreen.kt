package com.fitpulse.app.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fitpulse.app.ui.components.*

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

import com.fitpulse.app.ui.viewmodels.ProfileViewModel
import com.fitpulse.app.data.local.entities.UserEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val database = remember { com.fitpulse.app.data.local.AppDatabase.getInstance(context) }
    val securityManager = remember { com.fitpulse.app.security.SecurityManager(context) }
    val factory = remember { 
        com.fitpulse.app.ui.viewmodels.FitPulseViewModelFactory(
            database.userDao(), 
            database.foodDao(), 
            database.waterDao(),
            database.exerciseDao(),
            database.weightDao(),
            securityManager
        ) 
    }
    val viewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val user by viewModel.userProfile.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var heightCmInput by remember { mutableStateOf("") }
    var heightFeetInput by remember { mutableStateOf("5") }
    var heightInchesInput by remember { mutableStateOf("10") }
    var gender by remember { mutableStateOf("") }
    var heightUnit by remember { mutableStateOf(securityManager.getHeightUnit()) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var dateOfBirth by remember { mutableStateOf("12 Jan 2000") }
    var isDataLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        user?.let {
            if (!isDataLoaded) {
                fullName = it.fullName
                email = it.email
                dateOfBirth = it.dateOfBirth.ifBlank { "12 Jan 2000" }
                
                heightCmInput = it.heightCm.toInt().toString()
                val totalInches = (it.heightCm / 2.54f).toInt()
                heightFeetInput = (totalInches / 12).toString()
                heightInchesInput = (totalInches % 12).toString()
                
                gender = it.gender
                profileImageUri = it.profileImageUri?.let { uri -> Uri.parse(uri) }
                isDataLoaded = true
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    if (!isDataLoaded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    TextButton(onClick = { 
                        user?.let {
                            val finalHeightCm = if (heightUnit == "cm") {
                                heightCmInput.toFloatOrNull() ?: it.heightCm
                            } else {
                                val f = heightFeetInput.toFloatOrNull() ?: 0f
                                val i = heightInchesInput.toFloatOrNull() ?: 0f
                                ((f * 12) + i) * 2.54f
                            }
                            
                            var newAge = it.age
                            try {
                                val parts = dateOfBirth.split(" ")
                                val year = parts.last().toInt()
                                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                                newAge = currentYear - year
                            } catch(e: Exception) {}

                            viewModel.updateProfile(it.copy(
                                fullName = fullName,
                                email = email,
                                heightCm = finalHeightCm,
                                gender = gender,
                                profileImageUri = profileImageUri?.toString(),
                                dateOfBirth = dateOfBirth,
                                age = newAge
                            )) {
                                kotlinx.coroutines.MainScope().launch {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }) {
                        Text("Save", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        @OptIn(ExperimentalMaterial3Api::class)
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh(context) },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar with camera button
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(80.dp)) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileImageUri != null) {
                                AsyncImage(
                                    model = profileImageUri,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = fullName.take(2).uppercase().ifBlank { "SK" },
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                                .align(Alignment.BottomEnd)
                                .clickable { launcher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Change Picture",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            item { DarkInputField(value = fullName, onValueChange = { fullName = it }, label = "Full Name") }
            item {
                DarkInputField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    trailingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp)) }
                )
            }
            item {
                SurfaceCard {
                    Row(
                        modifier = Modifier.height(56.dp).padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Cake, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Date of Birth", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            val calendar = java.util.Calendar.getInstance()
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val monthStr = java.text.DateFormatSymbols().shortMonths[month]
                                    dateOfBirth = "$day $monthStr $year"
                                },
                                calendar.get(java.util.Calendar.YEAR),
                                calendar.get(java.util.Calendar.MONTH),
                                calendar.get(java.util.Calendar.DAY_OF_MONTH)
                            ).show()
                        }) { 
                            Text(dateOfBirth, color = MaterialTheme.colorScheme.primary) 
                        }
                    }
                }
            }
            item {
                SurfaceCard {
                    Row(
                        modifier = Modifier.wrapContentHeight().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Height, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Height", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        if (heightUnit == "cm") {
                            var cmExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.width(80.dp)) {
                                OutlinedButton(onClick = { cmExpanded = true }, contentPadding = PaddingValues(0.dp)) { Text("$heightCmInput cm", color = MaterialTheme.colorScheme.onSurface) }
                                DropdownMenu(expanded = cmExpanded, onDismissRequest = { cmExpanded = false }, modifier = Modifier.heightIn(max = 200.dp)) {
                                    (100..250).forEach { c -> DropdownMenuItem(text = { Text("$c cm") }, onClick = { heightCmInput = c.toString(); cmExpanded = false }) }
                                }
                            }
                        } else {
                            var ftExpanded by remember { mutableStateOf(false) }
                            var inExpanded by remember { mutableStateOf(false) }
                            
                            Box(modifier = Modifier.width(65.dp)) {
                                OutlinedButton(onClick = { ftExpanded = true }, contentPadding = PaddingValues(0.dp)) { Text("$heightFeetInput ft", color = MaterialTheme.colorScheme.onSurface) }
                                DropdownMenu(expanded = ftExpanded, onDismissRequest = { ftExpanded = false }, modifier = Modifier.heightIn(max = 200.dp)) {
                                    (3..8).forEach { f -> DropdownMenuItem(text = { Text("$f ft") }, onClick = { heightFeetInput = f.toString(); ftExpanded = false }) }
                                }
                            }
                            Spacer(Modifier.width(4.dp))
                            Box(modifier = Modifier.width(65.dp)) {
                                OutlinedButton(onClick = { inExpanded = true }, contentPadding = PaddingValues(0.dp)) { Text("$heightInchesInput in", color = MaterialTheme.colorScheme.onSurface) }
                                DropdownMenu(expanded = inExpanded, onDismissRequest = { inExpanded = false }, modifier = Modifier.heightIn(max = 200.dp)) {
                                    (0..11).forEach { i -> DropdownMenuItem(text = { Text("$i in") }, onClick = { heightInchesInput = i.toString(); inExpanded = false }) }
                                }
                            }
                        }
                        Spacer(Modifier.width(4.dp))
                        listOf("cm", "ft").forEach { unit ->
                            val sel = unit == heightUnit
                            FilterChip(
                                selected = sel,
                                onClick = { 
                                    heightUnit = unit 
                                    securityManager.setHeightUnit(unit)
                                },
                                label = { Text(unit, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant, labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = FilterChipDefaults.filterChipBorder(borderColor = MaterialTheme.colorScheme.outlineVariant, selectedBorderColor = MaterialTheme.colorScheme.primary, enabled = true, selected = sel)
                            )
                        }
                    }
                }
            }
            item {
                SurfaceCard {
                    Row(
                        modifier = Modifier.height(56.dp).padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Wc, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Biological Sex", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        listOf("Male", "Female").forEach { g ->
                            val sel = g == gender
                            FilterChip(
                                selected = sel, onClick = { gender = g },
                                label = { Text(g, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant, labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = FilterChipDefaults.filterChipBorder(borderColor = MaterialTheme.colorScheme.outlineVariant, selectedBorderColor = MaterialTheme.colorScheme.primary, enabled = true, selected = sel)
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}
}
