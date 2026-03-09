package com.weatherapp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forecast_hour")
data class ForecastHour(
    @PrimaryKey
    @ColumnInfo(name = "hour_epoch")
    val hourEpoch: Long,

    @ColumnInfo(name = "temperature_c")
    val temperatureC: Double,

    @ColumnInfo(name = "precipitation_probability")
    val precipitationProbability: Double,

    @ColumnInfo(name = "wind_speed_kmh")
    val windSpeedKmh: Double,

    @ColumnInfo(name = "weather_code")
    val weatherCode: Int
)
