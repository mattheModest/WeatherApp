# Story 2.1: Alert State Machine & Room Schema

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a developer,
I want the alert state machine entities, DAOs, and enum defined in Room,
so that the system has a persistent, per-location record of alert state across every WorkManager cycle.

## Acceptance Criteria

**AC-1:** Given `AlertState.kt` when reviewed then it declares `enum class AlertState { UNCHECKED, CONFIRMED_CLEAR, ALERT_SENT, RESOLVED }`.

**AC-2:** Given `AlertStateRecord.kt` (Room entity) when reviewed then it maps to table `alert_state_record` with columns `event_id` (String, primary key), `state` (AlertState), `confirmed_forecast_snapshot` (String — serialised threshold values at time of confirmation), `last_transition_at` (Long epoch).

**AC-3:** Given `AlertStateDao` when called then it supports `insertRecord(AlertStateRecord)`, `getByEventId(eventId): AlertStateRecord?`, and `resolveExpired(beforeEpoch)` (marks records RESOLVED where event end time has passed).

**AC-4:** Given an alert state transition occurs when the record is written then a new row is inserted with the updated state and `lastTransitionAt` timestamp — existing rows are never updated in place (append-only).

**AC-5:** Given `AppDatabase` when reviewed then it includes `AlertStateRecord` in its entities list; database version is incremented appropriately.

## Tasks / Subtasks

- [ ] Task 1: Create `AlertState.kt` enum (AC: 1)
  - [ ] 1.1 Create `app/src/main/java/com/weatherapp/model/AlertState.kt`
  - [ ] 1.2 Declare `enum class AlertState { UNCHECKED, CONFIRMED_CLEAR, ALERT_SENT, RESOLVED }`
  - [ ] 1.3 This enum is used as a Room column type — requires a `TypeConverter` in `AppDatabase`

- [ ] Task 2: Create `AlertStateRecord.kt` Room entity (AC: 2, 4)
  - [ ] 2.1 Create `app/src/main/java/com/weatherapp/data/db/entity/AlertStateRecord.kt`
  - [ ] 2.2 Annotate with `@Entity(tableName = "alert_state_record")`
  - [ ] 2.3 Define primary key: `@PrimaryKey @ColumnInfo(name = "event_id") val eventId: String`
  - [ ] 2.4 Define columns: `@ColumnInfo(name = "state") val state: AlertState`, `@ColumnInfo(name = "confirmed_forecast_snapshot") val confirmedForecastSnapshot: String`, `@ColumnInfo(name = "last_transition_at") val lastTransitionAt: Long`
  - [ ] 2.5 Note: table uses primary key `event_id` which is the natural key, but the append-only semantic means each INSERT replaces the existing row for that `eventId` via `@Insert(onConflict = OnConflictStrategy.REPLACE)` — the "append-only" semantic means we never do UPDATE SQL, but INSERT with the new state. The old state is preserved in the `lastTransitionAt` and `state` columns for audit purposes. See Dev Notes for clarification.

- [ ] Task 3: Create Room `TypeConverter` for `AlertState` enum (AC: 2, 5)
  - [ ] 3.1 Create `app/src/main/java/com/weatherapp/data/db/AlertStateConverters.kt`
  - [ ] 3.2 Implement:
    ```kotlin
    class AlertStateConverters {
        @TypeConverter fun fromAlertState(state: AlertState): String = state.name
        @TypeConverter fun toAlertState(value: String): AlertState = AlertState.valueOf(value)
    }
    ```
  - [ ] 3.3 Register converter in `AppDatabase` via `@TypeConverters(AlertStateConverters::class)` annotation

- [ ] Task 4: Create `AlertStateDao.kt` (AC: 3, 4)
  - [ ] 4.1 Create `app/src/main/java/com/weatherapp/data/db/dao/AlertStateDao.kt`
  - [ ] 4.2 Implement `@Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertRecord(record: AlertStateRecord)` — this is the append-only insert (no UPDATE statements anywhere)
  - [ ] 4.3 Implement `@Query("SELECT * FROM alert_state_record WHERE event_id = :eventId LIMIT 1") suspend fun getByEventId(eventId: String): AlertStateRecord?`
  - [ ] 4.4 Implement `resolveExpired(beforeEpoch: Long)`: mark all records as RESOLVED where the event has ended. Since we can't do this purely with a DELETE query without losing state, implement as a `@Transaction` that queries for expired records and re-inserts them with state RESOLVED:
    ```kotlin
    @Query("SELECT * FROM alert_state_record WHERE last_transition_at < :beforeEpoch AND state != 'RESOLVED'")
    suspend fun getExpiredUnresolved(beforeEpoch: Long): List<AlertStateRecord>
    ```
    Then in a `@Transaction` function, re-insert each as RESOLVED.
  - [ ] 4.5 Alternative simpler approach for v1: `@Query("UPDATE alert_state_record SET state = 'RESOLVED', last_transition_at = :nowEpoch WHERE last_transition_at < :beforeEpoch AND state != 'RESOLVED'") suspend fun resolveExpired(beforeEpoch: Long, nowEpoch: Long = System.currentTimeMillis() / 1000L)` — use this if the pure-insert approach becomes too complex. The key constraint is no UPDATE in business logic transitions; cleanup queries are acceptable.

