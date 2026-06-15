package com.fitpulse.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.fitpulse.app.*
import kotlinx.coroutines.flow.firstOrNull

// ============================================================
// FITPULSE TOP BARS — Drawer + Notification Panel
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserTopBar(
    title: String,
    navController: NavController,
    showBack: Boolean = false,
    hideInbox: Boolean = false,
    onMenuClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    var showDrawer by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }

    // Side Drawer overlay
    if (showDrawer) {
        SideDrawer(navController = navController, onDismiss = { showDrawer = false })
    }

    // Notification panel overlay
    if (showNotifications) {
        NotificationPanel(navController = navController, onDismiss = { showNotifications = false })
    }

    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
            } else {
                IconButton(onClick = { showDrawer = true }) {
                    Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onBackground)
                }
            }
        },
        actions = {
            actions()
            // Direct messages (Airplane)
            if (!hideInbox) {
                IconButton(onClick = { navController.navigate("user_inbox") }) {
                    Icon(Icons.AutoMirrored.Filled.Send, "Direct Messages", tint = MaterialTheme.colorScheme.onBackground)
                }
            }
            // Notification bell with badge dot
            Box {
                IconButton(onClick = { showNotifications = true }) {
                    Icon(Icons.Default.Notifications, "Notifications", tint = MaterialTheme.colorScheme.onBackground)
                }
                // Unread dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(LimeGreen)
                        .align(Alignment.TopEnd)
                        .offset(x = (-10).dp, y = 10.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpertTopBar(title: String, navController: NavController, showBack: Boolean = false) {
    var showDrawer by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }

    if (showDrawer) {
        SideDrawer(navController = navController, onDismiss = { showDrawer = false })
    }
    if (showNotifications) {
        NotificationPanel(navController = navController, onDismiss = { showNotifications = false })
    }

    TopAppBar(
        title = {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
        },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
            } else {
                IconButton(onClick = { showDrawer = true }) {
                    Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onBackground)
                }
            }
        },
        actions = {
            Box {
                IconButton(onClick = { showNotifications = true }) {
                    Icon(Icons.Default.Notifications, "Notifications", tint = MaterialTheme.colorScheme.onBackground)
                }
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(LimeGreen).align(Alignment.TopEnd).offset(x = (-10).dp, y = 10.dp))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background
        )
    )
}

// ── SIDE DRAWER ───────────────────────────────────────────────

