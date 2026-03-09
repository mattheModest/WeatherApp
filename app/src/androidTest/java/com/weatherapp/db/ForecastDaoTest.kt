package com.weatherapp.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.weatherapp.data.db.AppDatabase
import com.weatherapp.data.db.dao.ForecastDao
import com.weatherapp.data.db.entity.ForecastHour
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ForecastDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ForecastDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.forecastDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun hour(epoch: Long) = ForecastHour(
        hourEpoch = epoch,
        temperatureC = 15.0,
        precipitationProbability = 0.2,
        windSpeedKmh = 10.0,
        weatherCode = 1
    )

    @Test
    fun insert_andQuery_returnsCorrectRows() = runTest {
        dao.insert(listOf(hour(1000L), hour(2000L), hour(3000L)))

        val result = dao.queryByTimeWindow(1000L, 2000L).first()

        assertEquals(2, result.size)
        assertEquals(1000L, result[0].hourEpoch)
        assertEquals(2000L, result[1].hourEpoch)
    }

    @Test
    fun insert_withDuplicateEpoch_replacesExistingRow() = runTest {
        dao.insert(listOf(hour(1000L)))
        dao.insert(listOf(hour(1000L).copy(temperatureC = 25.0)))

        val result = dao.queryByTimeWindow(1000L, 1000L).first()

        assertEquals(1, result.size)
        assertEquals(25.0, result[0].temperatureC, 0.001)
    }

    @Test
    fun deleteExpired_removesOldRows() = runTest {
        dao.insert(listOf(hour(500L), hour(1000L), hour(2000L)))

        dao.deleteExpired(1000L)

        val result = dao.queryByTimeWindow(0L, Long.MAX_VALUE).first()
        assertEquals(2, result.size)
        assertTrue(result.none { it.hourEpoch < 1000L })
    }

    @Test
    fun queryByTimeWindow_returnsRowsInAscendingOrder() = runTest {
        dao.insert(listOf(hour(3000L), hour(1000L), hour(2000L)))

        val result = dao.queryByTimeWindow(1000L, 3000L).first()

        assertEquals(listOf(1000L, 2000L, 3000L), result.map { it.hourEpoch })
    }

    @Test
    fun queryByTimeWindow_returnsEmpty_whenNoRowsInRange() = runTest {
        dao.insert(listOf(hour(5000L)))

        val result = dao.queryByTimeWindow(1000L, 2000L).first()

        assertTrue(result.isEmpty())
    }
}
