package com.fitpulse.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitpulse.app.utils.DailySummary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun WeeklyTrendChart(
    weeklyData: List<DailySummary>,
    modifier: Modifier = Modifier
) {
    if (weeklyData.isEmpty()) return

    val targetColor = MaterialTheme.colorScheme.onSurfaceVariant
    val underGoalColor = MaterialTheme.colorScheme.primary
    val overGoalColor = MaterialTheme.colorScheme.error

    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationPlayed = true }
    val animHeight by animateFloatAsState(targetValue = if (animationPlayed) 1f else 0f, animationSpec = tween(1000), label = "chartAnim")

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Weekly Trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(16.dp))
            
            val sortedData = weeklyData.sortedBy { it.date }
            
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val maxGoal = sortedData.maxOfOrNull { it.calorieGoal }?.toFloat()?.takeIf { it > 0 } ?: 2000f
                    val maxEaten = sortedData.maxOfOrNull { it.caloriesEaten }?.toFloat() ?: 0f
                    val yMax = maxOf(maxGoal, maxEaten, 100f) * 1.15f // Add 15% visual buffer

                    val barWidth = 24.dp.toPx()
                    val totalSpacing = size.width - (barWidth * 7)
                    val spacing = totalSpacing / 6

                    // Draw Target Line
                    val goalY = size.height - (size.height * (maxGoal / yMax))
                    drawLine(
                        color = targetColor,
                        start = Offset(0f, goalY),
                        end = Offset(size.width, goalY),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // Draw Bars
                    sortedData.forEachIndexed { index, day ->
                        val x = index * (barWidth + spacing)
                        
                        val targetBarHeight = size.height * (day.caloriesEaten / yMax)
                        val barHeight = targetBarHeight * animHeight
                        val y = size.height - barHeight
                        
                        val barColor = if (day.caloriesEaten > day.calorieGoal && day.calorieGoal > 0) overGoalColor else underGoalColor

                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            // Labels
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                sortedData.forEach { day ->
                    val label = try {
                        val date = LocalDate.parse(day.date)
                        date.format(DateTimeFormatter.ofPattern("EE"))
                    } catch (e: DateTimeParseException) {
                        "?"
                    }
                    Text(
                        text = label, 
                        fontSize = 10.sp, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
