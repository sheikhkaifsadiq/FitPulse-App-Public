package com.fitpulse.app.ui.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fitpulse.app.ui.components.*

@Composable
fun PaymentSuccessScreen(navController: NavController, plan: String = "PREMIUM") {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        GlowBlob(color = MaterialTheme.colorScheme.primary, alignment = Alignment.TopEnd, size = 300.dp, opacity = 0.15f)

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated checkmark circle (static for now)
            Box(
                modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(40.dp))
            }

            Spacer(Modifier.height(20.dp))
            Text("You're Premium! 🎉", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("Enjoy unlimited access to all features.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))

            SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    PlanDetailRow("Plan", "Premium Annual", MaterialTheme.colorScheme.onSurface)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))
                    PlanDetailRow("Billing", "Billed annually · $59.88/yr", MaterialTheme.colorScheme.onSurface)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))
                    PlanDetailRow("Next Renewal", "May 1, 2027", MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = "Go to Home",
                onClick = { navController.navigate("home") { popUpTo(0) } }
            )
        }
    }
}

@Composable
fun PlanDetailRow(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun PaymentSuccessScreenPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    com.fitpulse.app.AppTheme {
        PaymentSuccessScreen(navController = navController)
    }
}
