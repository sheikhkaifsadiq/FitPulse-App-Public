package com.fitpulse.app

import androidx.compose.ui.graphics.Color

// ============================================================
// FITPULSE MODERN COLOR SYSTEM (Matching Reference Design)
// ============================================================

// --- Core Brand Accent: Lime / Yellow-Green ---
val LimeGreen       = Color(0xFFBEF264)   // Primary accent (lime green from design)
val LimeGreenDark   = Color(0xFF84CC16)   // Slightly deeper for press states
val LimeGreenSoft   = Color(0xFFD9F99D)   // Light tint for backgrounds

// --- Dark Theme Backgrounds ---
val DarkBg          = Color(0xFF0F0F0F)   // True near-black base
val DarkSurface     = Color(0xFF1A1A1A)   // Card surface
val DarkSurface2    = Color(0xFF242424)   // Elevated card layer
val DarkBorder      = Color(0xFF2C2C2C)   // Subtle border
val DarkOnSurface   = Color(0xFFF5F5F5)  // Primary text on dark
val DarkMuted       = Color(0xFF8A8A8A)   // Muted text on dark

// --- Light Theme Backgrounds ---
val LightBg         = Color(0xFFF4F4F4)   // Off-white base
val LightSurface    = Color(0xFFFFFFFF)   // Card surface
val LightSurface2   = Color(0xFFF0F0F0)   // Elevated card
val LightBorder     = Color(0xFFE5E5E5)   // Subtle border
val LightOnSurface  = Color(0xFF1A1A1A)   // Primary text on light
val LightMuted      = Color(0xFF777777)   // Muted text on light

// --- Semantic / Status Colors ---
val AccentOrange    = Color(0xFFFF6B2C)   // Warnings / Calories burned
val AccentBlue      = Color(0xFF3B82F6)   // Hydration / Info
val AccentPurple    = Color(0xFF8B5CF6)   // Carbs / Premium
val ErrorRed        = Color(0xFFEF4444)   // Errors
val SuccessGreen    = Color(0xFF22C55E)   // Success states

// --- Legacy aliases kept for backward-compat ---
val BgPrimary       = DarkBg
val BgSurface       = DarkSurface
val PrimaryTeal     = LimeGreen
val SecondaryTeal   = LimeGreenDark
val AccentBlue2     = AccentBlue

// ============================================================
// DARK COLOR SCHEME
// ============================================================
val md_theme_dark_primary               = LimeGreen
val md_theme_dark_onPrimary             = Color(0xFF0F0F0F)
val md_theme_dark_primaryContainer      = Color(0xFF1E2C0A)
val md_theme_dark_onPrimaryContainer    = LimeGreenSoft
val md_theme_dark_secondary             = LimeGreenDark
val md_theme_dark_onSecondary           = Color(0xFF0F0F0F)
val md_theme_dark_secondaryContainer    = Color(0xFF1E2C0A)
val md_theme_dark_onSecondaryContainer  = LimeGreenSoft
val md_theme_dark_background            = DarkBg
val md_theme_dark_onBackground          = DarkOnSurface
val md_theme_dark_surface               = DarkSurface
val md_theme_dark_onSurface             = DarkOnSurface
val md_theme_dark_surfaceVariant        = DarkSurface2
val md_theme_dark_onSurfaceVariant      = DarkMuted
val md_theme_dark_outline               = DarkBorder
val md_theme_dark_outlineVariant        = DarkSurface2
val md_theme_dark_tertiary              = AccentPurple
val md_theme_dark_error                 = ErrorRed

// ============================================================
// LIGHT COLOR SCHEME
// ============================================================
val md_theme_light_primary              = Color(0xFF1A2E05)  // Dark green for light mode
val md_theme_light_onPrimary            = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer     = LimeGreen
val md_theme_light_onPrimaryContainer   = Color(0xFF0F0F0F)
val md_theme_light_secondary            = LimeGreenDark
val md_theme_light_onSecondary          = Color(0xFF0F0F0F)
val md_theme_light_secondaryContainer   = LimeGreenSoft
val md_theme_light_onSecondaryContainer = Color(0xFF1A1A1A)
val md_theme_light_background           = LightBg
val md_theme_light_onBackground         = LightOnSurface
val md_theme_light_surface              = LightSurface
val md_theme_light_onSurface            = LightOnSurface
val md_theme_light_surfaceVariant       = LightSurface2
val md_theme_light_onSurfaceVariant     = LightMuted
val md_theme_light_outline              = LightBorder
