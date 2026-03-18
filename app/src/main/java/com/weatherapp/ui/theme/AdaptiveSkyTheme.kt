package com.weatherapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.lerp
import com.weatherapp.model.VisualTheme
import com.weatherapp.model.WeatherState

@Composable
fun AdaptiveSkyTheme(
    weatherState: WeatherState,
    darkTheme: Boolean,
    visualTheme: VisualTheme = VisualTheme.DEFAULT,
    content: @Composable () -> Unit
) {
    val tokens = visualTheme.toWidgetTokens(weatherState, darkTheme)

    // Derive selection/container colors from theme tokens so the Settings screen
    // doesn't fall back to Material3's purple defaults.
    val primaryContainer = lerp(tokens.cardBackground, tokens.accentColor, 0.25f)

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            background           = tokens.background,
            onBackground         = tokens.verdictText,
            surface              = tokens.chipBackground,
            onSurface            = tokens.secondaryText,
            surfaceVariant       = tokens.cardBackground,
            onSurfaceVariant     = tokens.secondaryText,
            primary              = tokens.accentColor,
            primaryContainer     = primaryContainer,
            onPrimaryContainer   = tokens.verdictText,
            secondary            = tokens.chipText
        )
    } else {
        lightColorScheme(
            background           = tokens.background,
            onBackground         = tokens.verdictText,
            surface              = tokens.chipBackground,
            onSurface            = tokens.secondaryText,
            surfaceVariant       = tokens.cardBackground,
            onSurfaceVariant     = tokens.secondaryText,
            primary              = tokens.accentColor,
            primaryContainer     = primaryContainer,
            onPrimaryContainer   = tokens.verdictText,
            secondary            = tokens.chipText
        )
    }

    // dynamicColor = false — Adaptive Sky palette always applies, never Material You
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
