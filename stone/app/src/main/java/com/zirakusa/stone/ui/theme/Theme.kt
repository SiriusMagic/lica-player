package com.zirakusa.stone.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/** Aspecto de Stone: oscuro, acento amarillo. */
private val StoneColors = darkColorScheme(
    primary = StoneYellow,
    onPrimary = OnStoneYellow,
    primaryContainer = StoneYellowContainer,
    onPrimaryContainer = OnStoneYellowContainer,
    secondary = StoneAmber,
    background = StoneBackground,
    surface = StoneBackground,
    surfaceVariant = StoneSurfaceHighlight,
    onSurfaceVariant = StoneTextSecondary,
    surfaceContainer = StoneSurface,
    surfaceContainerLow = StoneSurface,
    surfaceContainerHigh = StoneSurfaceHighlight,
    outline = StoneSurfaceHighlight,
    outlineVariant = StoneSurfaceHighlight,
    error = StoneError,
)

@Composable
fun StoneTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = StoneColors, typography = Typography(), content = content)
}
