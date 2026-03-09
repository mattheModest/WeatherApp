package com.weatherapp.data.calendar

data class CalendarEvent(
    val eventId: String,
    val title: String,
    val startEpoch: Long,
    val endEpoch: Long,
    val location: String?
)
