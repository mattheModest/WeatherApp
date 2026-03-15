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
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.weatherapp.MainActivity
import com.weatherapp.model.WeatherState
import com.weatherapp.model.WidgetDisplayState
import com.weatherapp.ui.theme.WeatherDesignTokens

@Composable
fun WeatherWidgetContent(state: WidgetDisplayState) {
    val size = LocalSize.current
    val isMinimal = size.height < 110.dp
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme(context)
    val tokens = WeatherDesignTokens.getTokens(state.weatherState, isDark)

    val clickAction = actionStartActivity<MainActivity>(
        actionParametersOf(ActionParameters.Key<Boolean>("open_hourly") to true)
    )

    if (isMinimal) {
        // Minimal: single solid-color row
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(tokens.topZoneBackground)
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
                    text = weatherEmoji(state.weatherState),
                    style = TextStyle(fontSize = 18.sp)
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = state.verdict,
                    style = TextStyle(
                        color = ColorProvider(tokens.verdictText),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2
                )
            }
        }
        return
    }

    // ── Full layout — painter's algorithm ──
    // All background zones are SIBLINGS inside the root Box (FrameLayout).
    // None are nested inside each other, which is the only reliable way to get
    // multiple background colors in Glance/RemoteViews.
    //
    // Drawing order (bottom → top):
    //   1. cardBackground   fills the entire widget
    //   2. accentColor      fills top 74dp  (72dp zone + 2dp divider)
    //   3. topZoneBackground fills top 72dp  (covers accent, leaving 2dp divider visible)
    //   4. content Column   transparent, just layout
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(clickAction)
            .semantics { contentDescription = buildContentDescription(state) }
    ) {
        // Layer 1: bottom-zone / card background (entire widget)
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(tokens.cardBackground)
        ) {}

        // Layer 2: accent divider strip — 74dp tall so 2dp peeks below top zone
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(74.dp)
                .background(tokens.accentColor)
        ) {}

        // Layer 3: top-zone background — 72dp, covers accent except the bottom 2dp divider
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(72.dp)
                .background(tokens.topZoneBackground)
        ) {}

        // Layer 4: content — no background, sits on top of the painted layers
        Column(modifier = GlanceModifier.fillMaxSize()) {

            // TOP CONTENT — 72dp, aligns with top zone background
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
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

            // 2dp spacer aligns with the accent divider painted in Layer 2
            Spacer(modifier = GlanceModifier.height(2.dp))

            // BOTTOM CONTENT — sits on cardBackground from Layer 1
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
                            fontStyle = FontStyle.Italic
                        ),
                        maxLines = 1
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
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Box(
                        modifier = GlanceModifier
                            .width(7.dp)
                            .height(7.dp)
                            .cornerRadius(4.dp)
                            .background(tokens.accentColor)
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
}

private fun weatherEmoji(state: WeatherState): String = when (state) {
    WeatherState.CLEAR    -> "☀️"
    WeatherState.OVERCAST -> "☁️"
    WeatherState.RAIN     -> "🌧"
    WeatherState.STORM    -> "⛈"
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
