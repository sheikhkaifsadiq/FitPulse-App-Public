package com.fitpulse.app.ui.navigation

sealed class Screen(val route: String) {
    // Auth Flow
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Permissions : Screen("permissions/{destination}") {
        fun createRoute(destination: String) = "permissions/$destination"
    }
    object Login : Screen("login")
    object Signup : Screen("signup/{role}") {
        fun createRoute(role: String) = "signup/$role"
    }
    object RoleEntry : Screen("role_entry")
    object ForgotPassword : Screen("forgot_password")
    object EmailVerification : Screen("email_verification")
    object VerificationPending : Screen("verification_pending")
    object VerificationRejected : Screen("verification_rejected")
    object NavigationHub : Screen("navigation_hub")

    // Onboarding Flow
    object OnboardingWelcome : Screen("onboarding_welcome")
    object OnboardingGoal : Screen("onboarding_goal")
    object OnboardingActivity : Screen("onboarding_activity")
    object OnboardingBody : Screen("onboarding_body")
    object OnboardingTarget : Screen("onboarding_target")
    object WeeklyGoal : Screen("weekly_goal")

    // User Flow
    object Home : Screen("home")
    object FoodDiary : Screen("food_diary")
    object FoodSearch : Screen("food_search/{mealType}") {
        fun createRoute(mealType: String) = "food_search/$mealType"
    }
    object FoodDetail : Screen("food_detail/{foodId}/{mealType}?isManual={isManual}") {
        fun createRoute(foodId: String, mealType: String, isManual: Boolean = false) = "food_detail/$foodId/$mealType?isManual=$isManual"
    }
    object CustomFood : Screen("custom_food")
    object RecipeBuilder : Screen("recipe_builder")
    object BarcodeScanner : Screen("barcode_scanner")
    object WaterTracker : Screen("water_tracker")
    
    object ExerciseLog : Screen("exercise_log")
    object ExerciseSearch : Screen("exercise_search/{category}") {
        fun createRoute(category: String) = "exercise_search/$category"
    }
    object LogExercise : Screen("log_exercise/{exerciseId}/{category}") {
        fun createRoute(exerciseId: String, category: String): String {
            val encId = java.net.URLEncoder.encode(exerciseId, "UTF-8")
            val encCat = java.net.URLEncoder.encode(category, "UTF-8")
            return "log_exercise/$encId/$encCat"
        }
    }

    object Progress : Screen("progress")
    object WeightProgress : Screen("weight_progress")
    object CalorieTrend : Screen("calorie_trend")
    object MacroTrend : Screen("macro_trend")
    object BodyMetrics : Screen("body_metrics")
    object Streaks : Screen("streaks")

    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object GoalsTargets : Screen("goals_targets")
    object AppPreferences : Screen("app_preferences")
    object NotificationSettings : Screen("notification_settings")

    object PremiumPlans : Screen("premium_plans")
    object Payment : Screen("payment/{plan}") {
        fun createRoute(plan: String) = "payment/$plan"
    }
    object PaymentSuccess : Screen("payment_success")
    object PaymentFailed : Screen("payment_failed")

    object Chat : Screen("chat/{peerId}/{role}") {
        fun createRoute(peerId: String, role: String) = "chat/$peerId/$role"
    }
    object ExpertList : Screen("expert_list")
    object ExpertProfile : Screen("expert_profile/{expertId}") {
        fun createRoute(expertId: String) = "expert_profile/$expertId"
    }

    // Expert Flow
    object ExpertDashboard : Screen("expert_dashboard")
    object EarningsDashboard : Screen("earnings_dashboard")
    object ClientList : Screen("client_list")
    object ClientProfile : Screen("client_profile/{clientId}") {
        fun createRoute(clientId: String) = "client_profile/$clientId"
    }
    object ExpertInbox : Screen("expert_inbox")
    object ExpertOwnProfile : Screen("expert_own_profile")
    object EditExpertProfile : Screen("edit_expert_profile")
    object Availability : Screen("availability")
    
    // AI Feature
    object AiNutrition : Screen("ai_nutrition")
}
