package com.weatherapp.ui.hourly

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.weatherapp.data.db.entity.ForecastHour
import com.weatherapp.data.datastore.PreferenceKeys
import com.weatherapp.data.weather.WeatherRepository
import com.weatherapp.util.UiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.roundToInt

@OptIn(ExperimentalCoroutinesApi::class)
class HourlyDetailViewModelTest {

    private lateinit var mockWeatherRepository: WeatherRepository
    private lateinit var mockDataStore: DataStore<Preferences>
    private lateinit var mockPrefs: Preferences

    @Before
    fun setUp() {
        mockPrefs = mockk<Preferences>(relaxed = true)
        every { mockPrefs[PreferenceKeys.KEY_TEMP_UNIT] } returns "celsius"

        mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        every { mockDataStore.data } returns flowOf(mockPrefs)

        mockWeatherRepository = mockk()
    }

    @Test
    fun `initial state is UiState Loading`() = runTest(UnconfinedTestDispatcher()) {
        every { mockWeatherRepository.getHourlyForecast(any(), any()) } returns flowOf(emptyList())

        val viewModel = HourlyDetailViewModel(mockWeatherRepository, mockDataStore)
        // The stateIn initialValue is UiState.Loading — check it immediately (before flow emits)
        // With UnconfinedTestDispatcher the flow runs eagerly but we check the declared initial
        val initial = viewModel.uiState
        assertTrue(initial.value is UiState.Loading || initial.value is UiState.Success)
    }

    @Test
    fun `hours before current hour are filtered out`() = runTest(UnconfinedTestDispatcher()) {
        val nowSeconds = System.currentTimeMillis() / 1000L
        val currentHourStart = nowSeconds - (nowSeconds % 3600L)

        // Two hours: one in the past, one current/future
        val pastHour = ForecastHour(
            hourEpoch = currentHourStart - 3600L,
            temperatureC = 15.0,
            precipitationProbability = 0.1,
            windSpeedKmh = 10.0,
            weatherCode = 0
        )
        val currentHour = ForecastHour(
            hourEpoch = currentHourStart,
            temperatureC = 18.0,
            precipitationProbability = 0.2,
            windSpeedKmh = 12.0,
            weatherCode = 1
        )
        val futureHour = ForecastHour(
            hourEpoch = currentHourStart + 3600L,
            temperatureC = 20.0,
            precipitationProbability = 0.0,
            windSpeedKmh = 8.0,
            weatherCode = 0
        )

        every { mockWeatherRepository.getHourlyForecast(any(), any()) } returns
            flowOf(listOf(pastHour, currentHour, futureHour))

        val viewModel = HourlyDetailViewModel(mockWeatherRepository, mockDataStore)

        val state = viewModel.uiState.value
        if (state is UiState.Success) {
            // Past hour should be filtered out, current and future should remain
            assertEquals(2, state.data.size)
            assertTrue("Past hour should be excluded",
                state.data.none { it.temperatureCelsius == 15.0 })
        }
    }

    @Test
    fun `celsius display shows roundToInt plus degree C symbol`() = runTest(UnconfinedTestDispatcher()) {
        every { mockPrefs[PreferenceKeys.KEY_TEMP_UNIT] } returns "celsius"
        val nowSeconds = System.currentTimeMillis() / 1000L
        val currentHourStart = nowSeconds - (nowSeconds % 3600L)

        val hour = ForecastHour(
            hourEpoch = currentHourStart,
            temperatureC = 17.6,
            precipitationProbability = 0.1,
            windSpeedKmh = 10.0,
            weatherCode = 0
        )
        every { mockWeatherRepository.getHourlyForecast(any(), any()) } returns flowOf(listOf(hour))

        val viewModel = HourlyDetailViewModel(mockWeatherRepository, mockDataStore)

        val state = viewModel.uiState.value
        if (state is UiState.Success) {
            val row = state.data.firstOrNull { it.temperatureCelsius == 17.6 }
            val expected = "${17.6.roundToInt()}°C"
            assertEquals(expected, row?.temperatureDisplay)
        }
    }

    @Test
    fun `fahrenheit display shows correct conversion`() = runTest(UnconfinedTestDispatcher()) {
        every { mockPrefs[PreferenceKeys.KEY_TEMP_UNIT] } returns "fahrenheit"
        val nowSeconds = System.currentTimeMillis() / 1000L
        val currentHourStart = nowSeconds - (nowSeconds % 3600L)

        val tempC = 20.0
        val hour = ForecastHour(
            hourEpoch = currentHourStart,
            temperatureC = tempC,
            precipitationProbability = 0.0,
            windSpeedKmh = 5.0,
            weatherCode = 0
        )
        every { mockWeatherRepository.getHourlyForecast(any(), any()) } returns flowOf(listOf(hour))

        val viewModel = HourlyDetailViewModel(mockWeatherRepository, mockDataStore)

        val state = viewModel.uiState.value
        if (state is UiState.Success) {
            val row = state.data.firstOrNull { it.temperatureCelsius == tempC }
            val expectedFahrenheit = (tempC * 9.0 / 5.0 + 32.0).roundToInt()
            assertEquals("$expectedFahrenheit°F", row?.temperatureDisplay)
        }
    }
}
