package com.weatherapp.data.weather.dto

import com.google.gson.annotations.SerializedName

data class HourlyForecastDto(
    @SerializedName("hour_epoch") val hourEpoch: Long,
    @SerializedName("temperature_c") val temperatureC: Double,
    @SerializedName("precipitation_probability") val precipitationProbability: Double,
    @SerializedName("wind_speed_kmh") val windSpeedKmh: Double,
    @SerializedName("weather_code") val weatherCode: Int
)
