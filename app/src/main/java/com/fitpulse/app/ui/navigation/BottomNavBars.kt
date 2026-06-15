package com.fitpulse.app.ui.navigation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fitpulse.app.*

// ============================================================
// FITPULSE FLOATING NAV BAR — Glassmorphic + Lime Accent
// Features: Frosted glass pill, scale animation, glow indicator,
//           animated LimeGreen dot, blur backdrop
// ============================================================

data class NavItem(val route: String, val icon: ImageVector, val label: String, val isCenter: Boolean = false)

@Composable
fun MainBottomNav(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val items = listOf(
        NavItem(Screen.ExpertList.route, Icons.Default.SupportAgent, "Experts"),
        NavItem(Screen.FoodDiary.route, Icons.Default.Book, "Diary"),
        NavItem(Screen.Home.route, Icons.Default.Home, "Home", isCenter = true),
        NavItem(Screen.Progress.route, Icons.Default.Insights, "Progress"),
        NavItem(Screen.ExerciseLog.route, Icons.Default.FitnessCenter, "Activity"),
    )

    FloatingNavBar(items = items, currentRoute = currentRoute) { route ->
        navController.navigate(route) {
            popUpTo(Screen.Home.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}

@Composable
fun ExpertBottomNav(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val items = listOf(
        NavItem(Screen.ExpertDashboard.route, Icons.Default.SpaceDashboard, "Dashboard"),
        NavItem(Screen.ClientList.route, Icons.Default.Group, "Clients"),
        NavItem(Screen.ExpertInbox.route, Icons.AutoMirrored.Filled.Chat, "Inbox", isCenter = true),
        NavItem(Screen.EarningsDashboard.route, Icons.Default.AttachMoney, "Earnings"),
        NavItem(Screen.ExpertOwnProfile.route, Icons.Default.Settings, "Settings"),
    )

    FloatingNavBar(items = items, currentRoute = currentRoute) { route ->
        navController.navigate(route) {
            popUpTo(Screen.ExpertDashboard.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}

// ── CORE FLOATING NAV BAR ────────────────────────────────────

@Composable
fun FloatingNavBar(
    items: List<NavItem>,
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // ── KEY FIX: respect Android nav buttons / gesture bar ──
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow blur layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(50))
                .background(LimeGreen.copy(alpha = 0.08f))
                .blur(24.dp)
        )

        // Main glass pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 32.dp,
                    shape = RoundedCornerShape(50),
                    ambientColor = LimeGreen.copy(alpha = 0.25f),
                    spotColor = LimeGreen.copy(alpha = 0.35f)
                )
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = currentRoute == item.route
                    if (item.isCenter) {
                        CenterNavButton(item = item, selected = selected, modifier = Modifier.weight(1f), onClick = { onNavigate(item.route) })
                    } else {
                        RegularNavButton(item = item, selected = selected, modifier = Modifier.weight(1f), onClick = { onNavigate(item.route) })
                    }
                }
            }
        }
    }
}

// ── REGULAR NAV BUTTON ───────────────────────────────────────

@Composable
private fun RegularNavButton(item: NavItem, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    // Scale animation on select
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )
    val offsetY = 0f // No vertical bounce to keep icons centered

    Column(
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Selected glow capsule behind icon
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 32.dp)
                        .clip(RoundedCornerShape(50))
                        .background(LimeGreen.copy(alpha = 0.18f))
                )
            }
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (selected) LimeGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(22.dp)
                    .scale(scale)
                    .offset(y = offsetY.dp)
            )
        }
        // Dots only — no reserved label space so icons stay centred
        if (selected) {
            Spacer(Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(LimeGreen)
            )
        }
    }
}

// ── CENTER AI / ACTION BUTTON ────────────────────────────────

@Composable
private fun CenterNavButton(item: NavItem, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    // Pulsing glow ring animation
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "centerScale"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp), // Removed negative offset to keep it centered
            contentAlignment = Alignment.Center
        ) {
            // Outer animated glow ring
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .scale(glowScale)
                    .clip(CircleShape)
                    .background(LimeGreen.copy(alpha = glowAlpha * 0.25f))
            )
            // Button body
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(scale)
                    .shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        ambientColor = LimeGreen.copy(alpha = 0.6f),
                        spotColor = LimeGreen.copy(alpha = 0.8f)
                    )
                    .clip(CircleShape)
                    .background(
                        if (selected)
                            Brush.radialGradient(listOf(Color.White.copy(0.2f), LimeGreen))
                        else
                            Brush.radialGradient(listOf(LimeGreen.copy(0.9f), LimeGreen))
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = DarkBg,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ── PREVIEWS ─────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F, name = "FloatingNav — Dark")
@Composable
fun FloatingNavPreviewDark() {
    AppTheme(useDarkTheme = true) {
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF0F0F0F))) {
            val fakeNav = rememberNavController()
            MainBottomNav(fakeNav)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F4F4, name = "FloatingNav — Light")
@Composable
fun FloatingNavPreviewLight() {
    AppTheme(useDarkTheme = false) {
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF4F4F4))) {
            val fakeNav = rememberNavController()
            MainBottomNav(fakeNav)
        }
    }
}
