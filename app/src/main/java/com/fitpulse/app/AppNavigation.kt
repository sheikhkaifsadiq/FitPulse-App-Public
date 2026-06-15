package com.fitpulse.app

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.fitpulse.app.security.SecurityManager
import com.fitpulse.app.ui.navigation.Screen
import com.fitpulse.app.ui.auth.*
import com.fitpulse.app.ui.onboarding.*
import com.fitpulse.app.ui.home.HomeScreen
import com.fitpulse.app.ui.diary.*
import com.fitpulse.app.ui.recipe.RecipeBuilderScreen
import com.fitpulse.app.ui.water.WaterTrackerScreen
import com.fitpulse.app.ui.exercise.*
import com.fitpulse.app.ui.progress.*
import com.fitpulse.app.ui.profile.*
import com.fitpulse.app.ui.premium.*
import com.fitpulse.app.ui.expert.*
import com.fitpulse.app.ui.experts.*
import com.fitpulse.app.ui.chat.ChatScreen
import com.fitpulse.app.ui.ai.AiNutritionScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val securityManager = remember { SecurityManager(context) }
    
    // Gatekeeper logic
    val isUserLoggedIn = remember { FirebaseAuth.getInstance().currentUser != null }
    val isOnboardingComplete = remember { securityManager.isOnboardingComplete() }
    
    // DO NOT REMOVE: Core session routing logic to prevent Onboarding loops.
    val startDest = if (isUserLoggedIn && isOnboardingComplete) {
        Screen.Home.route
    } else {
        Screen.Splash.route
    }

    NavHost(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = startDest,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        // --- Auth & Onboarding ---
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Welcome.route) { LandingScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(
            route = Screen.Signup.route,
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            SignUpScreen(
                navController,
                role = backStackEntry.arguments?.getString("role") ?: "USER"
            )
        }
        composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(navController) }
        composable(Screen.EmailVerification.route) { EmailVerificationScreen(navController) }
        composable(Screen.VerificationPending.route) { VerificationPendingScreen(navController) }
        composable(Screen.NavigationHub.route) { com.fitpulse.app.ui.auth.NavigationHubScreen(navController) }
        composable(Screen.OnboardingWelcome.route) { OnboardingWelcomeScreen(navController) }
        composable(Screen.OnboardingGoal.route) { OnboardingGoalScreen(navController) }
        composable(Screen.OnboardingActivity.route) { OnboardingActivityScreen(navController) }
        composable(Screen.OnboardingBody.route) { OnboardingBodyScreen(navController) }
        composable(Screen.OnboardingTarget.route) { OnboardingSummaryScreen(navController) }
        composable(Screen.VerificationRejected.route) { VerificationRejectedScreen(navController) }
        composable(
            route = Screen.Permissions.route,
            arguments = listOf(navArgument("destination") { type = NavType.StringType })
        ) { backStackEntry ->
            val dest = backStackEntry.arguments?.getString("destination") ?: Screen.Home.route
            PermissionsScreen(navController = navController, destination = dest)
        }

        // --- User Main Flow ---
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable("user_inbox") { com.fitpulse.app.ui.chat.UserInboxScreen(navController) }
        composable(Screen.FoodDiary.route) { FoodDiaryScreen(navController) }
        composable(
            route = Screen.FoodSearch.route,
            arguments = listOf(navArgument("mealType") { type = NavType.StringType })
        ) { backStackEntry ->
            FoodSearchScreen(
                navController,
                mealType = backStackEntry.arguments?.getString("mealType") ?: "BREAKFAST"
            )
        }
        composable(Screen.CustomFood.route) { CustomFoodScreen(navController) }
        composable(Screen.RecipeBuilder.route) { RecipeBuilderScreen(navController) }
        composable(Screen.BarcodeScanner.route) { BarcodeScannerScreen(navController) }
        composable(Screen.WaterTracker.route) { WaterTrackerScreen(navController) }
        
        composable(
            route = Screen.FoodDetail.route,
            arguments = listOf(
                navArgument("foodId") { type = NavType.StringType },
                navArgument("mealType") { type = NavType.StringType },
                navArgument("isManual") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            FoodDetailScreen(
                navController,
                foodId = backStackEntry.arguments?.getString("foodId") ?: "",
                mealType = backStackEntry.arguments?.getString("mealType") ?: "BREAKFAST",
                isManual = backStackEntry.arguments?.getBoolean("isManual") ?: false
            )
        }

        // --- Exercise & Progress ---
        composable(Screen.ExerciseLog.route) { ExerciseLogScreen(navController) }
        composable(
            route = Screen.ExerciseSearch.route,
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            ExerciseSearchScreen(
                navController,
                category = backStackEntry.arguments?.getString("category") ?: "Strength"
            )
        }
        composable(Screen.Progress.route) { ProgressScreen(navController) }
        composable(Screen.Streaks.route) { StreaksScreen(navController) }
        composable(Screen.WeightProgress.route) { WeightProgressScreen(navController) }
        composable(Screen.CalorieTrend.route) { CalorieTrendScreen(navController) }
        composable(Screen.MacroTrend.route) { MacroTrendScreen(navController) }
        composable(Screen.BodyMetrics.route) { BodyMetricsDetailScreen(navController) }
        composable(Screen.AiNutrition.route) { AiNutritionScreen(navController) }
        
        composable(
            route = Screen.LogExercise.route,
            arguments = listOf(
                navArgument("exerciseId") { type = NavType.StringType },
                navArgument("category") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rawId = backStackEntry.arguments?.getString("exerciseId") ?: ""
            val rawCat = backStackEntry.arguments?.getString("category") ?: "Strength"
            val decodedId = java.net.URLDecoder.decode(rawId, "UTF-8")
            val decodedCat = java.net.URLDecoder.decode(rawCat, "UTF-8")
            
            ExerciseDetailScreen(
                navController,
                exerciseName = decodedId,
                category = decodedCat
            )
        }

        // --- Profile & Premium ---
        composable(Screen.Profile.route) { UserProfileScreen(navController) }
        composable(Screen.EditProfile.route) { EditProfileScreen(navController) }
        composable(Screen.GoalsTargets.route) { GoalsTargetsScreen(navController) }
        composable(Screen.AppPreferences.route) { AppPreferencesScreen(navController) }
        composable(Screen.NotificationSettings.route) { NotificationSettingsScreen(navController) }
        composable(Screen.PremiumPlans.route) { PremiumPlansScreen(navController) }
        composable(Screen.WeeklyGoal.route) { WeeklyGoalSetupScreen(navController) }
        
        composable(
            route = Screen.Payment.route,
            arguments = listOf(navArgument("plan") { type = NavType.StringType })
        ) { entry -> 
            PaymentScreen(navController, plan = entry.arguments?.getString("plan") ?: "PREMIUM") 
        }

        // --- Expert Flow ---
        composable(Screen.ExpertDashboard.route) { ExpertDashboardScreen(navController) }
        composable(Screen.ClientList.route) { ClientListScreen(navController) }
        composable(Screen.ExpertInbox.route) { ExpertInboxScreen(navController) }
        composable(Screen.EarningsDashboard.route) { EarningsDashboardScreen(navController) }
        composable(Screen.Availability.route) { AvailabilityScreen(navController) }
        composable(Screen.ExpertOwnProfile.route) { ExpertOwnProfileScreen(navController) }
        composable(Screen.EditExpertProfile.route) { EditExpertProfileScreen(navController) }
        composable(Screen.ExpertList.route) { ExpertListScreen(navController) }
        
        composable(
            route = Screen.ClientProfile.route,
            arguments = listOf(navArgument("clientId") { type = NavType.StringType })
        ) { entry -> 
            ClientProfileViewScreen(navController, clientId = entry.arguments?.getString("clientId") ?: "") 
        }
        
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("peerId") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType }
            )
        ) { entry -> 
            ChatScreen(
                navController, 
                peerId = entry.arguments?.getString("peerId") ?: "",
                peerRole = entry.arguments?.getString("role") ?: "expert"
            ) 
        }
    }
}
