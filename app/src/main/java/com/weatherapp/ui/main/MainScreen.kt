package com.weatherapp.ui.main

import android.content.Intent
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
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weatherapp.data.update.UpdateInfo
import com.weatherapp.model.VisualTheme
import com.weatherapp.model.WeatherState
import com.weatherapp.model.WidgetDisplayState
import com.weatherapp.ui.hourly.HourlyDetailRow
import com.weatherapp.ui.hourly.HourlyDetailViewModel
import com.weatherapp.ui.theme.VisualThemeStyle
import com.weatherapp.ui.theme.WeatherColorTokens
import com.weatherapp.ui.theme.toStyle
import com.weatherapp.util.UiState

@Composable
fun MainScreen(
    displayState: WidgetDisplayState,
    updateInfo: UpdateInfo?,
    visualTheme: VisualTheme = VisualTheme.DEFAULT,
    onOpenSettings: () -> Unit,
    onDismissUpdate: () -> Unit,
    hourlyViewModel: HourlyDetailViewModel = hiltViewModel()
) {
    val isDark = isSystemInDarkTheme()
    val style = visualTheme.toStyle(displayState.weatherState, isDark)
    val hourlyState by hourlyViewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(style.tokens.background)
    ) {
        if (displayState.verdict.isEmpty() || displayState.verdict == "Loading...") {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = style.tokens.accentColor
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
                    WeatherCard(displayState, style)
                }
                item {
                    ForecastCard(hourlyState, style, displayState.lastUpdateEpoch)
                }
                if (updateInfo != null) {
                    item {
                        UpdateBanner(
                            updateInfo = updateInfo,
                            onDismiss = onDismissUpdate,
                            tokens = style.tokens
                        )
                    }
                }
            }
        }

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
                tint = style.tokens.secondaryText
            )
        }
    }

}

@Composable
private fun WeatherCard(displayState: WidgetDisplayState, style: VisualThemeStyle) {
    val tokens = style.tokens
    val shape = RoundedCornerShape(style.cardCornerRadius)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(tokens.cardBackground)
    ) {

        // ── INK: solid amber stripe at very top ───────────────────────────────
        if (style.hasTopEdgeStripe) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(style.topEdgeStripeColor)
            )
        }

        // ── BEIGE: retro titlebar ─────────────────────────────────────────────
        if (style.showTitlebar) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(style.titlebarBg)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "WEATHER.EXE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = style.titleFontFamily,
                    color = style.titlebarTextColor,
                    letterSpacing = 0.08.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .background(tokens.cardBackground)
                        )
                    }
                }
            }
        }

        // ── Top zone: emoji + verdict + bestWindow (+ LIVE badge for UTILITY) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(tokens.topZoneBackground)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = weatherSymbol(displayState.weatherState, style.useAsciiWeather),
                fontSize = 40.sp
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = displayState.verdict,
                    fontSize = 26.sp,
                    fontWeight = style.verdictWeight,
                    fontFamily = style.titleFontFamily,
                    color = tokens.verdictText,
                    lineHeight = 32.sp,
                    modifier = Modifier.semantics { heading() }
                )
                if (displayState.bestWindow != null) {
                    Text(
                        text = "Good window: ${displayState.bestWindow}",
                        fontSize = 13.sp,
                        fontFamily = style.metaFontFamily,
                        color = tokens.accentColor
                    )
                }
            }
            // UTILITY: static LIVE badge
            if (style.showLiveBadge) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(tokens.accentColor)
                    )
                    Text(
                        text = "LIVE",
                        fontSize = 8.sp,
                        fontFamily = style.metaFontFamily,
                        color = tokens.accentColor
                    )
                }
            }
        }

        // ── Zone divider ──────────────────────────────────────────────────────
        if (style.showTitlebar) {
            // Beige: 3-layer bevel
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(androidx.compose.ui.graphics.Color(0xFF7A6840)))
                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(androidx.compose.ui.graphics.Color(0xFFA89060)))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(androidx.compose.ui.graphics.Color(0xFFE8DCC0)))
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(tokens.chipBackground.copy(alpha = 0.25f))
            )
        }

        // ── Bottom zone: mood line + chips + accent dot ───────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (style.bottomZoneBackground != androidx.compose.ui.graphics.Color.Transparent)
                        Modifier.background(style.bottomZoneBackground)
                    else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (displayState.moodLine.isNotEmpty()) {
                val context = LocalContext.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = displayState.moodLine,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        fontFamily = style.metaFontFamily,
                        color = tokens.secondaryText,
                        lineHeight = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "\"${displayState.moodLine}\"\n— WeatherApp")
                            }
                            context.startActivity(Intent.createChooser(intent, null))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share today's mood",
                            tint = tokens.secondaryText.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    displayState.bringItems.take(2).forEach { item ->
                        val chipShape = RoundedCornerShape(style.chipCornerRadius)
                        Box(
                            modifier = Modifier
                                .clip(chipShape)
                                .background(tokens.chipBackground)
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item,
                                fontSize = 12.sp,
                                fontFamily = style.metaFontFamily,
                                color = tokens.chipText
                            )
                        }
                    }
                }
                // EIGHT_BIT: pixel indicator; all others: static accent dot
                if (style.showPixelIndicator) {
                    Text(
                        text = "> _",
                        fontSize = 10.sp,
                        fontFamily = style.metaFontFamily,
                        color = tokens.accentColor
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(tokens.accentColor)
                    )
                }
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
                    fontFamily = style.metaFontFamily,
                    color = tokens.secondaryText.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ForecastCard(
    hourlyState: UiState<List<HourlyDetailRow>>,
    style: VisualThemeStyle,
    lastUpdateEpoch: Long = 0L
) {
    val tokens = style.tokens
    val shape = RoundedCornerShape(style.cardCornerRadius)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(tokens.cardBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(tokens.topZoneBackground)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Today's forecast",
                fontSize = 18.sp,
                fontWeight = style.verdictWeight,
                fontFamily = style.titleFontFamily,
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
                if (state.data.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (lastUpdateEpoch == 0L) {
                            CircularProgressIndicator(color = tokens.accentColor)
                        } else {
                            Text("No forecast available", fontSize = 14.sp, color = tokens.secondaryText)
                        }
                    }
                } else {
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

private fun weatherSymbol(state: WeatherState, useAscii: Boolean = false): String {
    if (useAscii) return when (state) {
        WeatherState.CLEAR    -> "[*]"
        WeatherState.OVERCAST -> "[=]"
        WeatherState.RAIN     -> "[|]"
        WeatherState.STORM    -> "[!]"
    }
    return when (state) {
        WeatherState.CLEAR    -> "☀️"
        WeatherState.OVERCAST -> "🌥️"
        WeatherState.RAIN     -> "🌧"
        WeatherState.STORM    -> "⛈"
    }
}
