package com.weatherapp

import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherapp.data.datastore.PreferenceKeys
import com.weatherapp.data.update.UpdateChecker
import com.weatherapp.data.update.UpdateInfo
import com.weatherapp.model.WidgetDisplayState
import com.weatherapp.ui.widget.inferWeatherStateFromVerdictPublic
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
    private val updateChecker: UpdateChecker
) : ViewModel() {

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    private val _showHourlySheet = MutableStateFlow(false)
    val showHourlySheet: StateFlow<Boolean> = _showHourlySheet.asStateFlow()

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
        val weatherState = inferWeatherStateFromVerdictPublic(verdictText, isAllClear)
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

    fun openHourlyDetail() {
        _showHourlySheet.value = true
        Timber.d("MainViewModel: hourly detail opened")
    }

    fun closeHourlyDetail() {
        _showHourlySheet.value = false
        Timber.d("MainViewModel: hourly detail closed")
    }

    fun onNewIntent(intent: Intent) {
        if (intent.getBooleanExtra(MainActivity.EXTRA_OPEN_HOURLY, false)) {
            openHourlyDetail()
        }
    }

    fun onResume() {
        viewModelScope.launch {
            val prefs = dataStore.data.first()

            val verdicts = prefs[PreferenceKeys.KEY_VERDICT_CANDIDATES]
                ?.split("|")?.filter { it.isNotEmpty() } ?: emptyList()
            if (verdicts.isNotEmpty()) _pickedVerdict.value = verdicts.random()

            val moods = prefs[PreferenceKeys.KEY_MOOD_CANDIDATES]
                ?.split("|")?.filter { it.isNotEmpty() } ?: emptyList()
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
