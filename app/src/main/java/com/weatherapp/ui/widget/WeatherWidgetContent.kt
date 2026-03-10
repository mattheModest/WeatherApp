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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.weatherapp.MainActivity
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
            when {
                state.isAllClear -> AllClearLayout(state = state, tokens = tokens)
                isMinimal        -> MinimalWidgetLayout(state = state, tokens = tokens)
                else             -> FullWidgetLayout(state = state, tokens = tokens)
            }
        }
    }
}

@Composable
private fun MinimalWidgetLayout(
    state: WidgetDisplayState,
    tokens: WeatherColorTokens
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = state.verdict,
            style = TextStyle(
                color = ColorProvider(tokens.verdictText),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 2
        )
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

@Composable
private fun AllClearLayout(
    state: WidgetDisplayState,
    tokens: WeatherColorTokens
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state.currentTempC != null) {
            Text(
                text = "${state.currentTempC.toInt()}°",
                style = TextStyle(
                    color = ColorProvider(tokens.verdictText),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
        }
        Text(
            text = state.verdict,
            style = TextStyle(
                color = ColorProvider(tokens.verdictText),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 2
        )
        if (state.moodLine.isNotEmpty()) {
            Spacer(modifier = GlanceModifier.height(6.dp))
            Text(
                text = state.moodLine,
                style = TextStyle(
                    color = ColorProvider(tokens.secondaryText),
                    fontSize = 13.sp
                ),
                maxLines = 1
            )
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

@Composable
private fun FullWidgetLayout(
    state: WidgetDisplayState,
    tokens: WeatherColorTokens
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        // Temperature + verdict top line
        if (state.currentTempC != null) {
            val tempDisplay = "${state.currentTempC.toInt()}°"
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Text(
                    text = tempDisplay,
                    style = TextStyle(
                        color = ColorProvider(tokens.verdictText),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.width(10.dp))
                Text(
                    text = state.verdict,
                    style = TextStyle(
                        color = ColorProvider(tokens.verdictText),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2
                )
            }
        } else {
            // Verdict line — primary, bold, large (AC-1)
            Text(
                text = state.verdict,
                style = TextStyle(
                    color = ColorProvider(tokens.verdictText),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2
            )
        }

        // Staleness indicator (AC-4)
        if (state.isStale) {
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = formatStaleness(state.lastUpdateEpoch),
                style = TextStyle(
                    color = ColorProvider(tokens.secondaryText),
                    fontSize = 10.sp
                )
            )
        }

        // Bring chips (AC-1)
        if (state.bringItems.isNotEmpty()) {
            Spacer(modifier = GlanceModifier.height(8.dp))
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                state.bringItems.take(3).forEachIndexed { index, item ->
                    if (index > 0) Spacer(modifier = GlanceModifier.width(4.dp))
                    BringChip(text = item, tokens = tokens)
                }
            }
        }

        // Best outdoor window (AC-1)
        if (state.bestWindow != null) {
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = "Best time: ${state.bestWindow}",
                style = TextStyle(
                    color = ColorProvider(tokens.secondaryText),
                    fontSize = 12.sp
                )
            )
        }

        // Mood line (AC-1)
        if (state.moodLine.isNotEmpty()) {
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = state.moodLine,
                style = TextStyle(
                    color = ColorProvider(tokens.secondaryText),
                    fontSize = 12.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BringChip(text: String, tokens: WeatherColorTokens) {
    Box(
        modifier = GlanceModifier
            .background(ColorProvider(tokens.chipBackground))
            .cornerRadius(12.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
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

private fun buildContentDescription(state: WidgetDisplayState): String {
    val sb = StringBuilder(state.verdict)
    if (state.bringItems.isNotEmpty()) {
        sb.append(". Bring: ${state.bringItems.joinToString(", ")}")
    }
    if (state.bestWindow != null) {
        sb.append(". Best time: ${state.bestWindow}")
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
