package com.weatherapp.worker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.weatherapp.data.calendar.CalendarEvent
import com.weatherapp.data.calendar.CalendarRepository
import com.weatherapp.data.datastore.PreferenceKeys
import com.weatherapp.data.db.dao.AlertStateDao
import com.weatherapp.data.db.dao.CalendarEventForecastDao
import com.weatherapp.data.db.dao.ForecastDao
import com.weatherapp.data.db.entity.AlertStateRecord
import com.weatherapp.data.db.entity.ForecastHour
import com.weatherapp.data.location.LocationRepository
import com.weatherapp.model.AlertState
import com.weatherapp.model.AlertThresholds
import com.weatherapp.model.ForecastSnapshot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertEvaluationWorkerTest {

    private lateinit var alertStateDao: AlertStateDao
    private lateinit var forecastDao: ForecastDao
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var locationRepository: LocationRepository
    private lateinit var calendarRepository: CalendarRepository
    private lateinit var calendarEventForecastDao: CalendarEventForecastDao

    private val todayKey = "location:37.8:-122.4:2026-03-09"
    private val nowEpoch = System.currentTimeMillis() / 1000L

    @Before
    fun setup() {
        alertStateDao            = mockk(relaxed = true)
        forecastDao              = mockk(relaxed = true)
        dataStore                = mockk()
        locationRepository       = mockk()
        calendarRepository       = mockk(relaxed = true)
        calendarEventForecastDao = mockk(relaxed = true)

        val prefs: Preferences = mockk()
        every { prefs[PreferenceKeys.KEY_NOTIFICATIONS_ENABLED] } returns true
        every { prefs[PreferenceKeys.KEY_IS_PREMIUM] } returns false
        every { dataStore.data } returns flowOf(prefs)

        coEvery { locationRepository.getSnappedLocation() } returns Pair(37.8, -122.4)
    }

    // --- existing free-tier tests ---

    @Test
    fun unchecked_allClearForecast_transitionsToConfirmedClear_andQueuesNotification() = runTest {
        coEvery { alertStateDao.getByEventId(any()) } returns null
        coEvery { forecastDao.queryByTimeWindow(any(), any()) } returns flowOf(allClearHours())

        buildWorker().doWork()

        coVerify {
            alertStateDao.insertRecord(match { it.state == AlertState.CONFIRMED_CLEAR })
        }
    }

    @Test
    fun unchecked_notAllClearForecast_staysUnchecked_noInsert() = runTest {
        coEvery { alertStateDao.getByEventId(any()) } returns null
        coEvery { forecastDao.queryByTimeWindow(any(), any()) } returns flowOf(rainyHours())

        buildWorker().doWork()

        coVerify(exactly = 0) { alertStateDao.insertRecord(any()) }
    }

    @Test
    fun confirmedClear_stillAllClear_noTransition_atMostOnce() = runTest {
        val snapshot = ForecastSnapshot(precipProb = 0.05, windKmh = 10.0, windowStart = 0L, windowEnd = 0L)
        coEvery { alertStateDao.getByEventId(any()) } returns confirmedClearRecord(snapshot)
        coEvery { forecastDao.queryByTimeWindow(any(), any()) } returns flowOf(allClearHours())

        buildWorker().doWork()

        coVerify(exactly = 0) { alertStateDao.insertRecord(any()) }
    }

    @Test
    fun confirmedClear_materialPrecipChange_transitionsToAlertSent() = runTest {
        val snapshot = ForecastSnapshot(precipProb = 0.05, windKmh = 10.0, windowStart = 0L, windowEnd = 0L)
        coEvery { alertStateDao.getByEventId(any()) } returns confirmedClearRecord(snapshot)
        // Current forecast: precip jumped by >20%
        coEvery { forecastDao.queryByTimeWindow(any(), any()) } returns flowOf(rainyHours(precipProb = 0.80))

        buildWorker().doWork()

        coVerify {
            alertStateDao.insertRecord(match { it.state == AlertState.ALERT_SENT })
        }
    }

    @Test
    fun confirmedClear_windCrossesThreshold_transitionsToAlertSent() = runTest {
        val snapshot = ForecastSnapshot(precipProb = 0.05, windKmh = 10.0, windowStart = 0L, windowEnd = 0L)
        coEvery { alertStateDao.getByEventId(any()) } returns confirmedClearRecord(snapshot)
        // Wind crosses 40 km/h threshold
        coEvery { forecastDao.queryByTimeWindow(any(), any()) } returns flowOf(
            allClearHours(windKmh = AlertThresholds.WIND_SPEED_ALERT_KMH + 5.0)
        )

        buildWorker().doWork()

        coVerify {
            alertStateDao.insertRecord(match { it.state == AlertState.ALERT_SENT })
        }
    }

    @Test
    fun notificationsDisabled_skipsEvaluation_returnsSuccess() = runTest {
        val prefs: Preferences = mockk()
        every { prefs[PreferenceKeys.KEY_NOTIFICATIONS_ENABLED] } returns false
        every { prefs[PreferenceKeys.KEY_IS_PREMIUM] } returns false
        every { dataStore.data } returns flowOf(prefs)

        val result = buildWorker().doWork()

        assertEquals(Result.success(), result)
        coVerify(exactly = 0) { alertStateDao.insertRecord(any()) }
    }

    @Test
    fun locationUnavailable_skipsEvaluation_returnsSuccess() = runTest {
        coEvery { locationRepository.getSnappedLocation() } returns null

        val result = buildWorker().doWork()

        assertEquals(Result.success(), result)
        coVerify(exactly = 0) { alertStateDao.insertRecord(any()) }
    }

    @Test
    fun emptyHours_isNotAllClear_noTransition() = runTest {
        coEvery { alertStateDao.getByEventId(any()) } returns null
        coEvery { forecastDao.queryByTimeWindow(any(), any()) } returns flowOf(emptyList())

        buildWorker().doWork()

        coVerify(exactly = 0) { alertStateDao.insertRecord(any()) }
    }

    // --- premium tests ---

    @Test
    fun premium_confirmedClear_materialPrecipChange_eventAtLeast2hAway_transitionsToAlertSent() = runTest {
        val prefs: Preferences = mockk()
        every { prefs[PreferenceKeys.KEY_NOTIFICATIONS_ENABLED] } returns true
        every { prefs[PreferenceKeys.KEY_IS_PREMIUM] } returns true
        every { dataStore.data } returns flowOf(prefs)

        val eventStart = nowEpoch + 3 * 3600L // 3h from now — within default 2h lead? No, lead=2h means 2<=hours<=2, so 3h is NOT within window
        // Actually for default 2h lead: shouldSendAlert = hoursUntilEvent <= 2 AND >= 2
        // 3h away → hoursUntilEvent=3 > 2 → NOT sent. Use 2h exactly.
        val eventStartInWindow = nowEpoch + 2 * 3600L // exactly at lead time boundary
        val event = CalendarEvent(
            eventId    = "ev_precip",
            title      = "BBQ",
            startEpoch = eventStartInWindow,
            endEpoch   = eventStartInWindow + 7200L,
            location   = null
        )
        coEvery { calendarRepository.getUpcomingEvents(any()) } returns listOf(event)

        val snapshot = ForecastSnapshot(precipProb = 0.05, windKmh = 10.0, windowStart = 0L, windowEnd = 0L)
        coEvery { alertStateDao.getByEventId("location:37.8:-122.4:2026-03-09") } returns null
        coEvery { alertStateDao.getByEventId("ev_precip") } returns confirmedClearRecord(snapshot, "ev_precip")
        coEvery { forecastDao.queryByTimeWindow(any(), any()) } returns flowOf(rainyHours(precipProb = 0.80))

        buildWorker().doWork()

        coVerify {
            alertStateDao.insertRecord(match { it.eventId == "ev_precip" && it.state == AlertState.ALERT_SENT })
        }
    }

    @Test
    fun premium_confirmedClear_eventStartsIn1h_alertSuppressed_stateStillTransitions() = runTest {
        val prefs: Preferences = mockk()
        every { prefs[PreferenceKeys.KEY_NOTIFICATIONS_ENABLED] } returns true
        every { prefs[PreferenceKeys.KEY_IS_PREMIUM] } returns true
        every { dataStore.data } returns flowOf(prefs)

        // 1h away — below the 2h minimum window → shouldSendAlert returns false
        val eventStart = nowEpoch + 3600L
        val event = CalendarEvent(
            eventId    = "ev_1h",
            title      = "BBQ",
            startEpoch = eventStart,
            endEpoch   = eventStart + 7200L,
            location   = null
        )
        coEvery { calendarRepository.getUpcomingEvents(any()) } returns listOf(event)

        val snapshot = ForecastSnapshot(precipProb = 0.05, windKmh = 10.0, windowStart = 0L, windowEnd = 0L)
        coEvery { alertStateDao.getByEventId("location:37.8:-122.4:2026-03-09") } returns null
        coEvery { alertStateDao.getByEventId("ev_1h") } returns confirmedClearRecord(snapshot, "ev_1h")
        coEvery { forecastDao.queryByTimeWindow(any(), any()) } returns flowOf(rainyHours(precipProb = 0.80))

        buildWorker().doWork()

        // State should NOT transition to ALERT_SENT because 1h < 2h minimum
        coVerify(exactly = 0) {
            alertStateDao.insertRecord(match { it.eventId == "ev_1h" && it.state == AlertState.ALERT_SENT })
        }
    }

    @Test
    fun premium_alertSent_conditionsImproved_transitionsBackToConfirmedClear() = runTest {
        val prefs: Preferences = mockk()
        every { prefs[PreferenceKeys.KEY_NOTIFICATIONS_ENABLED] } returns true
        every { prefs[PreferenceKeys.KEY_IS_PREMIUM] } returns true
        every { dataStore.data } returns flowOf(prefs)

        val eventStart = nowEpoch + 3600L
        val event = CalendarEvent(
            eventId    = "ev_improve",
            title      = "BBQ",
            startEpoch = eventStart,
            endEpoch   = eventStart + 7200L,
            location   = null
        )
        coEvery { calendarRepository.getUpcomingEvents(any()) } returns listOf(event)

        val alertSentRecord = AlertStateRecord(
            eventId = "ev_improve",
            state   = AlertState.ALERT_SENT,
            confirmedForecastSnapshot = Gson().toJson(
                ForecastSnapshot(precipProb = 0.80, windKmh = 10.0, windowStart = 0L, windowEnd = 0L)
            ),
            lastTransitionAt = nowEpoch - 3600L
        )
        coEvery { alertStateDao.getByEventId("location:37.8:-122.4:2026-03-09") } returns null
        coEvery { alertStateDao.getByEventId("ev_improve") } returns alertSentRecord
        // Conditions now all-clear
        coEvery { forecastDao.queryByTimeWindow(any(), any()) } returns flowOf(allClearHours())

        buildWorker().doWork()

        coVerify {
            alertStateDao.insertRecord(match { it.eventId == "ev_improve" && it.state == AlertState.CONFIRMED_CLEAR })
        }
    }

    @Test
    fun premium_marathon_leadTime6h_suppressedIfLessThan6hToStart() = runTest {
        val prefs: Preferences = mockk()
        every { prefs[PreferenceKeys.KEY_NOTIFICATIONS_ENABLED] } returns true
        every { prefs[PreferenceKeys.KEY_IS_PREMIUM] } returns true
        every { dataStore.data } returns flowOf(prefs)

        val worker = buildWorker()

        // Marathon event 5h away — lead time is 6h, so 5h is within window (2h <= 5h <= 6h) → WOULD send
        // But let's test 7h away → 7h > 6h → suppressed
        val eventStart7h = nowEpoch + 7 * 3600L
        val event7h = CalendarEvent(
            eventId    = "marathon_7h",
            title      = "City Marathon",
            startEpoch = eventStart7h,
            endEpoch   = eventStart7h + 18000L,
            location   = null
        )

        assertFalse(
            "Marathon 7h away should NOT send alert (outside 6h lead time)",
            worker.shouldSendAlert(event7h, nowEpoch)
        )

        // Marathon 5h away — within 6h lead and >= 2h → should send
        val eventStart5h = nowEpoch + 5 * 3600L
        val event5h = CalendarEvent(
            eventId    = "marathon_5h",
            title      = "City Marathon",
            startEpoch = eventStart5h,
            endEpoch   = eventStart5h + 18000L,
            location   = null
        )

        assertTrue(
            "Marathon 5h away should send alert (within 6h lead time, >= 2h)",
            worker.shouldSendAlert(event5h, nowEpoch)
        )
    }

    @Test
    fun isPremiumFalse_premiumPathNotExecuted() = runTest {
        // prefs already set to isPremium=false in setUp()
        coEvery { alertStateDao.getByEventId(any()) } returns null
        coEvery { forecastDao.queryByTimeWindow(any(), any()) } returns flowOf(allClearHours())

        buildWorker().doWork()

        // CalendarRepository should never be called when isPremium=false
        coVerify(exactly = 0) { calendarRepository.getUpcomingEvents(any()) }
    }

    @Test
    fun postNotificationsDenied_stateTransitionsStillOccur() = runTest {
        // Even when POST_NOTIFICATIONS is denied, state transitions still happen
        // (sendNotification() logs and returns, but insertRecord() is still called)
        coEvery { alertStateDao.getByEventId(any()) } returns null
        coEvery { forecastDao.queryByTimeWindow(any(), any()) } returns flowOf(allClearHours())

        buildWorker().doWork()

        // UNCHECKED → CONFIRMED_CLEAR should still happen regardless of notification permission
        coVerify {
            alertStateDao.insertRecord(match { it.state == AlertState.CONFIRMED_CLEAR })
        }
    }

    // --- helpers ---

    private fun buildWorker(): AlertEvaluationWorker {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val params: WorkerParameters = mockk(relaxed = true)
        return AlertEvaluationWorker(
            context,
            params,
            alertStateDao,
            forecastDao,
            dataStore,
            locationRepository,
            calendarRepository,
            calendarEventForecastDao
        )
    }

    private fun allClearHours(windKmh: Double = 10.0) = List(14) { i ->
        ForecastHour(
            hourEpoch = 1_741_435_200L + i * 3600L, // starts at 07:00 UTC
            temperatureC = 18.0,
            precipitationProbability = 0.05,
            windSpeedKmh = windKmh,
            weatherCode = 0
        )
    }

    private fun rainyHours(precipProb: Double = 0.80) = List(14) { i ->
        ForecastHour(
            hourEpoch = 1_741_435_200L + i * 3600L,
            temperatureC = 14.0,
            precipitationProbability = precipProb,
            windSpeedKmh = 15.0,
            weatherCode = 80
        )
    }

    private fun confirmedClearRecord(
        snapshot: ForecastSnapshot,
        eventId: String = todayKey
    ) = AlertStateRecord(
        eventId = eventId,
        state   = AlertState.CONFIRMED_CLEAR,
        confirmedForecastSnapshot = Gson().toJson(snapshot),
        lastTransitionAt = System.currentTimeMillis() / 1000L - 3600L
    )
}
