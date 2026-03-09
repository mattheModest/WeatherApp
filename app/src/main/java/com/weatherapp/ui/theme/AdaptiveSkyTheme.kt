package com.weatherapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.weatherapp.model.WeatherState

@Composable
fun AdaptiveSkyTheme(
    weatherState: WeatherState,
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val tokens = WeatherDesignTokens.getTokens(weatherState, darkTheme)

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            background  = tokens.background,
            onBackground = tokens.verdictText,
            surface     = tokens.chipBackground,
            onSurface   = tokens.chipText,
            primary     = tokens.accentColor,
            secondary   = tokens.secondaryText
        )
    } else {
        lightColorScheme(
            background  = tokens.background,
            onBackground = tokens.verdictText,
            surface     = tokens.chipBackground,
            onSurface   = tokens.chipText,
            primary     = tokens.accentColor,
            secondary   = tokens.secondaryText
        )
    }

    // dynamicColor = false — Adaptive Sky palette always applies, never Material You
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
