package com.weatherapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.weatherapp.data.db.entity.ForecastHour
import kotlinx.coroutines.flow.Flow

@Dao
interface ForecastDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(hours: List<ForecastHour>)

    @Query("SELECT * FROM forecast_hour WHERE hour_epoch BETWEEN :startEpoch AND :endEpoch ORDER BY hour_epoch ASC")
    fun queryByTimeWindow(startEpoch: Long, endEpoch: Long): Flow<List<ForecastHour>>

    @Query("DELETE FROM forecast_hour WHERE hour_epoch < :beforeEpoch")
    suspend fun deleteExpired(beforeEpoch: Long)
}
