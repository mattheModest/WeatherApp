package com.weatherapp.data.weather

import com.weatherapp.data.db.dao.ForecastDao
import com.weatherapp.data.db.entity.ForecastHour
import com.weatherapp.data.location.LocationRepository
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val api: WeatherApi,
    private val forecastDao: ForecastDao,
    private val locationRepository: LocationRepository
) {
    suspend fun fetchForecast(): Result<Unit> = runCatching {
        val (latGrid, lonGrid) = locationRepository.getSnappedLocation()
            ?: return Result.failure(IllegalStateException("Location unavailable"))
        val date = LocalDate.now(ZoneOffset.UTC).toString()
        val response = api.getForecast(latGrid, lonGrid, date)
        val hours = response.hourlyForecasts.map { dto ->
            ForecastHour(
                hourEpoch = dto.hourEpoch,
                temperatureC = dto.temperatureC,
                precipitationProbability = dto.precipitationProbability,
                windSpeedKmh = dto.windSpeedKmh,
                weatherCode = dto.weatherCode
            )
        }
        forecastDao.insert(hours)
        val cutoff = System.currentTimeMillis() / 1000L - 48 * 3600L
        forecastDao.deleteExpired(cutoff)
        Timber.d("fetchForecast success: ${hours.size} hours stored")
    }

    fun getHourlyForecast(startEpoch: Long, endEpoch: Long): Flow<List<ForecastHour>> =
        forecastDao.queryByTimeWindow(startEpoch, endEpoch)
}
