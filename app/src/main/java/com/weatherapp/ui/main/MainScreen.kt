package com.weatherapp.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalUriHandler
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
import com.weatherapp.ui.hourly.HourlyDetailBottomSheet
import com.weatherapp.ui.hourly.HourlyDetailRow
import com.weatherapp.ui.hourly.HourlyDetailViewModel
import com.weatherapp.ui.theme.VisualThemeStyle
import com.weatherapp.ui.theme.WeatherColorTokens
import com.weatherapp.ui.theme.toStyle
import com.weatherapp.util.UiState
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect

@Composable
fun MainScreen(
    displayState: WidgetDisplayState,
    tempUnit: String,
    showHourlySheet: Boolean,
    updateInfo: UpdateInfo?,
    visualTheme: VisualTheme = VisualTheme.DEFAULT,
    onOpenHourly: () -> Unit,
    onCloseHourly: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismissUpdate: () -> Unit,
    hourlyViewModel: HourlyDetailViewModel = hiltViewModel()
) {
    val isDark = isSystemInDarkTheme()
    val style = visualTheme.toStyle(displayState.weatherState, isDark)
    val hourlyState by hourlyViewModel.uiState.collectAsStateWithLifecycle()

    val screenTransition = rememberInfiniteTransition(label = "screen")
    val screenGlowAlpha by screenTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(8000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "screenGlow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(style.tokens.background)
    ) {
        if (style.hasScreenGlow) {
            Box(modifier = Modifier.fillMaxSize().drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(style.screenGlowColor1, Color.Transparent),
                        center = Offset(-size.width * 0.12f, -size.height * 0.08f),
                        radius = size.width * 1.1f
                    ),
                    alpha = screenGlowAlpha
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(style.screenGlowColor2, Color.Transparent),
                        center = Offset(size.width * 1.1f, size.height * 1.05f),
                        radius = size.width * 1.0f
                    ),
                    alpha = screenGlowAlpha
                )
            })
        }
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
                    ForecastCard(hourlyState, style)
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

    if (showHourlySheet) {
        HourlyDetailBottomSheet(onDismiss = onCloseHourly)
    }
}

