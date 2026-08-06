package com.example.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val NeonGreen = Color(0xFF00FF66)
val NeonYellow = Color(0xFFFFFF00)
val CharcoalBlack = Color(0xFF121212)
val DeepCharcoal = Color(0xFF1E1E1E)
val LightText = Color(0xFFE0E0E0)

val CricbuzzGreen = Color(0xFF009270)
val OffWhite = Color(0xFFF2F2F7)
val CrispWhite = Color(0xFFFFFFFF)
val DarkText = Color(0xFF1C1C1E)
val MediumText = Color(0xFF8E8E93)

val PremiumGreen = Color(0xFF006C4C)
val PremiumBackground = Color(0xFFF8FAFC)
val PremiumSurface = Color(0xFFFFFFFF)
val PremiumTextDark = Color(0xFF0F172A)
val PremiumTextMedium = Color(0xFF475569)

val PremiumGradientLight = Brush.linearGradient(
    colors = listOf(
        Color(0xFFF1F5F9),
        Color(0xFFE2E8F0)
    ),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
)

val GlassBackgroundLight = Color(0xCCFFFFFF) // 80% opacity
val GlassBackgroundDark = Color(0xCC1E293B) // 80% opacity

val CardGradientLight = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFF8FAFC)
    )
)
