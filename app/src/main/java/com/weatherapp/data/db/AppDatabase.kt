package com.weatherapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.weatherapp.data.db.dao.AlertStateDao
import com.weatherapp.data.db.dao.CalendarEventForecastDao
import com.weatherapp.data.db.dao.ForecastDao
import com.weatherapp.data.db.entity.AlertStateRecord
import com.weatherapp.data.db.entity.CalendarEventForecast
import com.weatherapp.data.db.entity.ForecastHour

@Database(
    entities = [ForecastHour::class, AlertStateRecord::class, CalendarEventForecast::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(AlertStateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun forecastDao(): ForecastDao
    abstract fun alertStateDao(): AlertStateDao
    abstract fun calendarEventForecastDao(): CalendarEventForecastDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `alert_state_record` (
                        `event_id` TEXT NOT NULL,
                        `state` TEXT NOT NULL,
                        `confirmed_forecast_snapshot` TEXT NOT NULL,
                        `last_transition_at` INTEGER NOT NULL,
                        PRIMARY KEY(`event_id`)
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `calendar_event_forecast` (
                        `event_id` TEXT NOT NULL,
                        `last_weather_snapshot` TEXT NOT NULL,
                        `widget_display_string` TEXT NOT NULL,
                        `last_updated_epoch` INTEGER NOT NULL,
                        `event_start_epoch` INTEGER NOT NULL,
                        PRIMARY KEY(`event_id`)
                    )
                """.trimIndent())
            }
        }
    }
}
