package com.weatherapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.weatherapp.data.db.dao.AlertStateDao
import com.weatherapp.data.db.dao.ForecastDao
import com.weatherapp.data.db.entity.AlertStateRecord
import com.weatherapp.data.db.entity.ForecastHour

@Database(
    entities = [ForecastHour::class, AlertStateRecord::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(AlertStateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun forecastDao(): ForecastDao
    abstract fun alertStateDao(): AlertStateDao

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
    }
}
