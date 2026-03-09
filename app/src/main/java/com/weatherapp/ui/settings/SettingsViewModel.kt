package com.weatherapp.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherapp.data.datastore.PreferenceKeys
import com.weatherapp.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    val uiState = dataStore.data
        .map { prefs ->
            val tempUnitStr = prefs[PreferenceKeys.KEY_TEMP_UNIT] ?: "celsius"
            val tempUnit = if (tempUnitStr == "fahrenheit") TempUnit.FAHRENHEIT else TempUnit.CELSIUS
            val notificationsEnabled = prefs[PreferenceKeys.KEY_NOTIFICATIONS_ENABLED] ?: true
            val isPremium = prefs[PreferenceKeys.KEY_IS_PREMIUM] ?: false
            val moodLine = prefs[PreferenceKeys.KEY_MOOD_LINE] ?: ""
            val shareText = "\"$moodLine\"\n\nWeatherApp — daily weather in plain language"

            UiState.Success(
                SettingsState(
                    tempUnit = tempUnit,
                    notificationsEnabled = notificationsEnabled,
                    isPremium = isPremium,
                    moodLine = moodLine,
                    shareText = shareText
                )
            ) as UiState<SettingsState>
        }
        .catch { e ->
            Timber.e(e, "SettingsViewModel: error loading settings")
            emit(UiState.Error("Unable to load settings."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    fun onTempUnitToggled() {
        viewModelScope.launch {
            val currentPrefs = dataStore.data.first()
            val currentUnit = currentPrefs[PreferenceKeys.KEY_TEMP_UNIT] ?: "celsius"
            val newUnit = if (currentUnit == "celsius") "fahrenheit" else "celsius"
            dataStore.edit { prefs ->
                prefs[PreferenceKeys.KEY_TEMP_UNIT] = newUnit
            }
            Timber.d("SettingsViewModel: temp unit toggled to $newUnit")
        }
    }

    fun onNotificationsToggled() {
        viewModelScope.launch {
            val currentPrefs = dataStore.data.first()
            val current = currentPrefs[PreferenceKeys.KEY_NOTIFICATIONS_ENABLED] ?: true
            dataStore.edit { prefs ->
                prefs[PreferenceKeys.KEY_NOTIFICATIONS_ENABLED] = !current
            }
            Timber.d("SettingsViewModel: notifications toggled to ${!current}")
        }
    }
}
