package com.weatherapp.ui.hourly

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherapp.data.datastore.PreferenceKeys
import com.weatherapp.data.location.LocationRepository
import com.weatherapp.data.weather.WeatherRepository
import com.weatherapp.model.VerdictGenerator
import com.weatherapp.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class HourlyDetailViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val dataStore: DataStore<Preferences>,
    private val locationRepository: LocationRepository
) : ViewModel() {

    val uiState = flow {
        val nowSeconds = System.currentTimeMillis() / 1000L
        val startOfDay = nowSeconds - (nowSeconds % 86400L)
        val endOfDay = startOfDay + 86400L

        val currentHourStart = nowSeconds - (nowSeconds % 3600L)

        val prefs = dataStore.data.first()
        val tempUnit = prefs[PreferenceKeys.KEY_TEMP_UNIT] ?: "celsius"
        val location = locationRepository.getSnappedLocation()
        val comfortOffset = if (location != null) {
            VerdictGenerator.computeComfortOffset(location.first, LocalDate.now().monthValue)
        } else 0.0

        weatherRepository.getHourlyForecast(currentHourStart, endOfDay).collect { hours ->
            val rows = hours
                .filter { it.hourEpoch >= currentHourStart }
                .map { hour ->
                    val localHour = Instant.ofEpochSecond(hour.hourEpoch)
                        .atZone(ZoneId.systemDefault()).hour
                    val hourLabel = when {
                        localHour == 0  -> "12am"
                        localHour < 12  -> "${localHour}am"
                        localHour == 12 -> "12pm"
                        else            -> "${localHour - 12}pm"
                    }
                    val verdictText = VerdictGenerator.hourlyClothingVerdict(
                        tempC = hour.temperatureC,
                        precipProb = hour.precipitationProbability,
                        comfortOffset = comfortOffset
                    )
                    val temperatureDisplay = if (tempUnit == "fahrenheit") {
                        val fahrenheit = (hour.temperatureC * 9.0 / 5.0 + 32.0).roundToInt()
                        "$fahrenheit°F"
                    } else {
                        "${hour.temperatureC.roundToInt()}°C"
                    }
                    HourlyDetailRow(
                        hourLabel = hourLabel,
                        verdictText = verdictText,
                        temperatureCelsius = hour.temperatureC,
                        temperatureDisplay = temperatureDisplay
                    )
                }
            Timber.d("HourlyDetailViewModel: emitting ${rows.size} hour rows")
            emit(UiState.Success(rows) as UiState<List<HourlyDetailRow>>)
        }
    }.catch { e ->
        Timber.e(e, "HourlyDetailViewModel: error loading hourly forecast")
        emit(UiState.Error("Unable to load forecast. Please try again."))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState.Loading
    )
}
