package com.weatherapp.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherapp.model.WidgetDisplayState
import com.weatherapp.ui.hourly.HourlyDetailBottomSheet
import com.weatherapp.ui.theme.WeatherDesignTokens
import kotlin.math.roundToInt

@Composable
fun MainScreen(
    displayState: WidgetDisplayState,
    tempUnit: String,
    showHourlySheet: Boolean,
    onOpenHourly: () -> Unit,
    onCloseHourly: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val tokens = WeatherDesignTokens.getTokens(displayState.weatherState, isDark)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tokens.background)
    ) {
        // Settings button — top-right, status bar safe
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Open settings",
                tint = tokens.secondaryText
            )
        }

        // Main content
        if (displayState.verdict.isEmpty() || displayState.verdict == "Loading...") {
            // Loading state
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = tokens.accentColor
            )
        } else {
            // Weather content — centered column
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Temperature hero
                displayState.currentTempC?.let { tempC ->
                    val displayTemp = if (tempUnit == "fahrenheit") {
                        "${(tempC * 9f / 5f + 32f).roundToInt()}°"
                    } else {
                        "${tempC.roundToInt()}°"
                    }
                    Text(
                        text = displayTemp,
                        fontSize = 80.sp,
                        fontWeight = FontWeight.ExtraLight,
                        color = tokens.verdictText.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }

                // Verdict text
                Text(
                    text = displayState.verdict,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = tokens.verdictText,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )

                // Mood line
                if (displayState.moodLine.isNotEmpty()) {
                    Text(
                        text = displayState.moodLine,
                        fontSize = 15.sp,
                        color = tokens.secondaryText,
                        textAlign = TextAlign.Center,
                        fontStyle = FontStyle.Italic,
                        maxLines = 2
                    )
                }

                // Bring items chips — max 2
                if (displayState.bringItems.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        displayState.bringItems.take(2).forEach { item ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(tokens.chipBackground)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item,
                                    fontSize = 12.sp,
                                    color = tokens.chipText
                                )
                            }
                        }
                    }
                }

                // Best window
                if (displayState.bestWindow != null) {
                    Text(
                        text = "Best time: ${displayState.bestWindow}",
                        fontSize = 13.sp,
                        color = tokens.secondaryText,
                        textAlign = TextAlign.Center
                    )
                }

                // Staleness indicator
                if (displayState.isStale) {
                    val minutesAgo = if (displayState.lastUpdateEpoch > 0L) {
                        (System.currentTimeMillis() / 1000L - displayState.lastUpdateEpoch) / 60
                    } else null
                    val staleText = if (minutesAgo != null) "Updated ${minutesAgo} min ago" else "Data may be outdated"
                    Text(
                        text = staleText,
                        fontSize = 11.sp,
                        color = tokens.secondaryText,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Bottom CTA
        TextButton(
            onClick = onOpenHourly,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = "See today's forecast \u2192",
                fontSize = 14.sp,
                color = tokens.accentColor
            )
        }
    }

    // Hourly sheet overlay
    if (showHourlySheet) {
        HourlyDetailBottomSheet(onDismiss = onCloseHourly)
    }
}
