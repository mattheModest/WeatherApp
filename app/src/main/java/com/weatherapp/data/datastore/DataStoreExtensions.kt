package com.weatherapp.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.weatherapp.model.WidgetDisplayState

suspend fun DataStore<Preferences>.writeWidgetState(state: WidgetDisplayState) {
    edit { prefs ->
        prefs[PreferenceKeys.KEY_WIDGET_VERDICT] = state.verdict
        prefs[PreferenceKeys.KEY_BRING_LIST] = state.bringItems.joinToString("|")
        prefs[PreferenceKeys.KEY_BEST_WINDOW] = state.bestWindow ?: ""
        prefs[PreferenceKeys.KEY_ALL_CLEAR] = state.isAllClear
        prefs[PreferenceKeys.KEY_MOOD_LINE] = state.moodLine
        prefs[PreferenceKeys.KEY_LAST_UPDATE_EPOCH] = state.lastUpdateEpoch
        prefs[PreferenceKeys.KEY_STALENESS_FLAG] = state.isStale
    }
}
