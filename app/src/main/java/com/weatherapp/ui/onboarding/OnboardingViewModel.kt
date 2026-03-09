package com.weatherapp.ui.onboarding

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.weatherapp.data.datastore.PreferenceKeys
import com.weatherapp.ui.widget.WeatherWidget
import com.weatherapp.util.UiState
import com.weatherapp.worker.ForecastRefreshWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class OnboardingStep {
    LOCATION_PERMISSION,
    CALENDAR_RATIONALE,
    CALENDAR_PERMISSION,
    MANUAL_LOCATION,
    COMPLETE
}

data class OnboardingState(
    val step: OnboardingStep,
    val locationDenied: Boolean = false,
    val calendarDenied: Boolean = false,
    val manualLocation: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val workManager: WorkManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<OnboardingState>>(
        UiState.Success(OnboardingState(OnboardingStep.LOCATION_PERMISSION))
    )
    val uiState: StateFlow<UiState<OnboardingState>> = _uiState.asStateFlow()

    private fun updateState(transform: (OnboardingState) -> OnboardingState) {
        val current = _uiState.value
        if (current is UiState.Success) {
            _uiState.value = UiState.Success(transform(current.data))
        }
    }

    fun onLocationGranted() {
        Timber.d("OnboardingViewModel: location granted")
        updateState { it.copy(step = OnboardingStep.CALENDAR_RATIONALE) }
    }

    fun onLocationDenied() {
        Timber.d("OnboardingViewModel: location denied — moving to manual location entry")
        updateState { it.copy(step = OnboardingStep.MANUAL_LOCATION, locationDenied = true) }
    }

    fun onManualLocationEntered(location: String) {
        Timber.d("OnboardingViewModel: manual location entered: $location")
        updateState { it.copy(manualLocation = location, step = OnboardingStep.CALENDAR_RATIONALE) }
    }

    fun onCalendarRationaleAcknowledged() {
        Timber.d("OnboardingViewModel: calendar rationale acknowledged")
        updateState { it.copy(step = OnboardingStep.CALENDAR_PERMISSION) }
    }

    fun onCalendarGranted() {
        Timber.d("OnboardingViewModel: calendar granted")
        viewModelScope.launch {
            completeOnboarding(calendarGranted = true)
        }
    }

    fun onCalendarDenied() {
        Timber.d("OnboardingViewModel: calendar denied")
        viewModelScope.launch {
            completeOnboarding(calendarGranted = false)
        }
    }

    private suspend fun completeOnboarding(calendarGranted: Boolean) {
        val currentState = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Loading

        try {
            dataStore.edit { prefs ->
                prefs[PreferenceKeys.KEY_HAS_COMPLETED_ONBOARDING] = true
                currentState.manualLocation?.let { location ->
                    prefs[PreferenceKeys.KEY_MANUAL_LOCATION] = location
                }
            }

            val immediateRequest = OneTimeWorkRequestBuilder<ForecastRefreshWorker>().build()
            workManager.enqueue(immediateRequest)

            val periodicRequest = PeriodicWorkRequestBuilder<ForecastRefreshWorker>(
                30, TimeUnit.MINUTES
            ).setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            ).build()

            workManager.enqueueUniquePeriodicWork(
                "forecast_refresh",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )

            WeatherWidget.update(context)

            _uiState.value = UiState.Success(
                OnboardingState(
                    step = OnboardingStep.COMPLETE,
                    calendarDenied = !calendarGranted,
                    locationDenied = currentState.locationDenied,
                    manualLocation = currentState.manualLocation
                )
            )
            Timber.i("OnboardingViewModel: onboarding complete — calendarGranted=$calendarGranted")
        } catch (e: Exception) {
            Timber.e(e, "OnboardingViewModel: setup failed during completeOnboarding")
            _uiState.value = UiState.Error("Setup failed. Please restart the app.")
        }
    }
}
