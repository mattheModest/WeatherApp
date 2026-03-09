package com.weatherapp.data.weather

import com.weatherapp.data.db.dao.ForecastDao
import com.weatherapp.data.db.entity.ForecastHour
import com.weatherapp.data.location.LocationRepository
import com.weatherapp.data.weather.dto.ForecastResponse
import com.weatherapp.data.weather.dto.HourlyForecastDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class WeatherRepositoryTest {

    private lateinit var weatherApi: WeatherApi
    private lateinit var forecastDao: ForecastDao
    private lateinit var locationRepository: LocationRepository
    private lateinit var repository: WeatherRepository

    @Before
    fun setup() {
        weatherApi = mockk()
        forecastDao = mockk(relaxed = true)
        locationRepository = mockk()
        repository = WeatherRepository(weatherApi, forecastDao, locationRepository)
    }

    @Test
    fun `fetchForecast success calls forecastDao insert`() = runTest {
        val snappedLocation = Pair(37.8, -122.4)
        val fakeResponse = ForecastResponse(
            latGrid = 37.8,
            lonGrid = -122.4,
            fetchedAt = "2026-03-09T00:00:00Z",
            hourlyForecasts = listOf(
                HourlyForecastDto(
                    hourEpoch = 1000L,
                    temperatureC = 15.0,
                    precipitationProbability = 0.1,
                    windSpeedKmh = 12.0,
                    weatherCode = 1
                )
            )
        )

        coEvery { locationRepository.getSnappedLocation() } returns snappedLocation
        coEvery { weatherApi.getForecast(any(), any(), any()) } returns fakeResponse

        val result = repository.fetchForecast()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { forecastDao.insert(any<List<ForecastHour>>()) }
    }

    @Test
    fun `fetchForecast returns Result failure on IOException without throwing`() = runTest {
        val snappedLocation = Pair(37.8, -122.4)

        coEvery { locationRepository.getSnappedLocation() } returns snappedLocation
        coEvery { weatherApi.getForecast(any(), any(), any()) } throws IOException("Network error")

        val result = repository.fetchForecast()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `fetchForecast returns failure when location is null`() = runTest {
        coEvery { locationRepository.getSnappedLocation() } returns null

        val result = repository.fetchForecast()

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { weatherApi.getForecast(any(), any(), any()) }
    }
}
