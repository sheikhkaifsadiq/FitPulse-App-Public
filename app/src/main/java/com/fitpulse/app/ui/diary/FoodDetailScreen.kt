package com.fitpulse.app.ui.diary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fitpulse.app.data.local.AppDatabase
import com.fitpulse.app.security.SecurityManager
import com.fitpulse.app.ui.components.*
import com.fitpulse.app.ui.viewmodels.FitPulseViewModelFactory
import com.fitpulse.app.ui.viewmodels.FoodDetailState
import com.fitpulse.app.ui.viewmodels.FoodDetailViewModel

// ── Validation config ────────────────────────────────────────────────────────

private data class ValidationResult(
    val isError: Boolean,
    val isWarning: Boolean,
    val message: String
)

private fun validateCal(v: String): ValidationResult {
    val n = v.toIntOrNull()
    return when {
        n == null      -> ValidationResult(false, false, "")
        n < 0          -> ValidationResult(true,  false, "Cannot be negative")
        n > 2500       -> ValidationResult(false, true,  "Unusually high for a single item")
        else           -> ValidationResult(false, false, "")
    }
}

private fun validateMacro(v: String, max: Float, label: String): ValidationResult {
    val n = v.toFloatOrNull()
    return when {
        n == null -> ValidationResult(false, false, "")
        n < 0f    -> ValidationResult(true,  false, "$label cannot be negative")
        n > max   -> ValidationResult(false, true,  "$label: unusually high for one meal")
        else      -> ValidationResult(false, false, "")
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailScreen(
    navController: NavController,
    foodId: String   = "",
    mealType: String = "GENERAL",
    isManual: Boolean = false
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getInstance(context) }
    val securityManager = remember { SecurityManager(context) }
    val factory = remember {
        FitPulseViewModelFactory(
            userDao      = database.userDao(),
            foodDao      = database.foodDao(),
            waterDao     = database.waterDao(),
            exerciseDao  = database.exerciseDao(),
            weightDao    = database.weightDao(),
            securityManager = securityManager
        )
    }
    val viewModel: FoodDetailViewModel = viewModel(factory = factory)
    val state by viewModel.foodDetail.collectAsState()
    var quantity by remember { mutableStateOf(1f) }

    LaunchedEffect(foodId) { viewModel.loadFood(foodId) }

    FoodDetailContent(
        state          = state,
        quantity       = quantity,
        onQuantityChange = { quantity = it },
        mealType       = mealType,
        isManual       = isManual,
        onBackClick    = { navController.popBackStack() },
        onAddClick     = { name, cal, prot, carb, fat, baseCal, baseProt, baseCarb, baseFat ->
            viewModel.logFood(mealType, quantity, name, cal, prot, carb, fat, baseCal, baseProt, baseCarb, baseFat)
            navController.popBackStack()
        }
    )
}

// ── Content ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailContent(
    state: FoodDetailState,
    quantity: Float,
    onQuantityChange: (Float) -> Unit,
    mealType: String,
    isManual: Boolean,
    onBackClick: () -> Unit,
    onAddClick: (String, Int, Float, Float, Float, Int, Float, Float, Float) -> Unit
) {
    var editName by remember { mutableStateOf("") }
    var editCal  by remember { mutableStateOf("") }
    var editProt by remember { mutableStateOf("") }
    var editCarb by remember { mutableStateOf("") }
    var editFat  by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }

    // Populate fields once the VM resolves the food
    LaunchedEffect(state) {
        if (state is FoodDetailState.Success && !isInitialized) {
            editName = state.data.name
            if (isManual) {
                editCal  = "0"
                editProt = "0"
                editCarb = "0"
                editFat  = "0"
            } else {
                editCal  = state.data.calories.toString()
                editProt = state.data.protein.toString()
                editCarb = state.data.carbs.toString()
                editFat  = state.data.fat.toString()
            }
            isInitialized = true
        }
    }

    // Computed display values (multiplied by quantity for known foods)
    val displayCal  = if (isManual) editCal  else ((editCal.toIntOrNull()    ?: 0)  * quantity).toInt().toString()
    val displayProt = if (isManual) editProt else String.format(java.util.Locale.US, "%.1f", (editProt.toFloatOrNull() ?: 0f) * quantity)
    val displayCarb = if (isManual) editCarb else String.format(java.util.Locale.US, "%.1f", (editCarb.toFloatOrNull() ?: 0f) * quantity)
    val displayFat  = if (isManual) editFat  else String.format(java.util.Locale.US, "%.1f", (editFat.toFloatOrNull()  ?: 0f) * quantity)

    // ── Validation ─────────────────────────────────────────────────────────
    val calVal  = validateCal(if (isManual) editCal  else displayCal)
    val protVal = validateMacro(if (isManual) editProt else displayProt, 150f, "Protein")
    val carbVal = validateMacro(if (isManual) editCarb else displayCarb, 500f, "Carbs")
    val fatVal  = validateMacro(if (isManual) editFat  else displayFat,  250f, "Fat")

    val hasHardError = calVal.isError || protVal.isError || carbVal.isError || fatVal.isError

    val mealLabel = mealType.lowercase().replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isManual) "Manual Entry" else "Verify Details",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        when (val s = state) {
            is FoodDetailState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Loading food data...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            is FoodDetailState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Data source badge
                    item {
                        val sourceLabel = when {
                            s.data.brand == "Open Food Facts" -> "✓ Verified data from Open Food Facts"
                            isManual -> "Manual entry — please fill in the nutrition label"
                            else     -> "The app found this item. Confirm or adjust the portion."
                        }
                        Text(
                            sourceLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isManual) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Input card
                    item {
                        SurfaceCard {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                DarkInputField(
                                    value       = editName,
                                    onValueChange = { if (isManual) editName = it },
                                    label       = "Food Name",
                                    readOnly    = !isManual
                                )

                                // Calories row
                                DarkInputField(
                                    value       = displayCal,
                                    onValueChange = { if (isManual) editCal = it },
                                    label       = "Calories",
                                    readOnly    = !isManual,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    isError     = calVal.isError
                                )
                                ValidationMessage(calVal)

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(Modifier.weight(1f)) {
                                        DarkInputField(
                                            value       = displayProt,
                                            onValueChange = { if (isManual) editProt = it },
                                            label       = "Protein (g)",
                                            readOnly    = !isManual,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            isError     = protVal.isError
                                        )
                                        ValidationMessage(protVal)
                                    }
                                    Column(Modifier.weight(1f)) {
                                        DarkInputField(
                                            value       = displayCarb,
                                            onValueChange = { if (isManual) editCarb = it },
                                            label       = "Carbs (g)",
                                            readOnly    = !isManual,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            isError     = carbVal.isError
                                        )
                                        ValidationMessage(carbVal)
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(Modifier.weight(1f)) {
                                        DarkInputField(
                                            value       = displayFat,
                                            onValueChange = { if (isManual) editFat = it },
                                            label       = "Fat (g)",
                                            readOnly    = !isManual,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            isError     = fatVal.isError
                                        )
                                        ValidationMessage(fatVal)
                                    }
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    // Portion multiplier (known foods only)
                    if (!isManual) {
                        item {
                            SurfaceCard {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        "MULTIPLIER / PORTION",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            IconButton(
                                                onClick = { if (quantity > 0.1f) onQuantityChange(quantity - 0.1f) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Text("-", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                                            }
                                            Text(
                                                String.format(java.util.Locale.US, "%.1f", quantity),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            IconButton(
                                                onClick = { if (quantity < 10f) onQuantityChange(quantity + 0.1f) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Text("+", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                        Spacer(Modifier.weight(1f))
                                        Text("x Base Portion", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }

                    // Hard-error global warning
                    if (hasHardError) {
                        item {
                            SurfaceCard {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Please fix the highlighted fields before saving.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    // Save button
                    item {
                        PrimaryButton(
                            text    = if (hasHardError) "Fix Errors to Save" else "Add to $mealLabel",
                            enabled = !hasHardError,
                            onClick = {
                                val finalQty = if (isManual) 1f else quantity
                                val baseC  = editCal.toIntOrNull()    ?: 0
                                val baseP  = editProt.toFloatOrNull() ?: 0f
                                val baseCb = editCarb.toFloatOrNull() ?: 0f
                                val baseF  = editFat.toFloatOrNull()  ?: 0f
                                val totalC  = if (isManual) baseC  else (baseC * quantity).toInt()
                                val totalP  = if (isManual) baseP  else (baseP * quantity)
                                val totalCb = if (isManual) baseCb else (baseCb * quantity)
                                val totalF  = if (isManual) baseF  else (baseF * quantity)
                                onAddClick(editName, totalC, totalP, totalCb, totalF, baseC, baseP, baseCb, baseF)
                            }
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }

            is FoodDetailState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// ── Validation Message Composable ─────────────────────────────────────────────

@Composable
private fun ValidationMessage(result: ValidationResult) {
    AnimatedVisibility(
        visible = result.isError || result.isWarning,
        enter   = fadeIn(),
        exit    = fadeOut()
    ) {
        Text(
            text  = result.message,
            style = MaterialTheme.typography.labelSmall,
            color = if (result.isError) MaterialTheme.colorScheme.error
                    else Color(0xFFFFA726), // amber warning color
            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun FoodDetailScreenPreview() {
    val mockFood = com.fitpulse.app.ui.viewmodels.FoodDetail(
        name     = "Grilled Chicken Breast",
        brand    = "Open Food Facts",
        calories = 165,
        protein  = 31f,
        carbs    = 0f,
        fat      = 3.6f,
        serving  = "100g"
    )
    com.fitpulse.app.AppTheme {
        FoodDetailContent(
            state            = FoodDetailState.Success(mockFood),
            quantity         = 1.5f,
            onQuantityChange = {},
            mealType         = "LUNCH",
            isManual         = false,
            onBackClick      = {},
            onAddClick       = { _, _, _, _, _, _, _, _, _ -> }
        )
    }
}
