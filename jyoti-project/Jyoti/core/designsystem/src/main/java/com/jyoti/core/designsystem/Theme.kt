package com.jyoti.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JyotiColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = JyotiBackgroundTop,
    secondary = NeonMagenta,
    onSecondary = JyotiBackgroundTop,
    tertiary = NeonViolet,
    background = JyotiBackgroundTop,
    onBackground = TextPrimary,
    surface = JyotiSurface,
    onSurface = TextPrimary,
    surfaceVariant = JyotiSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    error = StatusError
)

/**
 * App-wide theme. Every screen (current and future) should wrap itself in JyotiTheme
 * only once, at the MainActivity root — feature modules just consume MaterialTheme.colorScheme.
 */
@Composable
fun JyotiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = JyotiColorScheme,
        typography = JyotiTypography,
        content = content
    )
}
