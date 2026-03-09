package com.weatherapp.data.weather

import com.weatherapp.data.weather.dto.ForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("forecast")
    suspend fun getForecast(
        @Query("lat_grid") latGrid: Double,
        @Query("lon_grid") lonGrid: Double,
        @Query("date") date: String
    ): ForecastResponse
}
