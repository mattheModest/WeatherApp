package com.weatherapp.model

data class WidgetDisplayState(
    val verdict: String,
    val bringItems: List<String>,
    val bestWindow: String?,
    val isAllClear: Boolean,
    val moodLine: String,
    val lastUpdateEpoch: Long,
    val isStale: Boolean,
    val weatherState: WeatherState,
    val currentTempC: Float? = null
) {
    companion object {
        val EMPTY = WidgetDisplayState(
            verdict = "Loading...",
            bringItems = emptyList(),
            bestWindow = null,
            isAllClear = false,
            moodLine = "",
            lastUpdateEpoch = 0L,
            isStale = false,
            weatherState = WeatherState.CLEAR
        )
    }
}
