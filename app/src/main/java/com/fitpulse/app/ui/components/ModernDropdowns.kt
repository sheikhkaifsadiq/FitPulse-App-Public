package com.fitpulse.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DateOfBirthSelector(
    selectedDay: String,
    selectedMonth: String,
    selectedYear: String,
    onDateChanged: (day: String, month: String, year: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (currentYear - 100..currentYear - 13).map { it.toString() }.reversed()
    val months = (1..12).map { it.toString().padStart(2, '0') }
    
    // Calculate valid days for the selected month/year
    val daysInMonth = try {
        val y = selectedYear.toInt()
        val m = selectedMonth.toInt()
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m - 1)
        }
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    } catch (e: Exception) {
        31
    }
    val days = (1..daysInMonth).map { it.toString().padStart(2, '0') }

    // Ensure selected day is within range if month changes
    LaunchedEffect(selectedMonth, selectedYear) {
        val currentDay = selectedDay.toIntOrNull() ?: 1
        if (currentDay > daysInMonth) {
            onDateChanged(daysInMonth.toString().padStart(2, '0'), selectedMonth, selectedYear)
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ModernDropdown(
            label = "Day",
            options = days,
            selectedOption = selectedDay,
            onOptionSelected = { onDateChanged(it, selectedMonth, selectedYear) },
            modifier = Modifier.weight(1f)
        )
        ModernDropdown(
            label = "Month",
            options = months,
            selectedOption = selectedMonth,
            onOptionSelected = { onDateChanged(selectedDay, it, selectedYear) },
            modifier = Modifier.weight(1f)
        )
        ModernDropdown(
            label = "Year",
            options = years,
            selectedOption = selectedYear,
            onOptionSelected = { onDateChanged(selectedDay, selectedMonth, it) },
            modifier = Modifier.weight(1.5f)
        )
    }
}

@Composable
fun HeightSelector(
    selectedFeet: String,
    selectedInches: String,
    onHeightChanged: (feet: String, inches: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val feetOptions = (3..8).map { it.toString() }
    val inchesOptions = (0..11).map { it.toString() }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ModernDropdown(
            label = "Feet",
            options = feetOptions,
            selectedOption = selectedFeet,
            onOptionSelected = { onHeightChanged(it, selectedInches) },
            modifier = Modifier.weight(1f)
        )
        ModernDropdown(
            label = "Inches",
            options = inchesOptions,
            selectedOption = selectedInches,
            onOptionSelected = { onHeightChanged(selectedFeet, it) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun WeightSelector(
    selectedWhole: String,
    selectedDecimal: String,
    onWeightChanged: (whole: String, decimal: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val wholeOptions = (30..250).map { it.toString() } // 30kg to 250kg
    val decimalOptions = (0..9).map { ".$it" }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ModernDropdown(
            label = "Kg",
            options = wholeOptions,
            selectedOption = selectedWhole,
            onOptionSelected = { onWeightChanged(it, selectedDecimal) },
            modifier = Modifier.weight(1.5f)
        )
        ModernDropdown(
            label = "Decimal",
            options = decimalOptions,
            selectedOption = selectedDecimal,
            onOptionSelected = { onWeightChanged(selectedWhole, it) },
            modifier = Modifier.weight(1f)
        )
    }
}
