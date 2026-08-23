package com.mohit.videoskipper.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val AppColorScheme = lightColorScheme(
    primary = Ocean700,
    onPrimary = SurfaceWhite,
    primaryContainer = Ocean100,
    onPrimaryContainer = Ocean900,

    secondary = Signal500,
    onSecondary = SurfaceWhite,
    secondaryContainer = Signal100,
    onSecondaryContainer = Signal500,

    error = Coral500,
    onError = SurfaceWhite,
    errorContainer = Coral100,
    onErrorContainer = Coral500,

    background = Sand50,
    onBackground = Ink900,

    surface = SurfaceWhite,
    onSurface = Ink900,
    surfaceVariant = Sand100,
    onSurfaceVariant = Ink600,

    outline = Sand200
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * Single theme wrapper for the whole app. Wrap your top-level NavHost (or
 * each screen's preview) in this so every screen shares one identity:
 *
 *   setContent { VideoSkipperTheme { AppNavHost() } }
 */
@Composable
fun VideoSkipperTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}