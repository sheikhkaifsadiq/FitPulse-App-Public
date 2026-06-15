package com.fitpulse.app.ui.progress

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fitpulse.app.*
import com.fitpulse.app.data.local.AppDatabase
import com.fitpulse.app.security.SecurityManager
import com.fitpulse.app.ui.components.SurfaceCard
import com.fitpulse.app.ui.navigation.UserTopBar

@Composable
fun BodyMetricsDetailScreen(navController: NavController) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getInstance(context) }
    val securityManager = remember { SecurityManager(context) }
    val weightUnit = securityManager.getWeightUnit()

    val user by database.userDao().getCurrentUser().collectAsState(initial = null)

    val bmi = if (user != null && user!!.heightCm > 0 && user!!.weightKg > 0) {
        user!!.weightKg / ((user!!.heightCm / 100f) * (user!!.heightCm / 100f))
    } else 0f

    val bodyFat = if (bmi > 0 && user != null && user!!.age > 0) {
        val genderFactor = if (user!!.gender.equals("Male", ignoreCase = true)) 1 else 0
        ((1.2f * bmi) + (0.23f * user!!.age) - (10.8f * genderFactor) - 5.4f).coerceAtLeast(5f)
    } else 0f

    val musclePct = if (bodyFat > 0) (100f - bodyFat - 10f).coerceAtLeast(0f) else 0f
    val bonePct = 10f
    val fatPct = bodyFat.coerceAtMost(100f - musclePct - bonePct)

    val currentWeight = user?.weightKg ?: 0f
    val displayWeight = if (weightUnit == "kg") currentWeight else currentWeight * 2.20462f

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { UserTopBar(title = "Body Metrics", navController = navController, showBack = true) }
    ) { pv ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pv).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // ── BMI HERO GAUGE ────────────────────────────────────────
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "BODY MASS INDEX",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        BmiArcGauge(bmi = bmi, modifier = Modifier.fillMaxWidth().height(160.dp))
                        Spacer(Modifier.height(4.dp))

                        // BMI category scale bar
                        val bmiLabels = listOf(
                            "Under" to AccentBlue,
                            "Healthy" to LimeGreen,
                            "Over" to AccentOrange,
                            "Obese" to ErrorRed
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            bmiLabels.forEach { (label, color) ->
                                val isActive = when (label) {
                                    "Under" -> bmi in 1f..18.49f
                                    "Healthy" -> bmi in 18.5f..24.99f
                                    "Over" -> bmi in 25f..29.99f
                                    "Obese" -> bmi >= 30f
                                    else -> false
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(if (isActive) 6.dp else 4.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isActive) color else color.copy(0.25f))
                                    )
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isActive) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }

                        if (user?.heightCm != null && user!!.heightCm > 0) {
                            Spacer(Modifier.height(4.dp))
                            val heightDisplay = if (weightUnit == "kg") {
                                "${user!!.heightCm.toInt()} cm"
                            } else {
                                val totalIn = (user!!.heightCm / 2.54f).toInt()
                                "${totalIn / 12}′ ${totalIn % 12}″"
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                MetricChip("Height", heightDisplay, MaterialTheme.colorScheme.onSurfaceVariant)
                                MetricChip("Weight", "${String.format("%.1f", displayWeight)} $weightUnit", MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // ── BODY COMPOSITION PIE ─────────────────────────────────
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "BODY COMPOSITION",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.5.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Donut chart
                            if (bodyFat > 0) {
                                CompositionDonut(
                                    musclePct = musclePct,
                                    fatPct = fatPct,
                                    bonePct = bonePct,
                                    modifier = Modifier.size(120.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier.size(120.dp).clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("N/A", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                CompositionLegendRow("💪 Muscle", if (bodyFat > 0) "${String.format("%.1f", musclePct)}%" else "--", LimeGreen)
                                CompositionLegendRow("🔥 Body Fat", if (bodyFat > 0) "${String.format("%.1f", fatPct)}%" else "--", AccentOrange)
                                CompositionLegendRow("🦴 Bone & Other", "${String.format("%.1f", bonePct)}%", AccentBlue)
                            }
                        }

                        if (bodyFat > 0) {
                            val bfCategory = when {
                                bodyFat < 10f -> "Athletic" to LimeGreen
                                bodyFat < 20f -> "Fit" to LimeGreenDark
                                bodyFat < 28f -> "Average" to AccentOrange
                                else -> "High" to ErrorRed
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bfCategory.second.copy(0.10f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(8.dp).clip(CircleShape).background(bfCategory.second)
                                )
                                Text(
                                    "Body fat is ${bfCategory.first.lowercase()} (${String.format("%.1f", fatPct)}%). ${
                                        when (bfCategory.first) {
                                            "Athletic" -> "Excellent — elite-level leanness."
                                            "Fit" -> "Great job — healthy and active range."
                                            "Average" -> "Within normal range. Exercise more to improve."
                                            else -> "Consider increasing cardio and nutrition changes."
                                        }
                                    }",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // ── MUSCLE BREAKDOWN ─────────────────────────────────────
            item {
                SurfaceCard(
                    modifier = Modifier.fillMaxWidth(),
                    bg = LimeGreen.copy(0.05f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(0.2f))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(LimeGreen.copy(0.2f)),
                                contentAlignment = Alignment.Center
                            ) { Text("💪", fontSize = 18.sp) }
                            Column {
                                Text("MUSCLE MASS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                                Text(
                                    if (bodyFat > 0) "${String.format("%.1f", musclePct)}% of body weight" else "Complete your profile",
                                    style = MaterialTheme.typography.bodySmall, color = LimeGreen, fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        if (bodyFat > 0) {
                            val muscleMassKg = currentWeight * (musclePct / 100f)
                            val displayMuscleKg = if (weightUnit == "kg") muscleMassKg else muscleMassKg * 2.20462f
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MuscleStatBox("Est. Mass", "${String.format("%.1f", displayMuscleKg)} $weightUnit", LimeGreen, Modifier.weight(1f))
                                MuscleStatBox("Lean %", "${String.format("%.1f", musclePct)}%", LimeGreenDark, Modifier.weight(1f))
                                MuscleStatBox("Fat-Free", "${String.format("%.1f", 100f - fatPct)}%", AccentBlue, Modifier.weight(1f))
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(10.dp)).background(LimeGreen.copy(0.12f))) {
                                Box(modifier = Modifier.fillMaxWidth(musclePct / 100f).fillMaxHeight().clip(RoundedCornerShape(10.dp)).background(LimeGreen))
                            }
                        } else {
                            Text(
                                "Add your age and gender in your profile to see muscle estimates.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── HEALTH TIPS ───────────────────────────────────────────
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("HEALTH TIPS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.5.sp)
                        val tips = when {
                            bmi <= 0 -> listOf(
                                "🏋️" to "Add your height and weight in your profile to unlock metrics.",
                                "📊" to "Body metrics help you track fitness progress over time."
                            )
                            bmi < 18.5f -> listOf(
                                "🍗" to "Increase calorie-dense foods like nuts, avocado, and lean proteins.",
                                "🏋️" to "Strength training builds muscle mass effectively.",
                                "😴" to "Sleep 8 hours to support muscle recovery and growth."
                            )
                            bmi < 25f -> listOf(
                                "✅" to "You're in a healthy BMI range — keep it up!",
                                "💪" to "Focus on maintaining muscle with strength training 3x/week.",
                                "🥗" to "Balanced macros (protein, carbs, fat) sustain your composition."
                            )
                            bmi < 30f -> listOf(
                                "🚶" to "Aim for 8,000–10,000 steps daily to burn extra calories.",
                                "🥩" to "Increase protein intake to 1.6g per kg of body weight.",
                                "💧" to "Drink water before meals to reduce calorie intake naturally."
                            )
                            else -> listOf(
                                "🏃" to "Start with low-impact cardio: walking, cycling, swimming.",
                                "🥗" to "Reduce processed foods and increase whole, nutrient-dense meals.",
                                "👩‍⚕️" to "Consider speaking to a certified nutritionist or doctor."
                            )
                        }
                        tips.forEach { (emoji, tip) ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                                Text(emoji, fontSize = 18.sp)
                                Text(tip, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.85f))
                            }
                        }
                    }
                }
            }

            // ── NOTE ─────────────────────────────────────────────────
            item {
                Text(
                    "* BMI and body composition estimates are calculated from height, weight, age and gender. Results are approximate and may vary. Consult a healthcare professional for clinical assessments.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

// ── BMI ARC GAUGE ──────────────────────────────────────────────────────────

@Composable
private fun BmiArcGauge(bmi: Float, modifier: Modifier = Modifier) {
    val animatedBmi by animateFloatAsState(
        targetValue = bmi.coerceIn(10f, 40f),
        animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic),
        label = "bmi_anim"
    )
    val bmiColor = when {
        bmi <= 0 -> Color.Gray
        bmi < 18.5f -> AccentBlue
        bmi < 25f -> LimeGreen
        bmi < 30f -> AccentOrange
        else -> ErrorRed
    }
    val bmiLabel = when {
        bmi <= 0 -> "--"
        else -> String.format("%.1f", bmi)
    }
    val category = when {
        bmi <= 0 -> "No data"
        bmi < 18.5f -> "Underweight"
        bmi < 25f -> "Healthy"
        bmi < 30f -> "Overweight"
        else -> "Obese"
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 22f
            val arcRadius = (size.minDimension / 2f) - strokeWidth
            val topLeft = Offset(center.x - arcRadius, center.y - arcRadius)
            val arcSize = Size(arcRadius * 2, arcRadius * 2)
            val startAngle = 150f
            val sweepTotal = 240f

            // Track (background arc)
            drawArc(
                color = Color.Gray.copy(alpha = 0.15f),
                startAngle = startAngle,
                sweepAngle = sweepTotal,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            // Color segments: under / healthy / over / obese
            val segments = listOf(
                AccentBlue.copy(0.4f) to (8.5f / 30f),   // <18.5 → underweight zone
                LimeGreen.copy(0.4f) to (6.5f / 30f),    // 18.5–25 → healthy
                AccentOrange.copy(0.4f) to (5f / 30f),   // 25–30 → overweight
                ErrorRed.copy(0.4f) to (10f / 30f)       // 30+ → obese
            )
            var segStart = startAngle
            segments.forEach { (col, fraction) ->
                val sweep = sweepTotal * fraction
                drawArc(color = col, startAngle = segStart, sweepAngle = sweep, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(strokeWidth, cap = StrokeCap.Butt))
                segStart += sweep
            }

            // Active progress arc
            if (bmi > 0) {
                val progress = ((animatedBmi - 10f) / 30f).coerceIn(0f, 1f)
                drawArc(
                    color = bmiColor,
                    startAngle = startAngle,
                    sweepAngle = sweepTotal * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text(bmiLabel, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = bmiColor, fontSize = 36.sp)
            Text(category, style = MaterialTheme.typography.labelMedium, color = bmiColor.copy(0.8f), fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── DONUT CHART ─────────────────────────────────────────────────────────────

@Composable
private fun CompositionDonut(musclePct: Float, fatPct: Float, bonePct: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 28f
        val radius = (size.minDimension / 2f) - strokeWidth
        val topLeft = Offset(center.x - radius, center.y - radius)
        val arcSize = Size(radius * 2, radius * 2)
        val total = musclePct + fatPct + bonePct
        val segments = listOf(
            LimeGreen to (musclePct / total * 360f),
            AccentOrange to (fatPct / total * 360f),
            AccentBlue to (bonePct / total * 360f)
        )
        var start = -90f
        segments.forEach { (color, sweep) ->
            drawArc(color = color, startAngle = start, sweepAngle = sweep - 2f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(strokeWidth, cap = StrokeCap.Round))
            start += sweep
        }
    }
}

// ── HELPERS ──────────────────────────────────────────────────────────────────

@Composable
private fun CompositionLegendRow(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun MuscleStatBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    SurfaceCard(modifier = modifier, bg = color.copy(0.07f)) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}
