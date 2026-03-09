package com.weatherapp.model

data class ForecastSnapshot(
    val precipProb: Double,
    val windKmh: Double,
    val windowStart: Long,
    val windowEnd: Long
)
