package com.weatherapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.weatherapp.data.db.entity.AlertStateRecord

@Dao
interface AlertStateDao {

    // Append-only insert — never UPDATE in business logic; INSERT with REPLACE semantics
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AlertStateRecord)

    @Query("SELECT * FROM alert_state_record WHERE event_id = :eventId LIMIT 1")
    suspend fun getByEventId(eventId: String): AlertStateRecord?

    // Cleanup-only UPDATE is acceptable for archiving expired records
    @Query("""
        UPDATE alert_state_record
        SET state = 'RESOLVED', last_transition_at = :nowEpoch
        WHERE last_transition_at < :beforeEpoch AND state != 'RESOLVED'
    """)
    suspend fun resolveExpired(beforeEpoch: Long, nowEpoch: Long)
}
