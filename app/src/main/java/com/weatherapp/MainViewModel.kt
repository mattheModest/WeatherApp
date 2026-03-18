package com.weatherapp

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.weatherapp.data.datastore.PreferenceKeys
import com.weatherapp.model.VisualTheme
import com.weatherapp.model.personalityCoreFromString
import com.weatherapp.model.visualThemeFromString
import com.weatherapp.data.update.UpdateChecker
import com.weatherapp.data.update.UpdateInfo
import com.weatherapp.model.WeatherState
import com.weatherapp.model.WidgetDisplayState
import com.weatherapp.ui.widget.inferWeatherStateFromVerdictPublic
import com.weatherapp.worker.ForecastRefreshWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val updateChecker: UpdateChecker,
    private val workManager: WorkManager
) : ViewModel() {

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    private val _requestNotificationPermission = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val requestNotificationPermission: SharedFlow<Unit> = _requestNotificationPermission.asSharedFlow()

    val weatherDisplayState: StateFlow<WidgetDisplayState> = dataStore.data.map { prefs ->
        val verdictText = prefs[PreferenceKeys.KEY_WIDGET_VERDICT] ?: ""
        val bringListStr = prefs[PreferenceKeys.KEY_BRING_LIST] ?: ""
        val stalenessFlag = prefs[PreferenceKeys.KEY_STALENESS_FLAG] ?: false
        val lastUpdateEpoch = prefs[PreferenceKeys.KEY_LAST_UPDATE_EPOCH] ?: 0L
        val isAllClear = prefs[PreferenceKeys.KEY_ALL_CLEAR] ?: false
        val isStale = stalenessFlag || (lastUpdateEpoch > 0 &&
            System.currentTimeMillis() / 1000L - lastUpdateEpoch > 1800)
        val weatherStateStr = prefs[PreferenceKeys.KEY_WEATHER_STATE]
        val weatherState = if (weatherStateStr != null)
            runCatching { WeatherState.valueOf(weatherStateStr) }.getOrElse { inferWeatherStateFromVerdictPublic(verdictText, isAllClear) }
        else inferWeatherStateFromVerdictPublic(verdictText, isAllClear)
        WidgetDisplayState(
            verdict         = verdictText,
            bringItems      = bringListStr.split("|").filter { it.isNotEmpty() },
            bestWindow      = prefs[PreferenceKeys.KEY_BEST_WINDOW]?.takeIf { it.isNotEmpty() },
            isAllClear      = isAllClear,
            moodLine        = prefs[PreferenceKeys.KEY_MOOD_LINE] ?: "",
            lastUpdateEpoch = lastUpdateEpoch,
            isStale         = isStale,
            weatherState    = weatherState,
            currentTempC    = prefs[PreferenceKeys.KEY_CURRENT_TEMP_C]
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WidgetDisplayState.EMPTY
    )

    private val _pickedVerdict = MutableStateFlow("")
    private val _pickedMood = MutableStateFlow("")

    /** Use this in the UI — verdict and mood line are re-picked randomly on every onResume(). */
    val displayState: StateFlow<WidgetDisplayState> = combine(
        weatherDisplayState, _pickedVerdict, _pickedMood
    ) { state, verdict, mood ->
        if (verdict.isNotEmpty()) state.copy(verdict = verdict, moodLine = mood) else state
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WidgetDisplayState.EMPTY
    )

    val tempUnit: StateFlow<String> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.KEY_TEMP_UNIT] ?: "celsius"
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = "celsius"
    )

    val visualTheme: StateFlow<VisualTheme> = dataStore.data.map { prefs ->
        visualThemeFromString(prefs[PreferenceKeys.KEY_VISUAL_THEME])
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VisualTheme.DEFAULT
    )

    init {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            val hasCompletedOnboarding = prefs[PreferenceKeys.KEY_HAS_COMPLETED_ONBOARDING] ?: false
            _startDestination.value = if (hasCompletedOnboarding) "main" else "onboarding"
            Timber.d("MainViewModel: startDestination=${_startDestination.value}")
        }
        viewModelScope.launch {
            _updateInfo.value = updateChecker.checkForUpdate()
            Timber.d("MainViewModel: updateInfo=${_updateInfo.value}")
        }
    }

    fun dismissUpdate() {
        _updateInfo.value = null
    }

    fun onResume() {
        viewModelScope.launch {
            val prefs = dataStore.data.first()

            // Don't trigger a refresh until onboarding is done — worker would fail (no location yet).
            val hasCompletedOnboarding = prefs[PreferenceKeys.KEY_HAS_COMPLETED_ONBOARDING] ?: false
            if (!hasCompletedOnboarding) {
                Timber.d("MainViewModel.onResume: onboarding not complete — skipping refresh")
                return@launch
            }

            // Trigger an immediate refresh if data is stale (>30 min) or never fetched.
            // Ensures forecast rows populate even if WorkManager's initial job was delayed.
            val lastUpdate = prefs[PreferenceKeys.KEY_LAST_UPDATE_EPOCH] ?: 0L
            val ageSeconds = System.currentTimeMillis() / 1000L - lastUpdate
            if (ageSeconds > 1800L) {
                Timber.d("MainViewModel.onResume: data age=${ageSeconds}s — triggering refresh")
                workManager.enqueue(OneTimeWorkRequestBuilder<ForecastRefreshWorker>().build())
            }

            val personality = personalityCoreFromString(prefs[PreferenceKeys.KEY_PERSONALITY_CORE])

            val verdictKey = when (personality) {
                com.weatherapp.model.PersonalityCore.FRANK  -> PreferenceKeys.KEY_VERDICT_CANDIDATES
                com.weatherapp.model.PersonalityCore.KELVIN -> PreferenceKeys.KEY_VERDICT_CANDIDATES_KELVIN
                com.weatherapp.model.PersonalityCore.GRAVES -> PreferenceKeys.KEY_VERDICT_CANDIDATES_GRAVES
            }
            val moodKey = when (personality) {
                com.weatherapp.model.PersonalityCore.FRANK  -> PreferenceKeys.KEY_MOOD_CANDIDATES
                com.weatherapp.model.PersonalityCore.KELVIN -> PreferenceKeys.KEY_MOOD_CANDIDATES_KELVIN
                com.weatherapp.model.PersonalityCore.GRAVES -> PreferenceKeys.KEY_MOOD_CANDIDATES_GRAVES
            }

            val verdicts = prefs[verdictKey]?.split("|")?.filter { it.isNotEmpty() } ?: emptyList()
            if (verdicts.isNotEmpty()) _pickedVerdict.value = verdicts.random()

            val moods = prefs[moodKey]?.split("|")?.filter { it.isNotEmpty() } ?: emptyList()
            if (moods.isNotEmpty()) _pickedMood.value = moods.random()

            val shouldRequest = prefs[PreferenceKeys.KEY_SHOULD_REQUEST_NOTIFICATIONS]
            if (shouldRequest == true) {
                dataStore.edit { it[PreferenceKeys.KEY_SHOULD_REQUEST_NOTIFICATIONS] = false }
                _requestNotificationPermission.tryEmit(Unit)
                Timber.d("MainViewModel: notification permission request emitted")
            }
        }
    }
}
