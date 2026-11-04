package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NeonColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = NeonBackground,
    primaryContainer = NeonSurfaceVariant,
    onPrimaryContainer = NeonCyan,
    secondary = NeonPurple,
    onSecondary = TextPrimary,
    secondaryContainer = NeonSurfaceVariant,
    onSecondaryContainer = NeonPurple,
    tertiary = NeonMagenta,
    onTertiary = TextPrimary,
    background = NeonBackground,
    onBackground = TextPrimary,
    surface = NeonSurface,
    onSurface = TextPrimary,
    surfaceVariant = NeonSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = NeonCardBorder,
    error = DangerNeon,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NeonColorScheme,
        typography = Typography,
        content = content
    )
}
