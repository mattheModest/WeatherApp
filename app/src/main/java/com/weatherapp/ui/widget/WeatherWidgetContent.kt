package com.weatherapp.ui.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontFamily
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.weatherapp.MainActivity
import com.weatherapp.model.VisualTheme
import com.weatherapp.model.WeatherState
import com.weatherapp.model.WidgetDisplayState
import com.weatherapp.ui.theme.WeatherColorTokens
import com.weatherapp.ui.theme.toWidgetTokens

@Composable
fun WeatherWidgetContent(state: WidgetDisplayState, visualTheme: VisualTheme = VisualTheme.DEFAULT) {
    val size = LocalSize.current
    val isMinimal = size.height < 80.dp
    val isMedium = size.height < 160.dp
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme(context)
    val tokens = visualTheme.toWidgetTokens(state.weatherState, isDark)
    // Only use the painter's-algorithm two-zone split when topZoneBackground is actually opaque.
    // Themes like INK, UTILITY, CHALK use near-transparent topZoneBackground (3–6% alpha) —
    // the solid accentColor box behind it bleeds through and looks garish (e.g. neon green).
    // For those themes, a single solid cardBackground matches the in-app card preview.
    val useZonedLayout = tokens.topZoneBackground.alpha >= 0.5f

    // Per-theme font family for Glance (limited to system fonts; no custom loading in RemoteViews)
    // CHALK_SLATE uses Serif — the in-app handwriting font has no Glance equivalent; Cursive maps
    // to whatever system script font is installed, which is often completely illegible.
    val verdictFont: FontFamily? = when (visualTheme) {
        VisualTheme.CHALK_SLATE -> FontFamily.Serif
        VisualTheme.SUN_STAINED_BEIGE, VisualTheme.UTILITY_CHIC,
        VisualTheme.EIGHT_BIT -> FontFamily.Monospace
        else -> null
    }
    val metaFont: FontFamily? = when (visualTheme) {
        VisualTheme.CHALK_SLATE -> FontFamily.Serif
        VisualTheme.SUN_STAINED_BEIGE, VisualTheme.UTILITY_CHIC,
        VisualTheme.EIGHT_BIT -> FontFamily.Monospace
        else -> null
    }
    // INK_EDITORIAL has a 3px amber stripe at the very top of its card — replicate in Glance
    val showTopEdgeStripe = visualTheme == VisualTheme.INK_EDITORIAL
    val showLiveBadge = visualTheme == VisualTheme.UTILITY_CHIC
    val useAsciiWeather = visualTheme == VisualTheme.EIGHT_BIT
    val topZoneHeight = 72.dp
    // Bottom zone darkening — only for non-zoned themes that can't use painter's two-zone
    val bottomZoneOverlay: Color = when (visualTheme) {
        VisualTheme.CHALK_SLATE -> Color(0x1E000000)   // 12% black — subtle bottom darkening
        else                    -> Color.Transparent
    }

    val clickAction = actionStartActivity<MainActivity>(
        actionParametersOf(ActionParameters.Key<Boolean>("open_hourly") to true)
    )

    if (isMinimal) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(if (useZonedLayout) tokens.topZoneBackground else tokens.cardBackground)
                .clickable(clickAction)
                .semantics { contentDescription = state.verdict }
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = weatherEmoji(state.weatherState, useAsciiWeather),
                    style = TextStyle(fontSize = 18.sp)
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = state.verdict,
                    style = TextStyle(
                        color = ColorProvider(tokens.verdictText),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = verdictFont
                    ),
                    maxLines = 2
                )
            }
        }
        return
    }

    if (isMedium) {
        val topHeight = size.height / 2
        val bottomHeight = size.height / 2
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable(clickAction)
                .semantics { contentDescription = buildContentDescription(state) }
        ) {
            Box(modifier = GlanceModifier.fillMaxSize().background(tokens.cardBackground)) {}
            if (!useZonedLayout && bottomZoneOverlay != Color.Transparent) {
                // Non-zoned themes: full dark overlay then cover top half back to cardBackground
                Box(modifier = GlanceModifier.fillMaxSize().background(bottomZoneOverlay)) {}
                Box(modifier = GlanceModifier.fillMaxWidth().height(topHeight).background(tokens.cardBackground)) {}
            }
            if (showTopEdgeStripe) {
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(tokens.accentColor)) {}
            }
            if (useZonedLayout) {
                Box(modifier = GlanceModifier.fillMaxWidth().height(topHeight + 2.dp).background(tokens.accentColor)) {}
                Box(modifier = GlanceModifier.fillMaxWidth().height(topHeight).background(tokens.topZoneBackground)) {}
            }
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(topHeight)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = weatherEmoji(state.weatherState, useAsciiWeather), style = TextStyle(fontSize = 30.sp))
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = state.verdict,
                        style = TextStyle(
                            color = ColorProvider(tokens.verdictText),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = verdictFont
                        ),
                        maxLines = 2
                    )
                    if (showLiveBadge) {
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = GlanceModifier
                                    .width(5.dp)
                                    .height(5.dp)
                                    .cornerRadius(3.dp)
                                    .background(tokens.accentColor)
                            ) {}
                            Text(
                                text = "LIVE",
                                style = TextStyle(
                                    color = ColorProvider(tokens.accentColor),
                                    fontSize = 7.sp,
                                    fontFamily = metaFont
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = GlanceModifier.height(2.dp))
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(bottomHeight - 2.dp)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (state.moodLine.isNotEmpty()) {
                            Text(
                                text = state.moodLine,
                                style = TextStyle(
                                    color = ColorProvider(tokens.secondaryText),
                                    fontSize = 11.sp,
                                    fontStyle = FontStyle.Italic,
                                    fontFamily = metaFont
                                ),
                                maxLines = 2
                            )
                            Spacer(modifier = GlanceModifier.height(4.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            state.bringItems.take(2).forEachIndexed { index, item ->
                                if (index > 0) Spacer(modifier = GlanceModifier.width(6.dp))
                                Box(
                                    modifier = GlanceModifier
                                        .background(tokens.chipBackground)
                                        .cornerRadius(10.dp)
                                        .padding(horizontal = 6.dp, vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item,
                                        style = TextStyle(
                                            color = ColorProvider(tokens.chipText),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = metaFont
                                        )
                                    )
                                }
                            }
                            if (state.bestWindow != null) {
                                Spacer(modifier = GlanceModifier.width(8.dp))
                                Text(
                                    text = "Good window: ${state.bestWindow}",
                                    style = TextStyle(
                                        color = ColorProvider(tokens.accentColor),
                                        fontSize = 10.sp,
                                        fontFamily = metaFont
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        return
    }

    // ── Full layout ───────────────────────────────────────────────────────────
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(clickAction)
            .semantics { contentDescription = buildContentDescription(state) }
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(tokens.cardBackground)
        ) {}

        if (!useZonedLayout && bottomZoneOverlay != Color.Transparent) {
            Box(modifier = GlanceModifier.fillMaxSize().background(bottomZoneOverlay)) {}
            Box(modifier = GlanceModifier.fillMaxWidth().height(topZoneHeight).background(tokens.cardBackground)) {}
        }

        if (showTopEdgeStripe) {
            Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(tokens.accentColor)) {}
        }

        if (useZonedLayout) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(topZoneHeight + 2.dp)
                    .background(tokens.accentColor)
            ) {}

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(topZoneHeight)
                    .background(tokens.topZoneBackground)
            ) {}
        }

        Column(modifier = GlanceModifier.fillMaxSize()) {

            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(topZoneHeight)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = weatherEmoji(state.weatherState, useAsciiWeather),
                    style = TextStyle(fontSize = 26.sp)
                )
                Spacer(modifier = GlanceModifier.width(10.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = state.verdict,
                        style = TextStyle(
                            color = ColorProvider(tokens.verdictText),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = verdictFont
                        ),
                        maxLines = 2
                    )
                    if (state.bestWindow != null) {
                        Text(
                            text = "Good window: ${state.bestWindow}",
                            style = TextStyle(
                                color = ColorProvider(tokens.accentColor),
                                fontSize = 11.sp,
                                fontFamily = metaFont
                            )
                        )
                    }
                }
                if (showLiveBadge) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = GlanceModifier
                                .width(6.dp)
                                .height(6.dp)
                                .cornerRadius(3.dp)
                                .background(tokens.accentColor)
                        ) {}
                        Text(
                            text = "LIVE",
                            style = TextStyle(
                                color = ColorProvider(tokens.accentColor),
                                fontSize = 8.sp,
                                fontFamily = metaFont
                            )
                        )
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(2.dp))

            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (state.moodLine.isNotEmpty()) {
                    Text(
                        text = state.moodLine,
                        style = TextStyle(
                            color = ColorProvider(tokens.secondaryText),
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            fontFamily = metaFont
                        ),
                        maxLines = 2
                    )
                    Spacer(modifier = GlanceModifier.height(6.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    state.bringItems.take(2).forEachIndexed { index, item ->
                        if (index > 0) Spacer(modifier = GlanceModifier.width(6.dp))
                        Box(
                            modifier = GlanceModifier
                                .background(tokens.chipBackground)
                                .cornerRadius(12.dp)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item,
                                style = TextStyle(
                                    color = ColorProvider(tokens.chipText),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = metaFont
                                )
                            )
                        }
                    }
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    // Accent dot — skip for UTILITY (LIVE badge handles it)
                    if (!showLiveBadge) {
                        Box(
                            modifier = GlanceModifier
                                .width(7.dp)
                                .height(7.dp)
                                .cornerRadius(4.dp)
                                .background(tokens.accentColor)
                        ) {}
                    }
                }
                if (state.isStale) {
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = formatStaleness(state.lastUpdateEpoch),
                        style = TextStyle(
                            color = ColorProvider(tokens.secondaryText),
                            fontSize = 10.sp,
                            fontFamily = metaFont
                        )
                    )
                }
            }
        }
    }
}

private fun weatherEmoji(state: WeatherState, useAscii: Boolean = false): String {
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

private fun buildContentDescription(state: WidgetDisplayState): String {
    val sb = StringBuilder(state.verdict)
    if (state.bringItems.isNotEmpty()) sb.append(". Bring: ${state.bringItems.joinToString(", ")}")
    if (state.bestWindow != null) sb.append(". Best window: ${state.bestWindow}")
    if (state.isStale) sb.append(". Data may be outdated.")
    return sb.toString()
}

private fun formatStaleness(lastUpdateEpoch: Long): String {
    if (lastUpdateEpoch == 0L) return "Not yet updated"
    val minutesAgo = (System.currentTimeMillis() / 1000L - lastUpdateEpoch) / 60
    return "Last updated ${minutesAgo}m ago"
}

private fun isSystemInDarkTheme(context: Context): Boolean {
    val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return nightMode == Configuration.UI_MODE_NIGHT_YES
}
