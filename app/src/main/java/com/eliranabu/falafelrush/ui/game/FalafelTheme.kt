package com.eliranabu.falafelrush.ui.game

import androidx.compose.ui.graphics.Color

// --- Premium 2026 Color Palette & Theme Tokens ---
object FalafelRushTheme {
    val DarkSpaceBg = Color(0xFF0F0A1E)
    val GlassCardBg = Color(0x331C1A32)
    val GlowGreen = Color(0xFF00E676)
    val DeepGold = Color(0xFFFFB300)
    val BrightGold = Color(0xFFFFE082)
    val HotOrange = Color(0xFFFF5722)
    val NeonCyan = Color(0xFF00E5FF)
    val CrimsonRed = Color(0xFFFF1744)
    val DeepBlue = Color(0xFF1E1B4B)
    val CozyCreame = Color(0xFFFDFBF7)
}

// Utility extension functions to lighten or darken color tokens programmatically
fun Color.lighten(factor: Float): Color {
    return Color(
        red = (red + (1f - red) * factor).coerceIn(0f, 1f),
        green = (green + (1f - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}

fun Color.darken(factor: Float): Color {
    return Color(
        red = (red * (1f - factor)).coerceIn(0f, 1f),
        green = (green * (1f - factor)).coerceIn(0f, 1f),
        blue = (blue * (1f - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}
