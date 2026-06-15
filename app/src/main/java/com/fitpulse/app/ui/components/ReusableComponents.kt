package com.fitpulse.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitpulse.app.*
import kotlin.math.cos
import kotlin.math.sin

// ============================================================
// FITPULSE PREMIUM COMPONENT LIBRARY v2.0
// Neo-Brutalist + Glassmorphism Design System
// Lime Green (#BEF264) Primary Accent — Zero Whitespace
// ============================================================

// ── CARD PRIMITIVES ─────────────────────────────────────────

@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    bg: Color = MaterialTheme.colorScheme.surface,
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    onClick: (() -> Unit)? = null,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) modifier.clickable { onClick() } else modifier
    Card(
        modifier = cardModifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = bg),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) { Column { content() } }
}

@Composable
fun DarkAccentCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardMod = if (onClick != null) modifier.clickable { onClick() } else modifier
    Card(
        modifier = cardMod,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) { Column { content() } }
}

@Composable
fun LimeCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardMod = if (onClick != null) modifier.clickable { onClick() } else modifier
    Card(
        modifier = cardMod,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LimeGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) { Column { content() } }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    opacity: Float = 0.15f,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = opacity),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        tonalElevation = 0.dp
    ) { Column(modifier = Modifier.padding(16.dp)) { content() } }
}

// ── GLOW / AMBIENT ──────────────────────────────────────────

@Composable
fun GlowBlob(color: Color, alignment: Alignment, size: Dp, opacity: Float) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = alignment) {
        Box(
            modifier = Modifier
                .size(size)
                .background(color.copy(alpha = opacity), shape = RoundedCornerShape(50))
        )
    }
}

// ── BUTTONS ─────────────────────────────────────────────────

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) { Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
}

@Composable
fun LimeButton(
    text: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = LimeGreen,
            contentColor = Color(0xFF0F0F0F)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) { Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0F0F0F)) }
}

@Composable
fun ArrowButton(onClick: () -> Unit, color: Color = LimeGreen) {
    Box(
        modifier = Modifier.size(36.dp).clip(CircleShape).background(color).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = "Go", tint = Color(0xFF0F0F0F), modifier = Modifier.size(22.dp))
    }
}

// ── INPUT ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkInputField(
    value: String = "",
    onValueChange: (String) -> Unit = {},
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: String = "",
    singleLine: Boolean = true,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    readOnly: Boolean = false,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        leadingIcon = leadingIcon, trailingIcon = trailingIcon,
        shape = RoundedCornerShape(16.dp),
        singleLine = singleLine, maxLines = 1, readOnly = readOnly,
        isError = isError,
        keyboardOptions = keyboardOptions, visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
            errorBorderColor     = MaterialTheme.colorScheme.error,
            focusedContainerColor   = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor   = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor   = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            errorLabelColor     = MaterialTheme.colorScheme.error,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitInputField(
    label: String,
    value: String,
    unit: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
) {
    OutlinedTextField(
        value = value, 
        onValueChange = onValueChange,
        label = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "($unit)", 
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true, maxLines = 1,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMetricInput(
    label: String,
    value: String,
    unit: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true, maxLines = 1,
            keyboardOptions = keyboardOptions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.primary
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

// ── LABELS & CHIPS ──────────────────────────────────────────

@Composable
fun SectionLabel(title: String, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }
        }
    }
}

@Composable
fun StatChip(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, tint: Color = LimeGreen, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (icon != null) Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Column {
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val textColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.5f))
    Surface(modifier = Modifier.clickable { onClick() }, color = bg, shape = RoundedCornerShape(50), border = border) {
        Text(label, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge, color = textColor, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

// ── PROGRESS BAR ─────────────────────────────────────────────

@Composable
fun ModernProgressBar(progress: Float, modifier: Modifier = Modifier, color: Color = LimeGreen, trackColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), height: Dp = 10.dp) {
    Box(modifier = modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(50)).background(trackColor)) {
        Box(modifier = Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(50)).background(color))
    }
}

// ── DONUT / RING CHART ───────────────────────────────────────

/**
 * DonutChart — Canvas-drawn ring chart for macro distribution.
 * Used in HomeScreen, FoodDiary, OnboardingSummary, etc.
 */
