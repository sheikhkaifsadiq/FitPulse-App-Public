package com.fitpulse.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CalorieSummaryCard() {
    SurfaceCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Calorie Summary", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("1,200 / 2,000 kcal", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
