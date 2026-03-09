package com.weatherapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.weatherapp.data.db.entity.CalendarEventForecast

@Dao
interface CalendarEventForecastDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: CalendarEventForecast)

    @Query("SELECT * FROM calendar_event_forecast WHERE event_id = :eventId")
    suspend fun getByEventId(eventId: String): CalendarEventForecast?

    @Query("DELETE FROM calendar_event_forecast WHERE event_start_epoch < :beforeEpoch")
    suspend fun deleteExpired(beforeEpoch: Long)
}