@Composable
fun DonutChart(
    segments: List<Pair<Float, Color>>,  // value to color
    modifier: Modifier = Modifier,
    strokeWidth: Float = 36f,
    centerContent: @Composable BoxScope.() -> Unit = {}
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val total = segments.sumOf { it.first.toDouble() }.toFloat().coerceAtLeast(1f)
            var startAngle = -90f
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)
            // Background track
            drawArc(color = Color.White.copy(alpha = 0.08f), startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            segments.forEach { (value, color) ->
                val sweep = (value / total) * 360f
                drawArc(color = color, startAngle = startAngle, sweepAngle = sweep - 2f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                startAngle += sweep
            }
        }
        centerContent()
    }
}

// ── BAR CHART ────────────────────────────────────────────────

/**
 * BarChart — Canvas-drawn vertical bar chart.
 * Matches the Heart Rate Zone style in the reference image.
 * Bars for Mon–Sat with the highlighted bar in LimeGreen.
 */
@Composable
fun BarChart(
    values: List<Float>,         // normalized 0..1
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColor: Color = LimeGreen,
    highlightIndex: Int = -1,    // which bar to highlight with full lime
    trackColor: Color = Color.White.copy(alpha = 0.1f)
) {
    Canvas(modifier = modifier) {
        val barCount = values.size
        if (barCount == 0) return@Canvas
        val totalWidth = size.width
        val barWidth = (totalWidth / (barCount * 1.6f)).coerceAtMost(40.dp.toPx())
        val gap = (totalWidth - barWidth * barCount) / (barCount + 1)
        val maxH = size.height * 0.85f

        values.forEachIndexed { i, v ->
            val x = gap + i * (barWidth + gap)
            val barH = (v.coerceIn(0f, 1f) * maxH).coerceAtLeast(8.dp.toPx())
            val top = size.height - barH

            // Background bar
            drawRoundRect(color = trackColor, topLeft = Offset(x, size.height - maxH), size = Size(barWidth, maxH), cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2))
            // Value bar
            val c = if (i == highlightIndex) LimeGreen else barColor.copy(alpha = 0.7f)
            drawRoundRect(color = c, topLeft = Offset(x, top), size = Size(barWidth, barH), cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2))
        }
    }
}

// ── LINE CHART ───────────────────────────────────────────────

/**
 * LineChart — Smooth Bézier line chart with gradient fill.
 * Used in WeightProgress, CalorieTrend screens.
 */
@Composable
fun LineChart(
    points: List<Float>,       // normalized 0..1 values
    modifier: Modifier = Modifier,
    lineColor: Color = LimeGreen,
    fillColor: Color = LimeGreen.copy(alpha = 0.15f)
) {
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas
        val stepX = size.width / (points.size - 1)
        val pts = points.mapIndexed { i, v -> Offset(i * stepX, size.height - v * size.height * 0.85f - size.height * 0.07f) }

        // Fill path
        val fillPath = Path().apply {
            moveTo(pts.first().x, size.height)
            lineTo(pts.first().x, pts.first().y)
            for (i in 1 until pts.size) {
                val cp1 = Offset((pts[i - 1].x + pts[i].x) / 2f, pts[i - 1].y)
                val cp2 = Offset((pts[i - 1].x + pts[i].x) / 2f, pts[i].y)
                cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, pts[i].x, pts[i].y)
            }
            lineTo(pts.last().x, size.height)
            close()
        }
        drawPath(fillPath, brush = Brush.verticalGradient(listOf(fillColor, Color.Transparent)))

        // Line path
        val linePath = Path().apply {
            moveTo(pts.first().x, pts.first().y)
            for (i in 1 until pts.size) {
                val cp1 = Offset((pts[i - 1].x + pts[i].x) / 2f, pts[i - 1].y)
                val cp2 = Offset((pts[i - 1].x + pts[i].x) / 2f, pts[i].y)
                cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, pts[i].x, pts[i].y)
            }
        }
        drawPath(linePath, color = lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Dots
        pts.forEach { pt -> drawCircle(color = lineColor, radius = 4.dp.toPx(), center = pt) }
    }
}

// ── CALORIE RING WIDGET ──────────────────────────────────────

/**
 * CalorieRingWidget — Large circular progress ring.
 * Center shows calories remaining. Matches reference image style.
 */
