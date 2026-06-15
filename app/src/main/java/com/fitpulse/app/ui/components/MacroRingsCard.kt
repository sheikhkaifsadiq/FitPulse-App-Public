package com.fitpulse.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MacroRingsCard(
    calories: Int,
    calorieGoal: Int,
    proteinG: Float,
    proteinGoal: Int,
    carbsG: Float,
    carbGoal: Int,
    fatG: Float,
    fatGoal: Int,
    modifier: Modifier = Modifier
) {
    val calPct = if (calorieGoal > 0) (calories.toFloat() / calorieGoal).coerceAtMost(1f) else 0f
    val proPct = if (proteinGoal > 0) (proteinG / proteinGoal).coerceAtMost(1f) else 0f
    val carPct = if (carbGoal > 0) (carbsG / carbGoal).coerceAtMost(1f) else 0f
    val fatPct = if (fatGoal > 0) (fatG / fatGoal).coerceAtMost(1f) else 0f

    var animationPlayed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    val animCal by animateFloatAsState(targetValue = if (animationPlayed) calPct else 0f, animationSpec = tween(1000), label = "calAnim")
    val animPro by animateFloatAsState(targetValue = if (animationPlayed) proPct else 0f, animationSpec = tween(1000), label = "proAnim")
    val animCar by animateFloatAsState(targetValue = if (animationPlayed) carPct else 0f, animationSpec = tween(1000), label = "carAnim")
    val animFat by animateFloatAsState(targetValue = if (animationPlayed) fatPct else 0f, animationSpec = tween(1000), label = "fatAnim")

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Main Calorie Ring
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animCal,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$calories", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "kcal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Macros
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                MacroIndicator("Protein", "${proteinG.toInt()} / $proteinGoal g", animPro, MaterialTheme.colorScheme.primary)
                MacroIndicator("Carbs", "${carbsG.toInt()} / $carbGoal g", animCar, MaterialTheme.colorScheme.secondary)
                MacroIndicator("Fat", "${fatG.toInt()} / $fatGoal g", animFat, MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Composable
fun MacroIndicator(label: String, value: String, percentage: Float, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        Canvas(modifier = Modifier.size(40.dp)) {
            val strokeWidth = 5.dp.toPx()
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * percentage,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(value, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
