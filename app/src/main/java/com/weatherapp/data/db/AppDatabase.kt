package com.weatherapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.weatherapp.data.db.dao.ForecastDao
import com.weatherapp.data.db.entity.ForecastHour

// Note: AlertStateRecord (Story 2.1) and CalendarEventForecast (Story 3.2) will be added
// to this database with version migrations.
@Database(
    entities = [ForecastHour::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun forecastDao(): ForecastDao
}