@Composable
fun CalorieRingWidget(
    consumed: Int,
    goal: Int,
    modifier: Modifier = Modifier
) {
    val progress = (consumed.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    val remaining = (goal - consumed).coerceAtLeast(0)
    val animProg by animateFloatAsState(targetValue = progress, animationSpec = tween(1000, easing = FastOutSlowInEasing), label = "ring")

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Draw ring on canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 20.dp.toPx()          // slightly thinner so inner text has room
            val diameter = size.minDimension - stroke
            val tl = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arc = Size(diameter, diameter)
            drawArc(color = Color.White.copy(0.08f), startAngle = -90f, sweepAngle = 360f, useCenter = false, topLeft = tl, size = arc, style = Stroke(stroke, cap = StrokeCap.Round))
            drawArc(color = LimeGreen, startAngle = -90f, sweepAngle = animProg * 360f, useCenter = false, topLeft = tl, size = arc, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        // Centre text — constrained to inner ring diameter with padding
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize(0.58f)   // 58% of the widget = safely inside the ring
                .padding(4.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$remaining",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = "kcal left",
                style = MaterialTheme.typography.labelSmall,
                color = LimeGreen,
                maxLines = 1
            )
            Text(
                text = "/ $goal",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

// ── MACRO ROW ────────────────────────────────────────────────

/**
 * MacroProgressRow — Horizontal macro bar with label and grams.
 * Matches "Derest 30.5%, Green 18.1%, Vibrant 18.7%" style from image.
 */
@Composable
fun MacroProgressRow(label: String, grams: Int, goalGrams: Int, color: Color) {
    val progress = (grams.toFloat() / goalGrams.toFloat()).coerceIn(0f, 1f)
    val pct = (progress * 100).toInt()
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            Text("${grams}g  •  $pct%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        ModernProgressBar(progress = progress, color = color, height = 8.dp)
    }
}

// ── NUTRIENT LEGEND ITEM ─────────────────────────────────────

@Composable
fun NutrientLegendItem(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ── METRIC HEADER PAIR ───────────────────────────────────────

@Composable
fun MetricPair(label1: String, val1: String, label2: String, val2: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(label1, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(val1, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(label2, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(val2, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ── ACTIVITY ROW CARD ────────────────────────────────────────

@Composable
fun ActivityRowCard(
    title: String,
    subtitle: String,
    progress: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = LimeGreen,
    onClick: () -> Unit = {}
) {
    SurfaceCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(color.copy(0.15f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
                }
                ModernProgressBar(progress = progress, color = color, height = 7.dp)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── WEEK DAY HEADER ──────────────────────────────────────────

@Composable
fun WeekDayRow(days: List<String> = listOf("M", "T", "W", "T", "F", "S", "S"), selectedIndex: Int = 3, onSelect: (Int) -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        days.forEachIndexed { i, d ->
            val sel = i == selectedIndex
            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape).background(if (sel) LimeGreen else Color.Transparent).clickable { onSelect(i) },
                contentAlignment = Alignment.Center
            ) { Text(d, style = MaterialTheme.typography.labelLarge, fontWeight = if (sel) FontWeight.Black else FontWeight.Normal, color = if (sel) DarkBg else MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

// ── PREVIEWS ─────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
fun DonutChartPreview() {
    AppTheme {
        DonutChart(
            segments = listOf(Pair(120f, LimeGreen), Pair(250f, AccentPurple), Pair(65f, AccentOrange)),
            modifier = Modifier.size(160.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("435", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.White)
                Text("kcal", fontSize = 12.sp, color = LimeGreen)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
fun BarChartPreview() {
    AppTheme {
        BarChart(
            values = listOf(0.3f, 0.6f, 0.45f, 0.8f, 0.55f, 0.95f),
            labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
            highlightIndex = 5,
            modifier = Modifier.fillMaxWidth().height(120.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
fun CalorieRingPreview() {
    AppTheme {
        CalorieRingWidget(consumed = 1450, goal = 2200, modifier = Modifier.size(200.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F0F)
@Composable
fun ActivityRowCardPreview() {
    AppTheme {
        ActivityRowCard(title = "Meditation", subtitle = "Progress 80%", progress = 0.8f, icon = Icons.Default.SelfImprovement)
    }
}
