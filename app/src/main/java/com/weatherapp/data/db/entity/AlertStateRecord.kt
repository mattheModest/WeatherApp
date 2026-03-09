package com.weatherapp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.weatherapp.model.AlertState

@Entity(tableName = "alert_state_record")
data class AlertStateRecord(
    @PrimaryKey
    @ColumnInfo(name = "event_id")
    val eventId: String,

    @ColumnInfo(name = "state")
    val state: AlertState,

    @ColumnInfo(name = "confirmed_forecast_snapshot")
    val confirmedForecastSnapshot: String,

    @ColumnInfo(name = "last_transition_at")
    val lastTransitionAt: Long
)
