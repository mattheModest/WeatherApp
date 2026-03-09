package com.weatherapp.worker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.weatherapp.data.datastore.PreferenceKeys
import com.weatherapp.data.db.dao.ForecastDao
import com.weatherapp.data.weather.WeatherRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ForecastRefreshWorkerTest {

    private lateinit var weatherRepository: WeatherRepository
    private lateinit var forecastDao: ForecastDao
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var mutablePrefs: MutablePreferences

    // Ordered list of (key, value) writes — preserves sequence
    private val writeLog = mutableListOf<Pair<Preferences.Key<*>, Any>>()

    @Before
    fun setup() {
        writeLog.clear()
        weatherRepository = mockk()
        forecastDao = mockk(relaxed = true)
        dataStore = mockk()
        mutablePrefs = mockk(relaxed = true)

        every { mutablePrefs.set(any<Preferences.Key<Boolean>>(), any()) } answers {
            writeLog.add(Pair(firstArg<Preferences.Key<Boolean>>(), secondArg<Boolean>()))
        }
        every { mutablePrefs.set(any<Preferences.Key<Long>>(), any()) } answers {
            writeLog.add(Pair(firstArg<Preferences.Key<Long>>(), secondArg<Long>()))
        }

        val prefs: Preferences = mockk()
        every { prefs[PreferenceKeys.KEY_STALENESS_FLAG] } returns null
        every { dataStore.data } returns flowOf(prefs)

        coEvery { dataStore.edit(any()) } coAnswers {
            firstArg<suspend (MutablePreferences) -> Unit>().invoke(mutablePrefs)
            mutablePrefs
        }
    }

    private fun buildWorker(runAttemptCount: Int = 0): ForecastRefreshWorker {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val params: WorkerParameters = mockk(relaxed = true) {
            every { this@mockk.runAttemptCount } returns runAttemptCount
        }
        return ForecastRefreshWorker(context, params, weatherRepository, forecastDao, dataStore)
    }

    @Test
    fun stalenessFlag_isWrittenTrueBeforeFetch() = runTest {
        coEvery { weatherRepository.fetchForecast() } returns kotlin.Result.success(Unit)

        buildWorker().doWork()

        // First staleness write must be true
        val firstStalenessWrite = writeLog.first { it.first == PreferenceKeys.KEY_STALENESS_FLAG }
        assertEquals(true, firstStalenessWrite.second)
    }

    @Test
    fun stalenessFlag_isClearedOnSuccess() = runTest {
        coEvery { weatherRepository.fetchForecast() } returns kotlin.Result.success(Unit)

        buildWorker().doWork()

        // Last staleness write must be false
        val lastStalenessWrite = writeLog.last { it.first == PreferenceKeys.KEY_STALENESS_FLAG }
        assertEquals(false, lastStalenessWrite.second)
    }

    @Test
    fun returnsRetry_onIOException_whenAttemptCountLessThan3() = runTest {
        coEvery { weatherRepository.fetchForecast() } throws IOException("timeout")

        val result = buildWorker(runAttemptCount = 0).doWork()

        assertEquals(Result.retry(), result)
    }

    @Test
    fun returnsFailure_onIOException_after3Attempts() = runTest {
        coEvery { weatherRepository.fetchForecast() } throws IOException("timeout")

        val result = buildWorker(runAttemptCount = 3).doWork()

        assertEquals(Result.failure(), result)
    }

    @Test
    fun stalenessFlag_remainsSet_afterFailure() = runTest {
        coEvery { weatherRepository.fetchForecast() } throws IOException("timeout")

        buildWorker(runAttemptCount = 3).doWork()

        // Staleness was set to true; on failure no clearing write occurs
        val stalenessWrites = writeLog.filter { it.first == PreferenceKeys.KEY_STALENESS_FLAG }
        assertTrue("Expected at least one staleness write", stalenessWrites.isNotEmpty())
        assertEquals("Staleness should remain true after failure", true, stalenessWrites.last().second)
    }
}
