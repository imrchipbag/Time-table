package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MinimalMonoColorScheme = darkColorScheme(
    primary = MonoWhite,
    onPrimary = MonoBlack,
    primaryContainer = MonoSurfaceElevated,
    onPrimaryContainer = MonoWhite,
    secondary = MonoLightGrey,
    onSecondary = MonoBlack,
    secondaryContainer = MonoSurfaceCard,
    onSecondaryContainer = MonoOffWhite,
    tertiary = MonoWhite,
    onTertiary = MonoBlack,
    background = MonoBlack,
    onBackground = MonoWhite,
    surface = MonoSurface,
    onSurface = MonoWhite,
    surfaceVariant = MonoSurfaceElevated,
    onSurfaceVariant = MonoLightGrey,
    outline = MonoBorder,
    outlineVariant = MonoBorderBright,
    error = MonoMissed,
    onError = MonoBlack
)

@Composable
fun GrooveTrackTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MinimalMonoColorScheme,
        typography = Typography,
        content = content
    )
}
