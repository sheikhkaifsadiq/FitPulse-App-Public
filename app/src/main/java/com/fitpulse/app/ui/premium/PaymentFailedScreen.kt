package com.fitpulse.app.ui.premium

import androidx.compose.foundation.BorderStroke
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
fun PaymentFailedScreen(navController: NavController, reason: String = "Payment was declined by the issuer.") {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        GlowBlob(color = MaterialTheme.colorScheme.error.copy(0.2f), alignment = Alignment.Center, size = 200.dp, opacity = 0.2f)

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.error.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(40.dp))
            }

            Spacer(Modifier.height(20.dp))
            Text("Payment Failed", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))

            SurfaceCard(border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(0.3f))) {
                Text(reason, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
            }

            Spacer(Modifier.height(20.dp))
            PrimaryButton(text = "Try Again", onClick = { navController.popBackStack() })
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = {}) { Text("Contact Support", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun PaymentFailedScreenPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    com.fitpulse.app.AppTheme {
        PaymentFailedScreen(navController = navController)
    }
}
