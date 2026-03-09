package com.weatherapp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_event_forecast")
data class CalendarEventForecast(
    @PrimaryKey @ColumnInfo(name = "event_id") val eventId: String,
    @ColumnInfo(name = "last_weather_snapshot") val lastWeatherSnapshot: String,
    @ColumnInfo(name = "widget_display_string") val widgetDisplayString: String,
    @ColumnInfo(name = "last_updated_epoch") val lastUpdatedEpoch: Long,
    @ColumnInfo(name = "event_start_epoch") val eventStartEpoch: Long
)
