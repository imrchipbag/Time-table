package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Minimal Black & White Core Palette
val MonoBlack = Color(0xFF000000)
val MonoSurface = Color(0xFF0C0C0C)
val MonoSurfaceCard = Color(0xFF141414)
val MonoSurfaceElevated = Color(0xFF1E1E1E)
val MonoBorder = Color(0xFF262626)
val MonoBorderBright = Color(0xFF404040)
val MonoGlassBorder = Color(0x35FFFFFF)
val MonoGlassBg = Color(0xCC121212)

// High-Contrast Pure Whites & Grayscale
val MonoWhite = Color(0xFFFFFFFF)
val MonoOffWhite = Color(0xFFE8E8E8)
val MonoLightGrey = Color(0xFFC0C0C0)
val MonoGrey = Color(0xFF888888)
val MonoDarkGrey = Color(0xFF444444)

// Attendance Status Tokens (Monochrome / High-Contrast)
val MonoAttended = Color(0xFFFFFFFF)
val MonoMissed = Color(0xFF888888)
val MonoCancelled = Color(0xFF555555)

// Danger Red for subjects below individual target percentage
val MonoDangerRed = Color(0xFFFF3B30)
val MonoDangerRedBg = Color(0x28FF3B30)
val MonoDangerRedBorder = Color(0x88FF3B30)

// Threshold Color Grading Tokens: Green for above threshold, Red for below threshold
val MetricSuccessGreen = Color(0xFF34C759)
val MetricSuccessGreenBg = Color(0x2434C759)
val MetricSuccessGreenBorder = Color(0x6634C759)

val MetricDangerRed = Color(0xFFFF3B30)
val MetricDangerRedBg = Color(0x28FF3B30)
val MetricDangerRedBorder = Color(0x88FF3B30)

// Backward-compatible alias tokens for existing components
val GroovyBg = MonoBlack
val GroovySurface = MonoSurface
val GroovySurfaceCard = MonoSurfaceCard
val GroovySurfaceElevated = MonoSurfaceElevated
val GroovyBorder = MonoBorder
val GroovyBorderBright = MonoBorderBright

val GroovyMagenta = MonoWhite
val GroovyCyan = MonoOffWhite
val GroovyPurple = MonoLightGrey
val GroovyYellow = MonoWhite
val GroovyOrange = MonoLightGrey
val GroovyBlue = MonoGrey

val GroovyAttended = MonoAttended
val GroovyMissed = MonoMissed
val GroovyCancelled = MonoCancelled

val GroovyTextPrimary = MonoWhite
val GroovyTextSecondary = MonoLightGrey
val GroovyTextMuted = MonoGrey
