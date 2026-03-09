package com.weatherapp.model

object AlertThresholds {
    const val MATERIAL_PRECIP_CHANGE = 0.20   // 20% shift in precipitation probability
    const val WIND_SPEED_ALERT_KMH   = 40.0   // ~25 mph; Worker API returns wind_speed_kmh
    const val ALL_CLEAR_PRECIP_MAX   = 0.20   // Below 20% = potential all-clear
    const val ALL_CLEAR_WIND_MAX_KMH = 30.0   // Below 30 km/h = all-clear for wind
}
