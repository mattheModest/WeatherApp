package com.weatherapp.ui.widget

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.weatherapp.data.datastore.PreferenceKeys
import com.weatherapp.model.WeatherState
import com.weatherapp.model.WidgetDisplayState
import kotlinx.coroutines.flow.first

/**
 * Builds a WidgetDisplayState from an already-fetched Preferences snapshot.
 * Called both from the suspend helper below and from inside Glance's provideContent
 * (where it's driven reactively by DataStore.data.collect).
 */
fun buildWidgetDisplayState(prefs: Preferences): WidgetDisplayState {
    val verdictCandidates = prefs[PreferenceKeys.KEY_VERDICT_CANDIDATES]
        ?.split("|")?.filter { it.isNotEmpty() } ?: emptyList()
    val verdictText = prefs[PreferenceKeys.KEY_WIDGET_SELECTED_VERDICT]?.takeIf { it.isNotEmpty() }
        ?: verdictCandidates.randomOrNull()
        ?: prefs[PreferenceKeys.KEY_WIDGET_VERDICT] ?: ""

    val moodCandidates = prefs[PreferenceKeys.KEY_MOOD_CANDIDATES]
        ?.split("|")?.filter { it.isNotEmpty() } ?: emptyList()
    val moodLine = prefs[PreferenceKeys.KEY_WIDGET_SELECTED_MOOD]?.takeIf { it.isNotEmpty() }
        ?: moodCandidates.randomOrNull()
        ?: prefs[PreferenceKeys.KEY_MOOD_LINE] ?: ""

    val bringListStr = prefs[PreferenceKeys.KEY_BRING_LIST] ?: ""
    val stalenessFlag = prefs[PreferenceKeys.KEY_STALENESS_FLAG] ?: false
    val lastUpdateEpoch = prefs[PreferenceKeys.KEY_LAST_UPDATE_EPOCH] ?: 0L
    val isAllClear = prefs[PreferenceKeys.KEY_ALL_CLEAR] ?: false

    val isStale = stalenessFlag || (lastUpdateEpoch > 0 &&
        System.currentTimeMillis() / 1000L - lastUpdateEpoch > 1800)

    val weatherState = inferWeatherStateFromVerdict(verdictText, isAllClear)

    return WidgetDisplayState(
        verdict         = verdictText,
        bringItems      = bringListStr.split("|").filter { it.isNotEmpty() },
        bestWindow      = prefs[PreferenceKeys.KEY_BEST_WINDOW]?.takeIf { it.isNotEmpty() },
        isAllClear      = isAllClear,
        moodLine        = moodLine,
        lastUpdateEpoch = lastUpdateEpoch,
        isStale         = isStale,
        weatherState    = weatherState,
        currentTempC    = prefs[PreferenceKeys.KEY_CURRENT_TEMP_C]
    )
}

suspend fun DataStore<Preferences>.readWidgetDisplayState(): WidgetDisplayState =
    buildWidgetDisplayState(data.first())

fun inferWeatherStateFromVerdictPublic(verdictText: String, isAllClear: Boolean): WeatherState =
    inferWeatherStateFromVerdict(verdictText, isAllClear)

private fun inferWeatherStateFromVerdict(verdictText: String, isAllClear: Boolean): WeatherState {
    val lower = verdictText.lowercase()
    return when {
        "storm" in lower || "thunder" in lower -> WeatherState.STORM
        "rain" in lower || "umbrella" in lower -> WeatherState.RAIN
        isAllClear                              -> WeatherState.CLEAR
        "bundle" in lower || "jacket" in lower -> WeatherState.OVERCAST
        else                                   -> WeatherState.CLEAR
    }
}
