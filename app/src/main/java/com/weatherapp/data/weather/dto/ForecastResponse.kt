package com.weatherapp.data.weather.dto

import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    @SerializedName("lat_grid") val latGrid: Double,
    @SerializedName("lon_grid") val lonGrid: Double,
    @SerializedName("fetched_at") val fetchedAt: String,
    @SerializedName("hourly_forecasts") val hourlyForecasts: List<HourlyForecastDto>
)
