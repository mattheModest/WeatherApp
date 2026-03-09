package com.weatherapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannels {
    const val CHANNEL_ID_WEATHER_ALERTS = "weather_alerts"

    fun ensureWeatherAlertsChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID_WEATHER_ALERTS,
            "Weather Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily weather alerts and condition changes"
            setShowBadge(false)
        }
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }
}
