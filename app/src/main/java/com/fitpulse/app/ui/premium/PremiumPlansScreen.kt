package com.fitpulse.app.ui.premium

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fitpulse.app.ui.components.*
import com.fitpulse.app.ui.navigation.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import com.fitpulse.app.data.local.AppDatabase

@Composable
fun PremiumPlansScreen(navController: NavController) {
    var isAnnual by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { UserTopBar(title = "Go Premium", navController = navController, showBack = true) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            GlowBlob(color = MaterialTheme.colorScheme.primary, alignment = Alignment.TopEnd, size = 200.dp, opacity = 0.12f)
            GlowBlob(color = MaterialTheme.colorScheme.secondary, alignment = Alignment.BottomStart, size = 200.dp, opacity = 0.10f)
            
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Billing toggle
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text("Monthly", style = MaterialTheme.typography.bodyMedium, color = if (!isAnnual) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    Switch(checked = isAnnual, onCheckedChange = { isAnnual = it }, colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary, uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant))
                    Spacer(Modifier.width(12.dp))
                    Text("Annual", style = MaterialTheme.typography.bodyMedium, color = if (isAnnual) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    AnimatedVisibility(visible = isAnnual) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Save 40%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.5f)),
                            border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = MaterialTheme.colorScheme.secondary.copy(0.3f)),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            // Plan cards
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Free plan
                    SurfaceCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Free", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text("$0", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
                            Text("/ forever", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            listOf("Diary" to true, "Exercise" to true, "Water" to true, "Weight" to true, "All Charts" to false, "Barcode" to false, "Experts" to false).forEach { (f, inc) ->
                                FeatureRow(f, inc)
                            }
                        }
                    }
                    // Premium plan
                    Box(modifier = Modifier.weight(1f)) {
                        SurfaceCard(border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary), bg = MaterialTheme.colorScheme.primaryContainer) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Premium", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                Text(if (isAnnual) "$4.99" else "$7.99", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(if (isAnnual) "/mo · billed annually" else "/month", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(0.2f))
                                listOf("Diary", "Exercise", "Water", "Weight", "All Charts", "Barcode", "Experts").forEach { f ->
                                    FeatureRow(f, true)
                                }
                            }
                        }
                        Surface(modifier = Modifier.align(Alignment.TopEnd).offset((-8).dp, (-8).dp), shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary) {
                            Text("POPULAR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }

            item { 
                PrimaryButton(text = "Subscribe to Premium", onClick = { 
                    scope.launch {
                        val user = db.userDao().getCurrentUser().firstOrNull()
                        user?.let { db.userDao().updateUser(it.copy(isPremium = true)) }
                    }
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://checkout.stripe.com/pay"))
                    context.startActivity(intent)
                }) 
            }
            item {
                TextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                    Text("Restore Purchases", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
        }
    }
}

@Composable
fun FeatureRow(feature: String, included: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (included) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (included) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.size(16.dp)
        )
        Text(feature, style = MaterialTheme.typography.bodyMedium, color = if (included) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun PremiumPlansScreenPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    com.fitpulse.app.AppTheme {
        PremiumPlansScreen(navController = navController)
    }
}
