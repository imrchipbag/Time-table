package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GroovyDarkColorScheme = darkColorScheme(
    primary = GroovyMagenta,
    onPrimary = GroovyBg,
    primaryContainer = GroovySurfaceElevated,
    onPrimaryContainer = GroovyMagenta,
    secondary = GroovyCyan,
    onSecondary = GroovyBg,
    secondaryContainer = GroovySurfaceCard,
    onSecondaryContainer = GroovyCyan,
    tertiary = GroovyYellow,
    onTertiary = GroovyBg,
    background = GroovyBg,
    onBackground = GroovyTextPrimary,
    surface = GroovySurface,
    onSurface = GroovyTextPrimary,
    surfaceVariant = GroovySurfaceElevated,
    onSurfaceVariant = GroovyTextSecondary,
    outline = GroovyBorder,
    outlineVariant = GroovyBorderBright,
    error = GroovyMissed,
    onError = GroovyBg
)

@Composable
fun GrooveTrackTheme(
    darkTheme: Boolean = true, // Default to true for the Dark and Groovy aesthetic requested
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GroovyDarkColorScheme,
        typography = Typography,
        content = content
    )
}
