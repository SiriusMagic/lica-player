package com.sren.music.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SrenColors = darkColorScheme(
    primary = Color(0xFFFFA24B),
    onPrimary = Color(0xFF241000),
    secondary = Color(0xFF8AC7FF),
    background = Color(0xFF0B0B0D),
    surface = Color(0xFF161617),
    surfaceVariant = Color(0xFF252528),
    onSurfaceVariant = Color(0xFFBDB7B1),
)

@Composable
fun SrenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SrenColors,
        typography = Typography(),
        content = content,
    )
}
