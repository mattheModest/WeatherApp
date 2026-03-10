package com.weatherapp.ui.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
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
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.weatherapp.MainActivity
import com.weatherapp.model.WeatherState
import com.weatherapp.model.WidgetDisplayState
import com.weatherapp.ui.theme.WeatherColorTokens
import com.weatherapp.ui.theme.WeatherDesignTokens

@Composable
fun WeatherWidgetContent(state: WidgetDisplayState) {
    val size = LocalSize.current
    val isMinimal = size.height < 110.dp
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme(context)
    val tokens = WeatherDesignTokens.getTokens(state.weatherState, isDark)

    GlanceTheme {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(tokens.background))
                .cornerRadius(16.dp)
                .clickable(actionStartActivity<MainActivity>(
                    actionParametersOf(ActionParameters.Key<Boolean>("open_hourly") to true)
                ))
                .semantics { contentDescription = buildContentDescription(state) }
        ) {
            if (isMinimal) {
                MinimalWidgetLayout(state = state, tokens = tokens)
            } else {
                FullWidgetLayout(state = state, tokens = tokens)
            }
        }
    }
}

@Composable
private fun MinimalWidgetLayout(
    state: WidgetDisplayState,
    tokens: WeatherColorTokens
) {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = weatherEmoji(state.weatherState),
            style = TextStyle(fontSize = 18.sp)
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Column(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = state.verdict,
                style = TextStyle(
                    color = ColorProvider(tokens.verdictText),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2
            )
            if (state.isStale) {
                Text(
                    text = formatStaleness(state.lastUpdateEpoch),
                    style = TextStyle(
                        color = ColorProvider(tokens.secondaryText),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun FullWidgetLayout(
    state: WidgetDisplayState,
    tokens: WeatherColorTokens
) {
    Column(modifier = GlanceModifier.fillMaxSize()) {

        // ── Top zone: weather emoji + bold verdict ──
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(ColorProvider(tokens.chipBackground))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = weatherEmoji(state.weatherState),
                style = TextStyle(fontSize = 26.sp)
            )
            Spacer(modifier = GlanceModifier.width(10.dp))
            Column {
                Text(
                    text = state.verdict,
                    style = TextStyle(
                        color = ColorProvider(tokens.verdictText),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2
                )
                if (state.bestWindow != null) {
                    Text(
                        text = "Good window: ${state.bestWindow}",
                        style = TextStyle(
                            color = ColorProvider(tokens.accentColor),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // ── Hairline divider ──
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ColorProvider(tokens.secondaryText.copy(alpha = 0.2f)))
        ) {}

        // ── Bottom zone: mood line + chips + accent dot ──
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(tokens.cardBackground))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (state.moodLine.isNotEmpty()) {
                Text(
                    text = state.moodLine,
                    style = TextStyle(
                        color = ColorProvider(tokens.secondaryText),
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic
                    ),
                    maxLines = 1
                )
                Spacer(modifier = GlanceModifier.height(6.dp))
            }
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                state.bringItems.take(2).forEachIndexed { index, item ->
                    if (index > 0) Spacer(modifier = GlanceModifier.width(4.dp))
                    BringChip(text = item, tokens = tokens)
                }
                Spacer(modifier = GlanceModifier.width(8.dp))
                // Accent dot — mirrors the app card
                Box(
                    modifier = GlanceModifier
                        .width(7.dp)
                        .height(7.dp)
                        .cornerRadius(4.dp)
                        .background(ColorProvider(tokens.accentColor))
                ) {}
            }
            if (state.isStale) {
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = formatStaleness(state.lastUpdateEpoch),
                    style = TextStyle(
                        color = ColorProvider(tokens.secondaryText),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun BringChip(text: String, tokens: WeatherColorTokens) {
    Box(
        modifier = GlanceModifier
            .background(ColorProvider(tokens.background))
            .cornerRadius(12.dp)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = ColorProvider(tokens.chipText),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

private fun weatherEmoji(state: WeatherState): String = when (state) {
    WeatherState.CLEAR    -> "☀️"
    WeatherState.OVERCAST -> "☁️"
    WeatherState.RAIN     -> "🌧"
    WeatherState.STORM    -> "⛈"
}

private fun buildContentDescription(state: WidgetDisplayState): String {
    val sb = StringBuilder(state.verdict)
    if (state.bringItems.isNotEmpty()) {
        sb.append(". Bring: ${state.bringItems.joinToString(", ")}")
    }
    if (state.bestWindow != null) {
        sb.append(". Best window: ${state.bestWindow}")
    }
    if (state.isStale) {
        sb.append(". Data may be outdated.")
    }
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
