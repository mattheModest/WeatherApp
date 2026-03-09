package com.weatherapp

import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherapp.data.datastore.PreferenceKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    private val _showHourlySheet = MutableStateFlow(false)
    val showHourlySheet: StateFlow<Boolean> = _showHourlySheet.asStateFlow()

    private val _requestNotificationPermission = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val requestNotificationPermission: SharedFlow<Unit> = _requestNotificationPermission.asSharedFlow()

    init {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            val hasCompletedOnboarding = prefs[PreferenceKeys.KEY_HAS_COMPLETED_ONBOARDING] ?: false
            _startDestination.value = if (hasCompletedOnboarding) "main" else "onboarding"
            Timber.d("MainViewModel: startDestination=${_startDestination.value}")
        }
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
            val shouldRequest = prefs[PreferenceKeys.KEY_SHOULD_REQUEST_NOTIFICATIONS]
            if (shouldRequest == true) {
                dataStore.edit { it[PreferenceKeys.KEY_SHOULD_REQUEST_NOTIFICATIONS] = false }
                _requestNotificationPermission.tryEmit(Unit)
                Timber.d("MainViewModel: notification permission request emitted")
            }
        }
    }
}