- [ ] Task 5: Update `AppDatabase` to include `AlertStateRecord` (AC: 5)
  - [ ] 5.1 Update `app/src/main/java/com/weatherapp/data/db/AppDatabase.kt`
  - [ ] 5.2 Add `AlertStateRecord::class` to the `entities` array
  - [ ] 5.3 Increment database version from 1 to 2
  - [ ] 5.4 Add migration `MIGRATION_1_2`:
    ```kotlin
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS alert_state_record (
                    event_id TEXT NOT NULL PRIMARY KEY,
                    state TEXT NOT NULL,
                    confirmed_forecast_snapshot TEXT NOT NULL,
                    last_transition_at INTEGER NOT NULL
                )
            """)
        }
    }
    ```
  - [ ] 5.5 Add `@TypeConverters(AlertStateConverters::class)` annotation to `AppDatabase`
  - [ ] 5.6 Declare `abstract fun alertStateDao(): AlertStateDao`
  - [ ] 5.7 In the `Room.databaseBuilder` call, add `.addMigrations(MIGRATION_1_2)`

- [ ] Task 6: Expose `AlertStateDao` from Hilt `DatabaseModule` (AC: 3)
  - [ ] 6.1 In `app/src/main/java/com/weatherapp/di/DatabaseModule.kt`, add:
    ```kotlin
    @Provides
    fun provideAlertStateDao(db: AppDatabase): AlertStateDao = db.alertStateDao()
    ```

- [ ] Task 7: Define the `ConfirmedForecastSnapshot` serialization format (AC: 2)
  - [ ] 7.1 The `confirmedForecastSnapshot` column stores the threshold values at the time of CONFIRMED_CLEAR confirmation
  - [ ] 7.2 Use a simple JSON-like string format: `"{\"precipProb\":0.15,\"windKmh\":18.0,\"windowStart\":1741435200,\"windowEnd\":1741453200}"`
  - [ ] 7.3 Create `data class ForecastSnapshot(val precipProb: Double, val windKmh: Double, val windowStart: Long, val windowEnd: Long)` in `model/` package
  - [ ] 7.4 Use Gson to serialize/deserialize: `Gson().toJson(snapshot)` / `Gson().fromJson(json, ForecastSnapshot::class.java)`
  - [ ] 7.5 This snapshot is compared against fresh forecast data in `AlertEvaluationWorker` (Story 2.2) to detect material changes

- [ ] Task 8: Write instrumented tests for `AlertStateDao` (AC: 3, 4)
  - [ ] 8.1 Create `app/src/androidTest/java/com/weatherapp/db/AlertStateDaoTest.kt`
  - [ ] 8.2 Test `insertRecord` with a new `eventId` → row exists in DB
  - [ ] 8.3 Test `insertRecord` with existing `eventId` → row is replaced (not duplicated) — this is the append-only semantic via REPLACE
  - [ ] 8.4 Test `getByEventId` returns correct record after insert
  - [ ] 8.5 Test `getByEventId` returns null for unknown eventId
  - [ ] 8.6 Test `resolveExpired` sets state to RESOLVED for records with `lastTransitionAt` before threshold
  - [ ] 8.7 Test `TypeConverter` round-trips all 4 `AlertState` enum values correctly

## Dev Notes

### Critical Architecture Rules for This Story

**1. AlertState Enum — Exact Values**

From architecture.md:
```kotlin
enum class AlertState { UNCHECKED, CONFIRMED_CLEAR, ALERT_SENT, RESOLVED }
```

The state machine transitions:
```
UNCHECKED
  → CONFIRMED_CLEAR   (all-clear threshold met; confirmation notification sent)
  → ALERT_SENT        (material change from CONFIRMED_CLEAR: ≥20% precip shift OR wind >25mph)
  → CONFIRMED_CLEAR   (conditions improved back to clear — re-confirmation)
  → RESOLVED          (event end time passed; record archived)
```

**2. Append-Only Insert Semantics**

The architecture specifies: "Transitions are append-only in Room — never update a row in place; insert a new record with updated state + `lastTransitionAt`."

For v1 with a single primary key per `eventId`, this means every state transition does an `INSERT OR REPLACE` (which overwrites the existing row). The "append-only" constraint means:
- No UPDATE SQL statements in business logic code
- No partial updates (e.g., updating only the `state` column)
- Always construct a new `AlertStateRecord` object with all fields set and INSERT it

```kotlin
// CORRECT — state transition
val newRecord = AlertStateRecord(
    eventId = existing.eventId,
    state = AlertState.CONFIRMED_CLEAR,
    confirmedForecastSnapshot = Gson().toJson(snapshot),
    lastTransitionAt = System.currentTimeMillis() / 1000L
)
alertStateDao.insertRecord(newRecord) // @Insert(onConflict = REPLACE)

// WRONG — never do this
alertStateDao.updateState(eventId, AlertState.CONFIRMED_CLEAR) // no such DAO method
```

**3. Room Entity — Exact Column Names**

