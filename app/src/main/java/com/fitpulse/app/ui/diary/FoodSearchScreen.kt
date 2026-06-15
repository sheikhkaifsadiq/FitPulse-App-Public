package com.fitpulse.app.ui.diary

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fitpulse.app.data.local.AppDatabase
import com.fitpulse.app.data.repository.FoodRepository
import com.fitpulse.app.security.SecurityManager
import com.fitpulse.app.ui.components.*
import com.fitpulse.app.ui.navigation.Screen
import com.fitpulse.app.ui.viewmodels.*
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchScreen(navController: NavController, mealType: String = "GENERAL") {
    if (androidx.compose.ui.platform.LocalInspectionMode.current) {
        FoodSearchContent(
            query = "",
            searchState = SearchState.Idle,
            recentFoods = emptyList(),
            onQueryChange = {},
            onFoodClick = {},
            onBackClick = {},
            onScanClick = {}
        )
        return
    }

    val context     = LocalContext.current
    val database    = remember { AppDatabase.getInstance(context) }
    val foodRepo    = remember { FoodRepository(database.foodDao()) }
    val factory     = remember {
        FitPulseViewModelFactory(
            userDao         = database.userDao(),
            foodDao         = database.foodDao(),
            waterDao        = database.waterDao(),
            exerciseDao     = database.exerciseDao(),
            weightDao       = database.weightDao(),
            securityManager = SecurityManager(context)
        )
    }

    // Use the two-arg constructor so SearchViewModel also gets FoodDao for recent foods
    val viewModel   = remember { SearchViewModel(foodRepo, database.foodDao()) }
    val query       by viewModel.query.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val recentFoods by viewModel.recentFoodsState.collectAsState()

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) navController.navigate(Screen.BarcodeScanner.route)
    }

    FoodSearchContent(
        query = query,
        searchState = searchState,
        recentFoods = recentFoods,
        onQueryChange = { viewModel.onQueryChange(it) },
        onFoodClick = { food ->
            viewModel.selectFood(food)
            navController.navigate(Screen.FoodDetail.createRoute(food.name, mealType, isManual = false))
        },
        onBackClick = { navController.popBackStack() },
        onScanClick = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                navController.navigate(Screen.BarcodeScanner.route)
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchContent(
    query: String,
    searchState: SearchState,
    recentFoods: List<FoodSearchResult>,
    onQueryChange: (String) -> Unit,
    onFoodClick: (FoodSearchResult) -> Unit,
    onBackClick: () -> Unit,
    onScanClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Add Food", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    IconButton(onClick = onScanClick) {
                        Icon(Icons.Default.QrCodeScanner, "Scan barcode", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // ── Search Bar ────────────────────────────────────────────────────
            OutlinedTextField(
                value          = query,
                onValueChange  = onQueryChange,
                modifier       = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder    = { Text("Search foods...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon    = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon   = if (query.isNotEmpty()) {
                    { IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }}
                } else null,
                shape          = RoundedCornerShape(50),
                colors         = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor    = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor   = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor        = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor      = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true
            )

            // ── State-driven content area ─────────────────────────────────────
            val currentSearchState = searchState
            when {
                // Idle: show recents + popular list
                currentSearchState is SearchState.Idle || query.isEmpty() -> {
                    LazyColumn(
                        modifier            = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Recent foods from Room DB
                        item {
                            Text(
                                "RECENTLY LOGGED",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        if (recentFoods.isEmpty()) {
                            item {
                                Text(
                                    "No recent foods yet. Try searching above!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }
                        } else {
                            items(recentFoods) { food ->
                                FoodResultItem(food = food, onClick = { onFoodClick(food) })
                            }
                        }

                        // Hardcoded popular list (local fallback — always available offline)
                        item {
                            Text(
                                "POPULAR FOODS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }

                        val popularFoods = listOf(
                            FoodSearchResult("Chicken Biryani",   350, 45f, 15f, 12f, "1 plate (250g)"),
                            FoodSearchResult("Roti",              120, 20f,  3f,  1f, "1 piece (40g)"),
                            FoodSearchResult("Naan",              260, 45f,  8f,  4f, "1 piece (100g)"),
                            FoodSearchResult("Chicken Karahi",    290, 10f, 20f, 18f, "1 serving (200g)"),
                            FoodSearchResult("Daal Mash",         220, 35f, 14f,  5f, "1 bowl (150g)"),
                            FoodSearchResult("Chai (Milk & Sugar)",120,15f,  3f,  5f, "1 cup (150ml)"),
                            FoodSearchResult("Paratha",           280, 35f,  5f, 12f, "1 piece (80g)"),
                            FoodSearchResult("Egg",                78,  0.6f,6f,  5f, "1 large"),
                            FoodSearchResult("Chicken Breast",    165,  0f, 31f, 3.6f,"100g"),
                            FoodSearchResult("Rice (White)",      205, 45f,  4f, 0.4f,"1 cup cooked"),
                            FoodSearchResult("Oatmeal",           150, 27f,  5f, 2.5f,"1 cup cooked"),
                            FoodSearchResult("Banana",            105, 27f, 1.3f,0.4f,"1 medium"),
                            FoodSearchResult("Apple",              95, 25f, 0.5f,0.3f,"1 medium"),
                            FoodSearchResult("Almonds",           164,  6f,  6f, 14f, "1 oz (28g)"),
                            FoodSearchResult("Seekh Kebab",       180,  5f, 14f, 12f, "2 pieces (100g)"),
                        )

                        items(popularFoods) { food ->
                            FoodResultItem(food = food, onClick = { onFoodClick(food) })
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }

                // Loading spinner
                currentSearchState is SearchState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Searching Open Food Facts...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // Success: show API results
                currentSearchState is SearchState.Success -> {
                    LazyColumn(
                        modifier            = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("RESULTS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "· Open Food Facts",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        items(currentSearchState.results) { food ->
                            FoodResultItem(food = food, onClick = { onFoodClick(food) })
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }

                // Empty results
                currentSearchState is SearchState.Empty -> {
                    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.SearchOff,
                                null,
                                tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No results found on Open Food Facts.\nTry a different search term.",
                                style     = MaterialTheme.typography.bodyMedium,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Network error
                currentSearchState is SearchState.Error -> {
                    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.WifiOff,
                                null,
                                tint     = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                currentSearchState.message,
                                style     = MaterialTheme.typography.bodyMedium,
                                color     = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Reusable food result card ─────────────────────────────────────────────────

@Composable
fun FoodResultItem(food: FoodSearchResult, onClick: () -> Unit) {
    SurfaceCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier            = Modifier.height(72.dp).padding(horizontal = 16.dp),
            verticalAlignment   = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(food.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "${food.serving} · ${food.calories} kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "P:${food.protein.toInt()}g  C:${food.carbs.toInt()}g  F:${food.fat.toInt()}g",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Keep old signature for compatibility (the old FoodResultItem(name, sub, macros, onClick))
@Composable
fun FoodResultItem(name: String, sub: String, macros: String, onClick: () -> Unit) {
    SurfaceCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier          = Modifier.height(70.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(sub,  style = MaterialTheme.typography.bodySmall,  color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(macros, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun FoodSearchPreview() {
    MaterialTheme {
        FoodSearchContent(
            query = "",
            searchState = com.fitpulse.app.ui.viewmodels.SearchState.Idle,
            recentFoods = emptyList(),
            onQueryChange = {},
            onFoodClick = {},
            onBackClick = {},
            onScanClick = {}
        )
    }
}
