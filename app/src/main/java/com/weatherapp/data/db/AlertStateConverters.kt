package com.weatherapp.data.db

import androidx.room.TypeConverter
import com.weatherapp.model.AlertState

class AlertStateConverters {
    @TypeConverter
    fun fromAlertState(state: AlertState): String = state.name

    @TypeConverter
    fun toAlertState(value: String): AlertState = AlertState.valueOf(value)
}
