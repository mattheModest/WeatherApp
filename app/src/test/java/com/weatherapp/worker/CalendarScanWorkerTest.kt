package com.weatherapp.worker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker.Result
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.weatherapp.data.calendar.CalendarEvent
import com.weatherapp.data.calendar.CalendarRepository
import com.weatherapp.data.datastore.PreferenceKeys
import com.weatherapp.data.db.dao.CalendarEventForecastDao
import com.weatherapp.data.db.dao.ForecastDao
import com.weatherapp.data.db.entity.ForecastHour
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CalendarScanWorkerTest {

    private lateinit var calendarRepository: CalendarRepository
    private lateinit var forecastDao: ForecastDao
    private lateinit var calendarEventForecastDao: CalendarEventForecastDao
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var workManager: WorkManager

    private val nowEpoch = System.currentTimeMillis() / 1000L

    @Before
    fun setUp() {
        calendarRepository       = mockk(relaxed = true)
        forecastDao              = mockk(relaxed = true)
        calendarEventForecastDao = mockk(relaxed = true)
        dataStore                = mockk(relaxed = true)
        workManager              = mockk(relaxed = true)

        // Default: premium
        val prefs = mockk<Preferences> {
            every { this@mockk[PreferenceKeys.KEY_IS_PREMIUM] } returns true
        }
        every { dataStore.data } returns flowOf(prefs)
        coEvery { forecastDao.queryByTimeWindow(any(), any()) } returns flowOf(allClearHours())
    }

    // --- isOutdoorPotential tests ---

    @Test
    fun isOutdoorPotential_bbqAtPark_60min_returnsTrue() {
        val worker = buildWorker()
        val event = CalendarEvent(
            eventId    = "1",
            title      = "BBQ at park",
            startEpoch = nowEpoch,
            endEpoch   = nowEpoch + 3600L, // 60 min
            location   = null
        )
        assertTrue(worker.isOutdoorPotential(event))
    }

    @Test
    fun isOutdoorPotential_doctorAppointment_60min_returnsFalse() {
        val worker = buildWorker()
        val event = CalendarEvent(
            eventId    = "2",
            title      = "Doctor appointment",
            startEpoch = nowEpoch,
            endEpoch   = nowEpoch + 3600L, // 60 min
            location   = null
        )
        assertFalse(worker.isOutdoorPotential(event))
    }

    @Test
    fun isOutdoorPotential_run_15min_returnsFalse_tooShort() {
        val worker = buildWorker()
        val event = CalendarEvent(
            eventId    = "3",
            title      = "run",
            startEpoch = nowEpoch,
            endEpoch   = nowEpoch + 900L, // 15 min
            location   = null
        )
        assertFalse(worker.isOutdoorPotential(event))
    }

    @Test
    fun isOutdoorPotential_run_30min_returnsTrue() {
        val worker = buildWorker()
        val event = CalendarEvent(
            eventId    = "4",
            title      = "Morning run",
            startEpoch = nowEpoch,
            endEpoch   = nowEpoch + 1800L, // 30 min
            location   = null
        )
        assertTrue(worker.isOutdoorPotential(event))
    }

    // --- isPremium guard ---

    @Test
    fun isPremiumFalse_exitsImmediately_noCalendarQuery() = runTest {
        val prefs = mockk<Preferences> {
            every { this@mockk[PreferenceKeys.KEY_IS_PREMIUM] } returns false
        }
        every { dataStore.data } returns flowOf(prefs)

        val result = buildWorker().doWork()

        assertEquals(Result.success(), result)
        coVerify(exactly = 0) { calendarRepository.getUpcomingEvents(any()) }
    }

    // --- single outdoor event verdict ---

    @Test
    fun singleOutdoorEvent_next24h_writesEventSpecificVerdict() = runTest {
        val eventStart = nowEpoch + 3600L // 1h from now
        val event = CalendarEvent(
            eventId    = "5",
            title      = "BBQ",
            startEpoch = eventStart,
            endEpoch   = eventStart + 7200L,
            location   = null
        )
        coEvery { calendarRepository.getUpcomingEvents(any()) } returns listOf(event)

        val editSlot = slot<suspend (MutablePreferences) -> Unit>()
        val mutablePrefs = mockk<MutablePreferences>(relaxed = true)
        coEvery { dataStore.edit(capture(editSlot)) } coAnswers {
            editSlot.captured.invoke(mutablePrefs)
            mutablePrefs
        }

        buildWorker().doWork()

        coVerify {
            mutablePrefs[PreferenceKeys.KEY_WIDGET_VERDICT] = match { verdict ->
                verdict.contains("BBQ") && (verdict.contains("is clear") || verdict.contains("Rain") || verdict.contains("Windy"))
            }
        }
    }

    // --- two overlapping outdoor events → conflict message ---

    @Test
    fun twoOverlappingOutdoorEvents_next24h_writesConflictVerdict() = runTest {
        val eventStart = nowEpoch + 3600L
        val event1 = CalendarEvent("6", "BBQ", eventStart, eventStart + 7200L, null)
        val event2 = CalendarEvent("7", "Picnic", eventStart + 1800L, eventStart + 9000L, null) // overlaps event1
        coEvery { calendarRepository.getUpcomingEvents(any()) } returns listOf(event1, event2)

        val editSlot = slot<suspend (MutablePreferences) -> Unit>()
        val mutablePrefs = mockk<MutablePreferences>(relaxed = true)
        coEvery { dataStore.edit(capture(editSlot)) } coAnswers {
            editSlot.captured.invoke(mutablePrefs)
            mutablePrefs
        }

        buildWorker().doWork()

        coVerify {
            mutablePrefs[PreferenceKeys.KEY_WIDGET_VERDICT] = match { verdict ->
                verdict.contains("outdoor events") && verdict.contains("Check both")
            }
        }
    }

    // --- detectConflicts ---

    @Test
    fun detectConflicts_overlappingEvents_returnsBothAsConflicting() {
        val worker = buildWorker()
        val event1 = CalendarEvent("8", "BBQ", nowEpoch,          nowEpoch + 7200L, null)
        val event2 = CalendarEvent("9", "Run", nowEpoch + 3600L,  nowEpoch + 10800L, null)
        val conflicts = worker.detectConflicts(listOf(event1, event2))
        assertEquals(2, conflicts.size)
        assertTrue(conflicts.any { it.eventId == "8" })
        assertTrue(conflicts.any { it.eventId == "9" })
    }

    @Test
    fun detectConflicts_nonOverlappingEvents_returnsEmpty() {
        val worker = buildWorker()
        val event1 = CalendarEvent("10", "BBQ", nowEpoch,          nowEpoch + 3600L, null)
        val event2 = CalendarEvent("11", "Run", nowEpoch + 7200L,  nowEpoch + 10800L, null)
        val conflicts = worker.detectConflicts(listOf(event1, event2))
        assertTrue(conflicts.isEmpty())
    }

    @Test
    fun detectConflicts_singleEvent_returnsEmpty() {
        val worker = buildWorker()
        val event = CalendarEvent("12", "BBQ", nowEpoch, nowEpoch + 3600L, null)
        val conflicts = worker.detectConflicts(listOf(event))
        assertTrue(conflicts.isEmpty())
    }

    // --- helpers ---

    private fun buildWorker(): CalendarScanWorker {
        val context = mockk<Context>(relaxed = true)
        val params: WorkerParameters = mockk(relaxed = true)
        return CalendarScanWorker(
            context,
            params,
            calendarRepository,
            forecastDao,
            calendarEventForecastDao,
            dataStore,
            workManager
        )
    }

    private fun allClearHours() = List(14) { i ->
        ForecastHour(
            hourEpoch = 1_741_435_200L + i * 3600L,
            temperatureC = 18.0,
            precipitationProbability = 0.05,
            windSpeedKmh = 10.0,
            weatherCode = 0
        )
    }
}
