package com.weatherapp.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.weatherapp.data.db.AppDatabase
import com.weatherapp.data.db.entity.CalendarEventForecast
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarEventForecastDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsert_newRecord_canBeRetrievedByEventId() = runTest {
        val record = CalendarEventForecast(
            eventId             = "event_001",
            lastWeatherSnapshot = """{"precipProb":0.1,"windKmh":5.0}""",
            widgetDisplayString = "Your BBQ (2pm) is clear.",
            lastUpdatedEpoch    = 1_700_000_000L,
            eventStartEpoch     = 1_700_010_000L
        )

        db.calendarEventForecastDao().upsert(record)

        val retrieved = db.calendarEventForecastDao().getByEventId("event_001")
        assertNotNull("Record should be retrievable", retrieved)
        assertEquals("event_001", retrieved!!.eventId)
        assertEquals("Your BBQ (2pm) is clear.", retrieved.widgetDisplayString)
    }

    @Test
    fun upsert_existingEventId_updatesRecord() = runTest {
        val original = CalendarEventForecast(
            eventId             = "event_002",
            lastWeatherSnapshot = """{"precipProb":0.1}""",
            widgetDisplayString = "Your Hike (9am) is clear.",
            lastUpdatedEpoch    = 1_700_000_000L,
            eventStartEpoch     = 1_700_010_000L
        )
        db.calendarEventForecastDao().upsert(original)

        val updated = original.copy(
            widgetDisplayString = "Your Hike (9am) Rain expected.",
            lastUpdatedEpoch    = 1_700_001_000L
        )
        db.calendarEventForecastDao().upsert(updated)

        val retrieved = db.calendarEventForecastDao().getByEventId("event_002")
        assertNotNull(retrieved)
        assertEquals("Updated string should be stored", "Your Hike (9am) Rain expected.", retrieved!!.widgetDisplayString)
        assertEquals(1_700_001_000L, retrieved.lastUpdatedEpoch)
    }

    @Test
    fun deleteExpired_removesOldRecords_keepsCurrentOnes() = runTest {
        val oldRecord = CalendarEventForecast(
            eventId             = "event_old",
            lastWeatherSnapshot = "{}",
            widgetDisplayString = "old",
            lastUpdatedEpoch    = 1_000_000_000L,
            eventStartEpoch     = 1_000_000_000L // well in the past
        )
        val newRecord = CalendarEventForecast(
            eventId             = "event_new",
            lastWeatherSnapshot = "{}",
            widgetDisplayString = "new",
            lastUpdatedEpoch    = 9_999_999_999L,
            eventStartEpoch     = 9_999_999_999L // far in the future
        )
        db.calendarEventForecastDao().upsert(oldRecord)
        db.calendarEventForecastDao().upsert(newRecord)

        val cutoff = System.currentTimeMillis() / 1000L
        db.calendarEventForecastDao().deleteExpired(cutoff)

        assertNull("Old record should be deleted", db.calendarEventForecastDao().getByEventId("event_old"))
        assertNotNull("New record should remain", db.calendarEventForecastDao().getByEventId("event_new"))
    }

    @Test
    fun getByEventId_nonExistent_returnsNull() = runTest {
        val result = db.calendarEventForecastDao().getByEventId("nonexistent")
        assertNull(result)
    }
}
