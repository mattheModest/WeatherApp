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
import com.weatherapp.data.datastore.PreferenceKeys
import com.weatherapp.data.db.dao.AlertStateDao
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

    private val todayKey = "location:37.8:-122.4:2026-03-09"

    @Before
    fun setup() {
        alertStateDao      = mockk(relaxed = true)
        forecastDao        = mockk(relaxed = true)
        dataStore          = mockk()
        locationRepository = mockk()

        val prefs: Preferences = mockk()
        every { prefs[PreferenceKeys.KEY_NOTIFICATIONS_ENABLED] } returns true
        every { dataStore.data } returns flowOf(prefs)

        coEvery { locationRepository.getSnappedLocation() } returns Pair(37.8, -122.4)
    }

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

    // --- helpers ---

    private fun buildWorker(): AlertEvaluationWorker {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val params: WorkerParameters = mockk(relaxed = true)
        return AlertEvaluationWorker(context, params, alertStateDao, forecastDao, dataStore, locationRepository)
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

    private fun confirmedClearRecord(snapshot: ForecastSnapshot) = AlertStateRecord(
        eventId = todayKey,
        state   = AlertState.CONFIRMED_CLEAR,
        confirmedForecastSnapshot = Gson().toJson(snapshot),
        lastTransitionAt = System.currentTimeMillis() / 1000L - 3600L
    )
}
