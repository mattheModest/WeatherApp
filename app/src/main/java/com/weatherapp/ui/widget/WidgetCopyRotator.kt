package com.weatherapp.ui.widget

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.weatherapp.data.datastore.PreferenceKeys
import com.weatherapp.data.datastore.weatherDataStore
import com.weatherapp.model.PersonalityCore
import com.weatherapp.model.personalityCoreFromString
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Picks a fresh random verdict + mood from the candidates pools, writes the
 * selections to DataStore, then triggers a Glance widget update.
 *
 * Writing to DataStore is the critical step — Glance observes DataStore state,
 * so a write guarantees re-composition (unlike calling update() with unchanged state).
 */
suspend fun rotateWidgetCopy(context: Context) {
    val appContext = context.applicationContext
    val prefs = appContext.weatherDataStore.data.first()
    val personality = personalityCoreFromString(prefs[PreferenceKeys.KEY_PERSONALITY_CORE])

    val verdictKey = when (personality) {
        PersonalityCore.FRANK  -> PreferenceKeys.KEY_VERDICT_CANDIDATES
        PersonalityCore.KELVIN -> PreferenceKeys.KEY_VERDICT_CANDIDATES_KELVIN
        PersonalityCore.GRAVES -> PreferenceKeys.KEY_VERDICT_CANDIDATES_GRAVES
    }
    val moodKey = when (personality) {
        PersonalityCore.FRANK  -> PreferenceKeys.KEY_MOOD_CANDIDATES
        PersonalityCore.KELVIN -> PreferenceKeys.KEY_MOOD_CANDIDATES_KELVIN
        PersonalityCore.GRAVES -> PreferenceKeys.KEY_MOOD_CANDIDATES_GRAVES
    }

    val verdictCandidates = prefs[verdictKey]?.split("|")?.filter { it.isNotEmpty() } ?: emptyList()
    val moodCandidates = prefs[moodKey]?.split("|")?.filter { it.isNotEmpty() } ?: emptyList()

    if (verdictCandidates.isEmpty() && moodCandidates.isEmpty()) {
        Timber.d("rotateWidgetCopy: no candidates in DataStore, skipping rotation")
        return
    }

    val selectedVerdict = verdictCandidates.randomOrNull()
    val selectedMood = moodCandidates.randomOrNull()

    appContext.weatherDataStore.edit { store ->
        if (selectedVerdict != null) store[PreferenceKeys.KEY_WIDGET_SELECTED_VERDICT] = selectedVerdict
        if (selectedMood != null) store[PreferenceKeys.KEY_WIDGET_SELECTED_MOOD] = selectedMood
    }

    Timber.d("rotateWidgetCopy: verdict='$selectedVerdict' mood='$selectedMood'")
    WeatherWidget.update(appContext)
}