@Composable
fun SideDrawer(navController: NavController, onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { com.fitpulse.app.data.local.AppDatabase.getInstance(context) }
    val profile by db.userDao().getCurrentUser().collectAsState(initial = null)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Scrim — tap outside to close
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable { onDismiss() }
            )

            // Drawer panel (slides in from left)
            AnimatedVisibility(
                visible = true,
                enter = slideInHorizontally(initialOffsetX = { -it }),
                exit = slideOutHorizontally(targetOffsetX = { -it })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.78f)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
                                )
                            ),
                            shape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
                        )
                        .clickable { /* Consume clicks so scrim doesn't close */ }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 56.dp)
                            .systemBarsPadding()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Brand header
                        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(LimeGreen, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(profile?.fullName ?: "FitPulse", fontWeight = FontWeight.Black, color = DarkBg, fontSize = 18.sp)
                            }
                            Spacer(Modifier.height(6.dp))
                            val planText = if (profile?.isPremium == true) "Premium Member" else "Free Plan"
                            val planColor = if (profile?.isPremium == true) AccentOrange else MaterialTheme.colorScheme.onSurfaceVariant
                            Text(planText, style = MaterialTheme.typography.bodySmall, color = planColor, fontWeight = FontWeight.Bold)
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(0.2f), modifier = Modifier.padding(horizontal = 20.dp))
                        Spacer(Modifier.height(8.dp))

                        // Navigation items
                        DrawerItem(Icons.Default.Person, "My Profile", LimeGreen) {
                            onDismiss(); navController.navigate(Screen.Profile.route)
                        }
                        DrawerItem(Icons.Default.TrackChanges, "Goals & Targets", LimeGreen) {
                            onDismiss(); navController.navigate(Screen.GoalsTargets.route)
                        }
                        DrawerItem(Icons.Default.AutoAwesome, "AI Nutritionist", LimeGreen) {
                            onDismiss(); navController.navigate(Screen.AiNutrition.route)
                        }
                        DrawerItem(Icons.Default.Explore, "Find Experts", LimeGreen) {
                            onDismiss(); navController.navigate(Screen.ExpertList.route)
                        }

                        Spacer(Modifier.height(8.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(0.15f), modifier = Modifier.padding(horizontal = 20.dp))
                        Spacer(Modifier.height(8.dp))

                        // Settings group
                        DrawerGroupLabel("Settings")
                        DrawerItem(Icons.Default.Settings, "App Settings", AccentPurple) {
                            onDismiss(); navController.navigate(Screen.AppPreferences.route)
                        }
                        DrawerItem(Icons.Default.Notifications, "Notifications", AccentPurple) {
                            onDismiss(); navController.navigate(Screen.NotificationSettings.route)
                        }
                        DrawerItem(Icons.Default.PhoneAndroid, "Device Settings", AccentPurple) {
                            onDismiss()
                            // Open App-Specific Settings
                            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = android.net.Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                        DrawerItem(Icons.Default.WorkspacePremium, "Premium Plans", AccentOrange) {
                            onDismiss(); navController.navigate(Screen.PremiumPlans.route)
                        }

                        Spacer(Modifier.height(8.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(0.15f), modifier = Modifier.padding(horizontal = 20.dp))
                        Spacer(Modifier.height(8.dp))

                        // Legal group
                        DrawerGroupLabel("Legal & Info")
                        DrawerItem(Icons.Default.PrivacyTip, "Privacy Policy", Color.Gray) { 
                            onDismiss()
                            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://fitpulse.com/privacy")))
                        }
                        DrawerItem(Icons.Default.Gavel, "Terms of Service", Color.Gray) { 
                            onDismiss()
                            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://fitpulse.com/terms")))
                        }
                        DrawerItem(Icons.Default.Info, "About FitPulse", Color.Gray) { 
                            onDismiss()
                            android.widget.Toast.makeText(context, "FitPulse v1.0.0 - Your Smart Fitness Companion", android.widget.Toast.LENGTH_LONG).show()
                        }

                        Spacer(Modifier.height(32.dp))

                        // Version footer
                        Text(
                            text = "FitPulse v1.0.0",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(icon: ImageVector, label: String, iconColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
            modifier = Modifier.size(14.dp)
        )
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun DrawerGroupLabel(label: String) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
    )
}

// ── NOTIFICATION PANEL ────────────────────────────────────────

private data class NotifItem(val id: String, val title: String, val body: String, val icon: ImageVector, val color: Color, val time: String, var isRead: Boolean = false)

@Composable
fun NotificationPanel(navController: NavController, onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { com.fitpulse.app.data.local.AppDatabase.getInstance(context) }
    val profile by db.userDao().getCurrentUser().collectAsState(initial = null)

    var dynamicNotifs by remember { mutableStateOf<List<NotifItem>>(emptyList()) }

    LaunchedEffect(profile) {
        val todayStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        val foodLogs = db.foodDao().getLogsForDate(todayStr).firstOrNull() ?: emptyList()
        val eaten = foodLogs.sumOf { it.calories }

        dynamicNotifs = buildList {
            if (profile != null) {
                val user = profile!!
                
                if (user.streakCount > 0) {
                    add(NotifItem("1", "Streak Alert", "${user.streakCount}-day streak! Keep logging meals to maintain your streak.", Icons.Default.EmojiEvents, AccentOrange, "Now"))
                }
                
                if (user.dailyCalorieGoal > 0) {
                    add(NotifItem("2", "Daily Calories", "You've eaten ~$eaten kcal out of ${user.dailyCalorieGoal} kcal today.", Icons.Default.LocalFireDepartment, LimeGreen, "1h ago"))
                }
                
                if (user.targetWeightKg > 0 && user.weightKg != user.targetWeightKg) {
                    val diff = kotlin.math.abs(user.weightKg - user.targetWeightKg)
                    add(NotifItem("3", "Weight Goal", "You are ${String.format("%.1f", diff)}kg away from your target weight of ${user.targetWeightKg}kg.", Icons.Default.FitnessCenter, AccentPurple, "3h ago"))
                }
                
                add(NotifItem("4", "Tip of the day", "Drinking a glass of water before each meal helps metabolism.", Icons.Default.WaterDrop, AccentBlue, "4h ago"))
            } else {
                add(NotifItem("0", "Welcome to FitPulse!", "Set up your profile to start tracking your daily progress.", Icons.Default.WavingHand, LimeGreen, "Now"))
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Scrim
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.55f)).clickable { onDismiss() })

            // Panel slides from top-right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxHeight(0.72f)
                    .fillMaxWidth(0.92f)
                    .padding(top = 72.dp, end = 12.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .clickable { /* consume */ }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Notifications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                            Text("${dynamicNotifs.size} alerts for you", style = MaterialTheme.typography.labelSmall, color = LimeGreen)
                        }
                        // Settings shortcut button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(LimeGreen.copy(0.12f))
                                .clickable { onDismiss(); navController.navigate(Screen.NotificationSettings.route) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Settings, null, tint = LimeGreen, modifier = Modifier.size(16.dp))
                                Text("Settings", style = MaterialTheme.typography.labelMedium, color = LimeGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(0.15f))

                    // Notification list
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(dynamicNotifs.size) { i ->
                            val n = dynamicNotifs[i]
                            NotifCard(
                                n = n,
                                onRead = { 
                                    dynamicNotifs = dynamicNotifs.map { if (it.id == n.id) it.copy(isRead = true) else it }
                                },
                                onDelete = {
                                    dynamicNotifs = dynamicNotifs.filter { it.id != n.id }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotifCard(n: NotifItem, onRead: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (n.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(0.5f) else MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(if (n.isRead) Color.Gray.copy(0.2f) else n.color.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(n.icon, null, tint = if (n.isRead) Color.Gray else n.color, modifier = Modifier.size(22.dp))
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(n.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (n.isRead) Color.Gray else MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        Text(n.time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(n.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                if (!n.isRead) {
                    TextButton(onClick = onRead, modifier = Modifier.height(36.dp)) {
                        Text("Mark as Read", style = MaterialTheme.typography.labelMedium, color = LimeGreen)
                    }
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ── PREVIEW ───────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
fun UserTopBarPreview() {
    AppTheme(useDarkTheme = true) {
        UserTopBar(title = "Today", navController = rememberNavController())
    }
}
