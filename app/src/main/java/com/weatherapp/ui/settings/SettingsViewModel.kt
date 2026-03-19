package com.weatherapp.ui.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.weatherapp.data.datastore.PreferenceKeys
import com.weatherapp.model.PersonalityCore
import com.weatherapp.model.VisualTheme
import com.weatherapp.model.personalityCoreFromString
import com.weatherapp.model.visualThemeFromString
import com.weatherapp.ui.widget.WeatherWidget
import com.weatherapp.util.UiState
import com.weatherapp.worker.ForecastRefreshWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val dataStore: DataStore<Preferences>,
    private val workManager: WorkManager,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    val uiState = dataStore.data
        .map { prefs ->
            val tempUnitStr = prefs[PreferenceKeys.KEY_TEMP_UNIT] ?: "celsius"
            val tempUnit = if (tempUnitStr == "fahrenheit") TempUnit.FAHRENHEIT else TempUnit.CELSIUS
            val notificationsEnabled = prefs[PreferenceKeys.KEY_NOTIFICATIONS_ENABLED] ?: true
            val moodLine = prefs[PreferenceKeys.KEY_MOOD_LINE] ?: ""
            val shareText = "\"$moodLine\"\n\nWeatherApp — daily weather in plain language"
            val personality = personalityCoreFromString(prefs[PreferenceKeys.KEY_PERSONALITY_CORE])
            val visualTheme = visualThemeFromString(prefs[PreferenceKeys.KEY_VISUAL_THEME])
            val manualLocation = prefs[PreferenceKeys.KEY_MANUAL_LOCATION] ?: ""

            UiState.Success(
                SettingsState(
                    tempUnit = tempUnit,
                    notificationsEnabled = notificationsEnabled,
                    moodLine = moodLine,
                    shareText = shareText,
                    personality = personality,
                    visualTheme = visualTheme,
                    manualLocation = manualLocation
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

    fun onPersonalitySelected(personality: PersonalityCore) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[PreferenceKeys.KEY_PERSONALITY_CORE] = personality.name
            }
            Timber.d("SettingsViewModel: personality changed to ${personality.name}")
        }
    }

    fun onThemeSelected(theme: VisualTheme) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[PreferenceKeys.KEY_VISUAL_THEME] = theme.name
            }
            WeatherWidget.update(appContext)
            Timber.d("SettingsViewModel: visual theme changed to ${theme.name}")
        }
    }

    fun onManualLocationSaved(city: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[PreferenceKeys.KEY_MANUAL_LOCATION] = city.trim()
            }
            workManager.enqueue(OneTimeWorkRequestBuilder<ForecastRefreshWorker>().build())
            Timber.d("SettingsViewModel: manual location saved: '$city' — refresh enqueued")
        }
    }

    fun onManualLocationCleared() {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[PreferenceKeys.KEY_MANUAL_LOCATION] = ""
            }
            workManager.enqueue(OneTimeWorkRequestBuilder<ForecastRefreshWorker>().build())
            Timber.d("SettingsViewModel: manual location cleared — will use GPS")
        }
    }

}
