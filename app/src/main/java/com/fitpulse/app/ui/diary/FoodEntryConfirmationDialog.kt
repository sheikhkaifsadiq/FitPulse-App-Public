package com.fitpulse.app.ui.diary

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.fitpulse.app.ui.components.DarkInputField
import com.fitpulse.app.ui.components.PrimaryButton
import androidx.compose.ui.tooling.preview.Preview
import com.fitpulse.app.AppTheme

@Composable
fun FoodEntryConfirmationDialog(
    initialName: String,
    calories: Int,
    protein: Float,
    carbs: Float,
    fat: Float,
    servingSize: String,
    onConfirm: (String, Int, Float, Float, Float) -> Unit,
    onDismiss: () -> Unit
) {
    var foodName by remember { mutableStateOf(initialName) }
    var calInput by remember { mutableStateOf(calories.toString()) }
    var proteinInput by remember { mutableStateOf(protein.toString()) }
    var carbsInput by remember { mutableStateOf(carbs.toString()) }
    var fatInput by remember { mutableStateOf(fat.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Log Details") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Verify detected item details for $servingSize:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                DarkInputField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = "Food Name"
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DarkInputField(
                        value = calInput,
                        onValueChange = { calInput = it },
                        label = "kcal",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    DarkInputField(
                        value = proteinInput,
                        onValueChange = { proteinInput = it },
                        label = "P (g)",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DarkInputField(
                        value = carbsInput,
                        onValueChange = { carbsInput = it },
                        label = "C (g)",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    DarkInputField(
                        value = fatInput,
                        onValueChange = { fatInput = it },
                        label = "F (g)",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Add to Diary",
                onClick = { 
                    onConfirm(
                        foodName, 
                        calInput.toIntOrNull() ?: 0,
                        proteinInput.toFloatOrNull() ?: 0f,
                        carbsInput.toFloatOrNull() ?: 0f,
                        fatInput.toFloatOrNull() ?: 0f
                    ) 
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun FoodEntryConfirmationDialogPreview() {
    AppTheme {
        FoodEntryConfirmationDialog(
            initialName = "Apple",
            calories = 95,
            protein = 0.5f,
            carbs = 25f,
            fat = 0.3f,
            servingSize = "1 medium",
            onConfirm = { _, _, _, _, _ -> },
            onDismiss = {}
        )
    }
}
