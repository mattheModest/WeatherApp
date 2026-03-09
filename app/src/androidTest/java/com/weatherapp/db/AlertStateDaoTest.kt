package com.weatherapp.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.weatherapp.data.db.AppDatabase
import com.weatherapp.data.db.entity.AlertStateRecord
import com.weatherapp.model.AlertState
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertStateDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertRecord_newEventId_rowExists() = runTest {
        val record = makeRecord("event-1", AlertState.UNCHECKED)
        db.alertStateDao().insertRecord(record)
        val fetched = db.alertStateDao().getByEventId("event-1")
        assertNotNull(fetched)
        assertEquals(AlertState.UNCHECKED, fetched!!.state)
    }

    @Test
    fun insertRecord_existingEventId_rowIsReplaced() = runTest {
        val dao = db.alertStateDao()
        dao.insertRecord(makeRecord("event-2", AlertState.UNCHECKED, epoch = 1000L))
        dao.insertRecord(makeRecord("event-2", AlertState.CONFIRMED_CLEAR, epoch = 2000L))
        val fetched = dao.getByEventId("event-2")
        assertNotNull(fetched)
        assertEquals(AlertState.CONFIRMED_CLEAR, fetched!!.state)
        assertEquals(2000L, fetched.lastTransitionAt)
        // Should be exactly one row — no duplicate
        assertEquals(fetched.eventId, "event-2")
    }

    @Test
    fun getByEventId_returnsCorrectRecord() = runTest {
        val dao = db.alertStateDao()
        dao.insertRecord(makeRecord("event-A", AlertState.ALERT_SENT))
        dao.insertRecord(makeRecord("event-B", AlertState.CONFIRMED_CLEAR))
        val fetched = dao.getByEventId("event-A")
        assertEquals(AlertState.ALERT_SENT, fetched?.state)
    }

    @Test
    fun getByEventId_returnsNull_forUnknownEventId() = runTest {
        val result = db.alertStateDao().getByEventId("nonexistent")
        assertNull(result)
    }

    @Test
    fun resolveExpired_setsResolvedForOldRecords() = runTest {
        val dao = db.alertStateDao()
        val nowEpoch = System.currentTimeMillis() / 1000L
        dao.insertRecord(makeRecord("old-event", AlertState.CONFIRMED_CLEAR, epoch = nowEpoch - 100_000L))
        dao.insertRecord(makeRecord("new-event", AlertState.CONFIRMED_CLEAR, epoch = nowEpoch))

        dao.resolveExpired(beforeEpoch = nowEpoch - 50_000L, nowEpoch = nowEpoch)

        assertEquals(AlertState.RESOLVED, dao.getByEventId("old-event")?.state)
        assertEquals(AlertState.CONFIRMED_CLEAR, dao.getByEventId("new-event")?.state)
    }

    @Test
    fun resolveExpired_doesNotAffectAlreadyResolvedRecords() = runTest {
        val dao = db.alertStateDao()
        val nowEpoch = System.currentTimeMillis() / 1000L
        val oldResolved = makeRecord("resolved-event", AlertState.RESOLVED, epoch = nowEpoch - 200_000L)
        dao.insertRecord(oldResolved)

        dao.resolveExpired(beforeEpoch = nowEpoch, nowEpoch = nowEpoch)

        // lastTransitionAt should not change since WHERE state != 'RESOLVED'
        val fetched = dao.getByEventId("resolved-event")
        assertEquals(AlertState.RESOLVED, fetched?.state)
        assertEquals(oldResolved.lastTransitionAt, fetched?.lastTransitionAt)
    }

    @Test
    fun typeConverter_roundTripsAllAlertStateValues() = runTest {
        val dao = db.alertStateDao()
        AlertState.entries.forEachIndexed { index, state ->
            dao.insertRecord(makeRecord("event-state-$index", state))
            val fetched = dao.getByEventId("event-state-$index")
            assertEquals("Round-trip failed for $state", state, fetched?.state)
        }
    }

    private fun makeRecord(
        eventId: String,
        state: AlertState,
        epoch: Long = 1_000_000L
    ) = AlertStateRecord(
        eventId = eventId,
        state = state,
        confirmedForecastSnapshot = "{}",
        lastTransitionAt = epoch
    )
}