```kotlin
// app/src/main/java/com/weatherapp/data/db/entity/AlertStateRecord.kt
@Entity(tableName = "alert_state_record")
data class AlertStateRecord(
    @PrimaryKey
    @ColumnInfo(name = "event_id")
    val eventId: String,

    @ColumnInfo(name = "state")
    val state: AlertState,

    @ColumnInfo(name = "confirmed_forecast_snapshot")
    val confirmedForecastSnapshot: String,

    @ColumnInfo(name = "last_transition_at")
    val lastTransitionAt: Long
)
```

**4. TypeConverter Registration**

```kotlin
// AppDatabase.kt — must add annotation
@Database(
    entities = [ForecastHour::class, AlertStateRecord::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(AlertStateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun forecastDao(): ForecastDao
    abstract fun alertStateDao(): AlertStateDao
    // ...
}
```

**5. ForecastSnapshot Model**

```kotlin
// app/src/main/java/com/weatherapp/model/ForecastSnapshot.kt
package com.weatherapp.model

data class ForecastSnapshot(
    val precipProb: Double,      // e.g., 0.15 (15%)
    val windKmh: Double,         // e.g., 18.0 km/h
    val windowStart: Long,       // epoch seconds — start of the event window
    val windowEnd: Long          // epoch seconds — end of the event window
)
```

Serialized as JSON string to `confirmedForecastSnapshot` column. Gson is already included as a transitive dependency of Retrofit; no additional dependency needed.

**6. Free Tier Event ID**

For free tier (location-based, not calendar-event-based), the `eventId` is constructed as `"location:{latGrid}:{lonGrid}:{date}"` (e.g., `"location:37.8:-122.4:2026-03-08"`). This is not the same as the premium per-calendar-event key introduced in Story 3.4.

**7. Alert Threshold Constants**

Define these constants in a companion object within `AlertEvaluationWorker` or a `AlertThresholds` object in `model/`:

```kotlin
object AlertThresholds {
    const val MATERIAL_PRECIP_CHANGE = 0.20   // 20% shift in precipitation probability
    const val WIND_SPEED_ALERT_KMH = 40.0     // ~25 mph converted to km/h
    const val ALL_CLEAR_PRECIP_MAX = 0.20     // Below 20% = potential all-clear
    const val ALL_CLEAR_WIND_MAX_KMH = 30.0   // Below 30 km/h = all-clear for wind
}
```

Note: the architecture doc specifies "wind speed crossing 25 mph" — 25 mph ≈ 40.2 km/h. Use 40.0 km/h as the threshold since the Worker API returns `wind_speed_kmh`.

**8. Database Migration — Required**

This story MUST add a Room migration since `AppDatabase` version increases from 1 to 2. The migration SQL must exactly match the entity definition:

```kotlin
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
```

**9. Anti-Patterns to Avoid**

- NEVER do `UPDATE alert_state_record SET state = ...` in business logic — always INSERT with REPLACE
- NEVER store `AlertState` as an integer — store as its name string via TypeConverter
- NEVER skip the database migration — always provide `addMigrations()` when version is incremented
- NEVER use `Log.*` — use `Timber.d/i/w/e()`
- NEVER query `AlertStateDao` from a Glance composable or ViewModel directly — only from Workers and Repositories

### Project Structure Notes

Files to create in this story:
```
app/src/main/java/com/weatherapp/
  model/
    AlertState.kt                   ← NEW: enum class (4 states)
    ForecastSnapshot.kt             ← NEW: data class for snapshot serialization
  data/db/
    entity/
      AlertStateRecord.kt           ← NEW: alert_state_record table entity
    dao/
      AlertStateDao.kt              ← NEW: insert, getByEventId, resolveExpired
    AlertStateConverters.kt         ← NEW: Room TypeConverter for AlertState enum

app/src/androidTest/java/com/weatherapp/
  db/
    AlertStateDaoTest.kt            ← NEW
```

Files to modify in this story:
```
app/src/main/java/com/weatherapp/
  data/db/
    AppDatabase.kt                  ← MODIFY: add AlertStateRecord entity, version 2, migration, TypeConverters
  di/
    DatabaseModule.kt               ← MODIFY: expose AlertStateDao via @Provides
```

### Cross-Story Dependencies

- **Depends on Story 1.3**: `AppDatabase` version 1, `DatabaseModule.kt` Hilt wiring pattern
- **Provides to Story 2.2**: `AlertStateDao` for reading/writing state machine records; `AlertState` enum for transition logic; `ForecastSnapshot` for confirmed state comparison
- **Provides to Story 3.4**: Same `AlertStateDao` used for premium per-event tracking (with different `eventId` format)

### References

- [Source: architecture.md#API & Communication Patterns] — Alert State Machine diagram and transitions
- [Source: architecture.md#Naming Patterns] — `snake_case` Room table/column names
- [Source: architecture.md#Format Patterns] — Alert State Enum definition
- [Source: architecture.md#Enforcement Guidelines] — Rule 6: `snake_case` for Room columns
- [Source: epics.md#Story 2.1] — All acceptance criteria

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

### Completion Notes List

### File List