@Composable
private fun WeatherCard(displayState: WidgetDisplayState, style: VisualThemeStyle) {
    val tokens = style.tokens
    val shape = RoundedCornerShape(style.cardCornerRadius)

    val cardTransition = rememberInfiniteTransition(label = "card")

    val animatedDotColor by cardTransition.animateColor(
        initialValue = tokens.accentColor,
        targetValue = if (style.dotAnimatesColor) style.dotAnimationTargetColor else tokens.accentColor,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Reverse),
        label = "dotColor"
    )

    val blinkDotAlpha by cardTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (style.hasDotBlink) 0f else 1f,
        animationSpec = if (style.hasDotBlink) infiniteRepeatable(
            keyframes {
                durationMillis = 1400
                1f at 0
                1f at 699
                0f at 700
                0f at 1399
            }
        ) else infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "blinkDot"
    )

    val cursorAlpha by cardTransition.animateFloat(
        initialValue = if (style.hasBlinkingCursor) 1f else 0f,
        targetValue = 0f,
        animationSpec = if (style.hasBlinkingCursor) infiniteRepeatable(
            keyframes {
                durationMillis = 1000
                1f at 0
                1f at 499
                0f at 500
                0f at 999
            }
        ) else infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "cursor"
    )

    // Neo-Brutalism: outer Box reserves space for hard shadow; padding only applied when shadow is active
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (style.hasBrutalistShadow)
                    Modifier.padding(end = style.brutalistShadowOffset, bottom = style.brutalistShadowOffset)
                else Modifier
            )
    ) {
        if (style.hasBrutalistShadow) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(
                        x = style.brutalistShadowOffset,
                        y = style.brutalistShadowOffset
                    )
                    .background(style.brutalistShadowColor)
            )
        }

        // Scanline overlay wrapper
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .then(
                        if (style.cardBorderWidth > 0.dp)
                            Modifier.border(style.cardBorderWidth, style.cardBorderColor, shape)
                        else Modifier
                    )
                    .background(tokens.cardBackground)
            ) {

                // ── Ink: top-edge amber stripe ────────────────────────────────────
                if (style.hasTopEdgeStripe) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        style.topEdgeStripeColor.copy(alpha = 0.95f),
                                        style.topEdgeStripeColor.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                // ── BEIGE: WEATHER.EXE titlebar ──────────────────────────────────────
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

                // ── Top zone: emoji + verdict + bestWindow (+ LIVE badge for UTILITY) ─
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
                        // Chalk: multi-layer glow on verdict text
                        if (style.verdictGlowColor != Color.Transparent) {
                            Box(contentAlignment = Alignment.TopStart) {
                                Text(
                                    text = displayState.verdict,
                                    fontSize = 26.sp,
                                    fontWeight = style.verdictWeight,
                                    fontFamily = style.titleFontFamily,
                                    color = style.verdictGlowColor.copy(alpha = 0.35f),
                                    lineHeight = 32.sp,
                                )
                                Text(
                                    text = displayState.verdict,
                                    fontSize = 26.sp,
                                    fontWeight = style.verdictWeight,
                                    fontFamily = style.titleFontFamily,
                                    color = tokens.verdictText,
                                    lineHeight = 32.sp
                                )
                            }
                        } else {
                            Text(
                                text = displayState.verdict,
                                fontSize = 26.sp,
                                fontWeight = style.verdictWeight,
                                fontFamily = style.titleFontFamily,
                                color = tokens.verdictText,
                                lineHeight = 32.sp
                            )
                        }
                        if (displayState.bestWindow != null) {
                            Text(
                                text = "Good window: ${displayState.bestWindow}",
                                fontSize = 13.sp,
                                fontFamily = style.metaFontFamily,
                                color = tokens.accentColor
                            )
                        }
                    }
                    // UTILITY: LIVE badge
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

                // ── Zone divider ──────────────────────────────────────────────────
                when {
                    style.showTitlebar -> {
                        // Beige: 3-layer bevel divider
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF7A6840)))
                            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFFA89060)))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE8DCC0)))
                        }
                    }
                    style.hasDashedDivider -> {
                        // Chalk: dashed line
                        Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                            drawLine(
                                color = Color(0x59F0EBDC),
                                start = Offset(0f, size.height / 2),
                                end = Offset(size.width, size.height / 2),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                            )
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(tokens.chipBackground.copy(alpha = 0.25f))
                        )
                    }
                }

                // ── Bottom zone: mood line + chips + accent dot ───────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (style.bottomZoneBackground != Color.Transparent)
                                Modifier.background(style.bottomZoneBackground)
                            else Modifier
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (displayState.moodLine.isNotEmpty()) {
                        if (style.showPullQuoteBorder) {
                            // INK: pull-quote with left accent border
                            val borderColor = style.pullQuoteBorderColor
                            Text(
                                text = displayState.moodLine,
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic,
                                fontFamily = style.metaFontFamily,
                                color = tokens.secondaryText,
                                lineHeight = 20.sp,
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .drawBehind {
                                        drawLine(
                                            color = borderColor,
                                            start = Offset(-8.dp.toPx(), 0f),
                                            end = Offset(-8.dp.toPx(), size.height),
                                            strokeWidth = 2.dp.toPx()
                                        )
                                    }
                            )
                        } else {
                            Text(
                                text = displayState.moodLine,
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic,
                                fontFamily = style.metaFontFamily,
                                color = tokens.secondaryText,
                                lineHeight = 20.sp
                            )
                        }
                        // Beige: blinking cursor after mood text
                        if (style.hasBlinkingCursor) {
                            Box(
                                modifier = Modifier
                                    .width(7.dp)
                                    .height(12.dp)
                                    .background(tokens.secondaryText.copy(alpha = cursorAlpha))
                            )
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
                                        .then(
                                            if (style.chipBorderWidth > 0.dp)
                                                Modifier.border(style.chipBorderWidth, style.chipBorderColor, chipShape)
                                            else Modifier
                                        )
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
                        // Accent dot — animated (Default), blinking (Utility), or static
                        when {
                            style.showPixelIndicator -> Text(
                                text = "> _",
                                fontSize = 10.sp,
                                fontFamily = style.metaFontFamily,
                                color = tokens.accentColor
                            )
                            style.dotAnimatesColor -> Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(animatedDotColor)
                            )
                            style.hasDotBlink -> Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(tokens.accentColor.copy(alpha = blinkDotAlpha))
                            )
                            else -> Box(
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
            } // Column

            // Utility: CRT scanline overlay
            if (style.hasScanlineOverlay) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(style.cardCornerRadius))
                        .drawBehind {
                            val lineSpacing = 4.dp.toPx()
                            val lineColor = Color(0x0F000000)
                            var y = 3.dp.toPx()
                            while (y < size.height) {
                                drawLine(
                                    color = lineColor,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                                y += lineSpacing
                            }
                        }
                )
            }
        } // scanline wrapper Box
    } // shadow Box
}

@Composable
private fun ForecastCard(
    hourlyState: UiState<List<HourlyDetailRow>>,
    style: VisualThemeStyle
) {
    val tokens = style.tokens
    val shape = RoundedCornerShape(style.cardCornerRadius)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (style.hasBrutalistShadow)
                    Modifier.padding(end = style.brutalistShadowOffset, bottom = style.brutalistShadowOffset)
                else Modifier
            )
    ) {
        if (style.hasBrutalistShadow) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(
                        x = style.brutalistShadowOffset,
                        y = style.brutalistShadowOffset
                    )
                    .background(style.brutalistShadowColor)
            )
        }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(
                if (style.cardBorderWidth > 0.dp)
                    Modifier.border(style.cardBorderWidth, style.cardBorderColor, shape)
                else Modifier
            )
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
    } // Column
    } // shadow Box
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
        WeatherState.OVERCAST -> "☁️"
        WeatherState.RAIN     -> "🌧"
        WeatherState.STORM    -> "⛈"
    }
}
