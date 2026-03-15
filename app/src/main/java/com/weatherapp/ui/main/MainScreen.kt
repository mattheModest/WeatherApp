package com.weatherapp.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weatherapp.data.update.UpdateInfo
import com.weatherapp.model.WeatherState
import com.weatherapp.model.WidgetDisplayState
import com.weatherapp.ui.hourly.HourlyDetailBottomSheet
import com.weatherapp.ui.hourly.HourlyDetailRow
import com.weatherapp.ui.hourly.HourlyDetailViewModel
import com.weatherapp.ui.theme.WeatherColorTokens
import com.weatherapp.ui.theme.WeatherDesignTokens
import com.weatherapp.util.UiState

@Composable
fun MainScreen(
    displayState: WidgetDisplayState,
    tempUnit: String,
    showHourlySheet: Boolean,
    updateInfo: UpdateInfo?,
    onOpenHourly: () -> Unit,
    onCloseHourly: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismissUpdate: () -> Unit,
    hourlyViewModel: HourlyDetailViewModel = hiltViewModel()
) {
    val isDark = isSystemInDarkTheme()
    val tokens = WeatherDesignTokens.getTokens(displayState.weatherState, isDark)
    val hourlyState by hourlyViewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(tokens.background)
    ) {
        if (displayState.verdict.isEmpty() || displayState.verdict == "Loading...") {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = tokens.accentColor
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 56.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    WeatherCard(displayState, tokens)
                }
                item {
                    ForecastCard(hourlyState, tokens)
                }
                if (updateInfo != null) {
                    item {
                        UpdateBanner(
                            updateInfo = updateInfo,
                            onDismiss = onDismissUpdate,
                            tokens = tokens
                        )
                    }
                }
            }
        }

        // Settings button — declared last so it sits on top of the scroll content
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
    }

    if (showHourlySheet) {
        HourlyDetailBottomSheet(onDismiss = onCloseHourly)
    }
}

@Composable
private fun WeatherCard(displayState: WidgetDisplayState, tokens: WeatherColorTokens) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(tokens.cardBackground)
    ) {
        // Top zone: emoji + verdict + best window
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(tokens.topZoneBackground)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = weatherEmoji(displayState.weatherState),
                fontSize = 40.sp
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = displayState.verdict,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tokens.verdictText,
                    lineHeight = 32.sp
                )
                if (displayState.bestWindow != null) {
                    Text(
                        text = "Good window: ${displayState.bestWindow}",
                        fontSize = 13.sp,
                        color = tokens.accentColor
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(tokens.chipBackground.copy(alpha = 0.25f))
        )

        // Bottom zone: mood line + chips + accent dot
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (displayState.moodLine.isNotEmpty()) {
                Text(
                    text = displayState.moodLine,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    color = tokens.secondaryText,
                    lineHeight = 20.sp
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    displayState.bringItems.take(2).forEach { item ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(tokens.chipBackground)
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = item, fontSize = 12.sp, color = tokens.chipText)
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(tokens.accentColor)
                )
            }
        }

        if (displayState.isStale) {
            val staleText = if (displayState.lastUpdateEpoch > 0L) {
                val minutesAgo = (System.currentTimeMillis() / 1000L - displayState.lastUpdateEpoch) / 60
                "Last updated ${minutesAgo}m ago · may be outdated"
            } else "Data may be outdated"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tokens.chipBackground.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = staleText,
                    fontSize = 11.sp,
                    color = tokens.secondaryText.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ForecastCard(
    hourlyState: UiState<List<HourlyDetailRow>>,
    tokens: WeatherColorTokens
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(tokens.cardBackground)
    ) {
        // Header — same top zone style as the weather card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(tokens.topZoneBackground)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Today's forecast",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = tokens.verdictText
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(tokens.chipBackground.copy(alpha = 0.25f))
        )

        when (val state = hourlyState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = tokens.accentColor)
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message, fontSize = 14.sp, color = tokens.secondaryText)
                }
            }
            is UiState.Success -> {
                state.data.forEachIndexed { index, row ->
                    HourlyDetailRow(row = row)
                    if (index < state.data.lastIndex) {
                        HorizontalDivider(
                            color = tokens.chipBackground.copy(alpha = 0.25f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateBanner(
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit,
    tokens: WeatherColorTokens
) {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(tokens.chipBackground)
            .clickable { uriHandler.openUri(updateInfo.downloadUrl) }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Update available — v${updateInfo.latestVersion}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = tokens.chipText
            )
            Text(
                text = "Tap to download",
                fontSize = 11.sp,
                color = tokens.chipText.copy(alpha = 0.6f)
            )
        }
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss update",
                tint = tokens.chipText.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun weatherEmoji(state: WeatherState): String = when (state) {
    WeatherState.CLEAR    -> "☀️"
    WeatherState.OVERCAST -> "☁️"
    WeatherState.RAIN     -> "🌧"
    WeatherState.STORM    -> "⛈"
}
