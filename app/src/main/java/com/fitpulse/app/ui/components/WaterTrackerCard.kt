package com.fitpulse.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitpulse.app.AccentBlue

@Composable
fun WaterTrackerCard(
    amountInMl: Int,
    dailyGoalMl: Int = 2000,
    onAddWater: () -> Unit,
    onRemoveWater: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (amountInMl.toFloat() / dailyGoalMl.toFloat()).coerceIn(0f, 1f)
    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationPlayed = true }
    
    val animProgress by animateFloatAsState(
        targetValue = if (animationPlayed) progress else 0f, 
        animationSpec = tween(1000), 
        label = "waterAnim"
    )

    SurfaceCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("HYDRATION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                    Text("$amountInMl / $dailyGoalMl mL", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                }
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(AccentBlue.copy(0.15f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(24.dp))
                }
            }

            ModernProgressBar(animProgress, color = AccentBlue)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                // Undo Button
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(enabled = amountInMl > 0) { onRemoveWater() }
                        .padding(12.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Undo", tint = if (amountInMl > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                }

                // Add Button
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .background(AccentBlue)
                        .clickable { onAddWater() }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Add Water", tint = Color.White, modifier = Modifier.size(18.dp))
                        Text("250 mL", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
