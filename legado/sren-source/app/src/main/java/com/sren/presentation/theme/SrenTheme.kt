package com.sren.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SrenColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF7CF7D4),
    secondary = Color(0xFFFFD166),
    background = Color(0xFF090A10),
    surface = Color(0xFF12141D),
    surfaceVariant = Color(0xFF1C2030),
    onPrimary = Color(0xFF001B16),
    onSecondary = Color(0xFF1F1600),
    onBackground = Color(0xFFF4F7FB),
    onSurface = Color(0xFFF4F7FB),
    onSurfaceVariant = Color(0xFFB9C0D4)
)

@Composable
fun SrenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SrenColors,
        content = content
    )
}
