package com.sren.music.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/** Sren's own look: always dark, warm orange accent. */
private val SrenColors = darkColorScheme(
    primary = SrenOrange,
    onPrimary = OnSrenOrange,
    primaryContainer = SrenOrangeContainer,
    onPrimaryContainer = OnSrenOrangeContainer,
    secondary = SrenBlue,
    background = SrenBackground,
    surface = SrenSurface,
    surfaceVariant = SrenSurfaceVariant,
    onSurfaceVariant = SrenOnSurfaceVariant,
    surfaceContainer = SrenSurface,
    surfaceContainerLow = SrenSurface,
    surfaceContainerHigh = SrenSurfaceVariant,
)

@Composable
fun SrenTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = SrenColors, typography = Typography(), content = content)
}
