# Story 3.2: Calendar Data Layer

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a developer,
I want `CalendarRepository` to safely query Android's CalendarContract and surface upcoming events as domain models,
so that the calendar scan worker has reliable, sanitised event data to work with even when calendar data is malformed or permission is revoked mid-session.

## Acceptance Criteria

**AC-1:** Given `CalendarRepository.getUpcomingEvents(daysAhead: Int)` when called with `READ_CALENDAR` permission granted then it returns a `List<CalendarEvent>` covering the next `daysAhead` days, each with `eventId`, `title`, `startEpoch`, `endEpoch`, `location` (nullable).

**AC-2:** Given `CalendarRepository.getUpcomingEvents()` when the `CalendarContract` query returns a row with a non-ASCII or malformed event title then the title is sanitised to a valid UTF-8 string; the event is included in results; the worker does not stall or crash.

**AC-3:** Given `CalendarRepository.getUpcomingEvents()` when `READ_CALENDAR` permission is revoked between the permission check and the cursor query then the `SecurityException` is caught, a `Timber.w` log is emitted, and an empty list is returned — no crash, no propagated exception.

**AC-4:** Given any `CalendarContract` query when reviewed then it is wrapped in `try { ... } catch (e: SecurityException) { ... } catch (e: Exception) { ... }` — no unguarded query exists anywhere in `CalendarRepository`.

**AC-5:** Given `CalendarEvent.kt` domain model when reviewed then it contains `eventId: String`, `title: String`, `startEpoch: Long`, `endEpoch: Long`, `location: String?`.

**AC-6:** Given `CalendarEventForecast.kt` (Room entity) when reviewed then it maps to table `calendar_event_forecast` with `event_id` as primary key, storing the last weather snapshot and widget display string for that event.

**AC-7:** Given `CalendarEventForecastDao` when called then it supports `upsert(CalendarEventForecast)`, `getByEventId(eventId): CalendarEventForecast?`, and `deleteExpired(beforeEpoch)`.

## Tasks / Subtasks

- [ ] Task 1: Create `CalendarEvent.kt` domain model (AC: 5)
  - [ ] 1.1 Create `app/src/main/java/com/weatherapp/data/calendar/CalendarEvent.kt`
  - [ ] 1.2 Define: `data class CalendarEvent(val eventId: String, val title: String, val startEpoch: Long, val endEpoch: Long, val location: String?)`
  - [ ] 1.3 This is a pure domain model — no Room annotations, no Android dependencies

- [ ] Task 2: Create `CalendarRepository.kt` with full safety wrapping (AC: 1–4)
  - [ ] 2.1 Create `app/src/main/java/com/weatherapp/data/calendar/CalendarRepository.kt`
  - [ ] 2.2 Inject `@ApplicationContext context: Context` via Hilt constructor injection
  - [ ] 2.3 Implement `fun getUpcomingEvents(daysAhead: Int = 7): List<CalendarEvent>`:
    - Check `READ_CALENDAR` permission via `ContextCompat.checkSelfPermission()` BEFORE querying
    - If permission not granted: `Timber.d("READ_CALENDAR not granted — returning empty list")`, return `emptyList()`
    - Execute `contentResolver.query(CalendarContract.Events.CONTENT_URI, ...)` inside full try/catch
    - Process cursor: for each row, call `sanitizeTitle(rawTitle)`
    - Catch `SecurityException`: `Timber.w(e, "READ_CALENDAR revoked mid-session")`, return `emptyList()`
    - Catch `Exception`: `Timber.e(e, "Unexpected CalendarContract failure")`, return `emptyList()`
  - [ ] 2.4 Query projection: `arrayOf(CalendarContract.Events._ID, CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.EVENT_LOCATION)`
  - [ ] 2.5 Query selection: `"${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"` — filter to upcoming events within `daysAhead`
  - [ ] 2.6 Selection args: `arrayOf(nowEpoch.toString(), (nowEpoch + daysAhead * 86400L * 1000L).toString())` — CalendarContract uses milliseconds, not seconds

- [ ] Task 3: Implement title sanitization (AC: 2)
  - [ ] 3.1 Create private function `fun sanitizeTitle(rawTitle: String?): String`:
    - If null → return `"Untitled Event"`
    - Strip non-UTF-8 characters: replace bytes that cannot be decoded as UTF-8 with replacement character or remove
    - Limit length to 100 characters (safety cap)
    - Trim leading/trailing whitespace
    - If resulting string is blank → return `"Untitled Event"`
  - [ ] 3.2 Implementation:
    ```kotlin
    private fun sanitizeTitle(rawTitle: String?): String {
        if (rawTitle.isNullOrBlank()) return "Untitled Event"
        // Convert to bytes and back to remove invalid sequences
        val sanitized = rawTitle
            .toByteArray(Charsets.UTF_8)
            .toString(Charsets.UTF_8)
            .filter { it.isLetterOrDigit() || it.isWhitespace() || it in ".,!?-:()&'\"" }
            .trim()
            .take(100)
        return sanitized.ifBlank { "Untitled Event" }
    }
    ```
  - [ ] 3.3 Note: the filter function above is a conservative whitelist. If product requires emoji support, expand the filter to include Unicode letters. For MVP, this is sufficient.

- [ ] Task 4: Create `CalendarEventForecast.kt` Room entity (AC: 6)
  - [ ] 4.1 Create `app/src/main/java/com/weatherapp/data/db/entity/CalendarEventForecast.kt`
  - [ ] 4.2 Annotate with `@Entity(tableName = "calendar_event_forecast")`
  - [ ] 4.3 Define:
    ```kotlin
    @Entity(tableName = "calendar_event_forecast")
    data class CalendarEventForecast(
        @PrimaryKey
        @ColumnInfo(name = "event_id")
        val eventId: String,

        @ColumnInfo(name = "last_weather_snapshot")
        val lastWeatherSnapshot: String,       // JSON: precipProb, windKmh, etc.

        @ColumnInfo(name = "widget_display_string")
        val widgetDisplayString: String,       // e.g., "Your BBQ (12pm) is clear."

        @ColumnInfo(name = "last_updated_epoch")
        val lastUpdatedEpoch: Long,

        @ColumnInfo(name = "event_start_epoch")
        val eventStartEpoch: Long              // for deleteExpired() queries
    )
    ```

- [ ] Task 5: Create `CalendarEventForecastDao.kt` (AC: 7)
  - [ ] 5.1 Create `app/src/main/java/com/weatherapp/data/db/dao/CalendarEventForecastDao.kt`
  - [ ] 5.2 Implement `@Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(record: CalendarEventForecast)`
  - [ ] 5.3 Implement `@Query("SELECT * FROM calendar_event_forecast WHERE event_id = :eventId") suspend fun getByEventId(eventId: String): CalendarEventForecast?`
  - [ ] 5.4 Implement `@Query("DELETE FROM calendar_event_forecast WHERE event_start_epoch < :beforeEpoch") suspend fun deleteExpired(beforeEpoch: Long)`

- [ ] Task 6: Update `AppDatabase` to include `CalendarEventForecast` (AC: 6, 7)
  - [ ] 6.1 Update `app/src/main/java/com/weatherapp/data/db/AppDatabase.kt`
  - [ ] 6.2 Add `CalendarEventForecast::class` to the entities array
  - [ ] 6.3 Increment database version from 2 to 3
  - [ ] 6.4 Add migration `MIGRATION_2_3`:
    ```kotlin
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `calendar_event_forecast` (
                    `event_id` TEXT NOT NULL,
                    `last_weather_snapshot` TEXT NOT NULL,
                    `widget_display_string` TEXT NOT NULL,
                    `last_updated_epoch` INTEGER NOT NULL,
                    `event_start_epoch` INTEGER NOT NULL,
                    PRIMARY KEY(`event_id`)
                )
            """.trimIndent())
        }
    }
    ```
  - [ ] 6.5 Add `.addMigrations(MIGRATION_2_3)` to the `databaseBuilder` call

- [ ] Task 7: Expose `CalendarEventForecastDao` from `DatabaseModule` (AC: 7)
  - [ ] 7.1 Add to `DatabaseModule.kt`:
    ```kotlin
    @Provides
    fun provideCalendarEventForecastDao(db: AppDatabase): CalendarEventForecastDao =
        db.calendarEventForecastDao()
    ```
  - [ ] 7.2 Add `abstract fun calendarEventForecastDao(): CalendarEventForecastDao` to `AppDatabase`

- [ ] Task 8: Create Hilt binding for `CalendarRepository` (AC: 1)
  - [ ] 8.1 Add to `AppModule.kt`:
    ```kotlin
    @Provides @Singleton
    fun provideCalendarRepository(@ApplicationContext context: Context): CalendarRepository =
        CalendarRepository(context)
    ```

- [ ] Task 9: Write unit tests for `CalendarRepository` (AC: 1–4)
  - [ ] 9.1 Create `app/src/test/java/com/weatherapp/data/calendar/CalendarRepositoryTest.kt`
  - [ ] 9.2 Test: `READ_CALENDAR` not granted → returns `emptyList()` without crash
  - [ ] 9.3 Test: `SecurityException` thrown during query → returns `emptyList()`, `Timber.w` logged
  - [ ] 9.4 Test: `sanitizeTitle(null)` → `"Untitled Event"`
  - [ ] 9.5 Test: `sanitizeTitle("")` → `"Untitled Event"`
  - [ ] 9.6 Test: `sanitizeTitle("My BBQ 🔥")` → sanitized string without crashing
  - [ ] 9.7 Test: title longer than 100 chars → truncated to 100 chars

- [ ] Task 10: Write instrumented tests for `CalendarEventForecastDao` (AC: 7)
  - [ ] 10.1 Create `app/src/androidTest/java/com/weatherapp/db/CalendarEventForecastDaoTest.kt`
  - [ ] 10.2 Test `upsert` new record → `getByEventId` returns it
  - [ ] 10.3 Test `upsert` existing eventId → record is updated (REPLACE semantics)
  - [ ] 10.4 Test `deleteExpired` removes records with `eventStartEpoch < beforeEpoch`

## Dev Notes

### Critical Architecture Rules for This Story

**1. Every CalendarContract Query Must Be Wrapped — No Exceptions**

From architecture.md (mandatory pattern):

```kotlin
// CalendarRepository.kt — every query must follow this pattern
fun getUpcomingEvents(daysAhead: Int = 7): List<CalendarEvent> {
    // Permission check FIRST
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Timber.d("READ_CALENDAR not granted — returning empty list")
        return emptyList()
    }

    return try {
        val nowMs = System.currentTimeMillis()
        val endMs = nowMs + daysAhead.toLong() * 86400L * 1000L

        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION
        )
        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
        val selectionArgs = arrayOf(nowMs.toString(), endMs.toString())

        context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${CalendarContract.Events.DTSTART} ASC"
        )?.use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val eventId = cursor.getLong(0).toString()
                    val rawTitle = cursor.getString(1)
                    val startMs = cursor.getLong(2)
                    val endMs = cursor.getLong(3)
                    val location = cursor.getString(4)

                    add(CalendarEvent(
                        eventId = eventId,
                        title = sanitizeTitle(rawTitle),
                        startEpoch = startMs / 1000L, // Convert ms to seconds
                        endEpoch = endMs / 1000L,
                        location = location?.takeIf { it.isNotBlank() }
                    ))
                }
            }
        } ?: emptyList()

    } catch (e: SecurityException) {
        Timber.w(e, "READ_CALENDAR permission revoked mid-session")
        emptyList()
    } catch (e: Exception) {
        Timber.e(e, "Unexpected CalendarContract failure")
        emptyList()
    }
}
```

**2. CalendarContract Timestamps — Milliseconds**

CalendarContract stores timestamps in **milliseconds**, not seconds. Convert:
```kotlin
val startEpochSeconds = cursor.getLong(startIndex) / 1000L
val endEpochSeconds = cursor.getLong(endIndex) / 1000L
```

The rest of the app uses epoch seconds. This conversion must happen in `CalendarRepository`.

**3. CalendarEvent Domain Model — Pure Kotlin**

No Room annotations on `CalendarEvent`. It is a pure domain model passed between `CalendarRepository` and `CalendarScanWorker`. The Room entity is `CalendarEventForecast` which stores the computed result.

**4. Outdoor Event Keyword Signals**

While not a responsibility of `CalendarRepository`, the following keywords are used by `CalendarScanWorker` (Story 3.3) to identify outdoor-potential events. Document here for clarity:

```kotlin
// In CalendarScanWorker — outdoor event detection
val OUTDOOR_KEYWORDS = setOf(
    "bbq", "barbecue", "run", "match", "picnic", "game", "walk",
    "hike", "cycling", "cycle", "outdoor", "garden", "park",
    "football", "cricket", "tennis", "marathon", "race",
    "wedding", "graduation", "festival", "concert", "fair"
)

fun isOutdoorPotential(event: CalendarEvent): Boolean {
    val titleLower = event.title.lowercase()
    val durationMinutes = (event.endEpoch - event.startEpoch) / 60
    return OUTDOOR_KEYWORDS.any { keyword -> titleLower.contains(keyword) } &&
           durationMinutes >= 30
}
```

**5. AppDatabase Version 3 — CalendarEventForecast Migration**

The database version sequence:
- Version 1 (Story 1.3): `forecast_hour` table
- Version 2 (Story 2.1): + `alert_state_record` table
- Version 3 (This story): + `calendar_event_forecast` table

```kotlin
@Database(
    entities = [ForecastHour::class, AlertStateRecord::class, CalendarEventForecast::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(AlertStateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun forecastDao(): ForecastDao
    abstract fun alertStateDao(): AlertStateDao
    abstract fun calendarEventForecastDao(): CalendarEventForecastDao

    companion object {
        // ... MIGRATION_1_2, MIGRATION_2_3 ...
        fun getInstance(context: Context): AppDatabase =
            // ... Room.databaseBuilder(...).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
    }
}
```

**6. widget_display_string — Pre-formatted by Worker**

The `widgetDisplayString` column stores a display-ready string written by `CalendarScanWorker`. The Glance widget reads this via DataStore (not Room) — `CalendarScanWorker` writes the string to `DataStore[KEY_WIDGET_VERDICT]` after computing it from `CalendarEventForecast` data.

The Room entity is used for persistence across worker runs; DataStore is the widget's read surface.

**7. Anti-Patterns to Avoid**

- NEVER query CalendarContract without try/catch(SecurityException)
- NEVER pass milliseconds to the rest of the app — always convert to epoch seconds in CalendarRepository
- NEVER call `CalendarRepository` from a ViewModel or Glance composable — only from Workers
- NEVER assume CalendarContract returns valid UTF-8 — always sanitize titles
- NEVER use `Log.*` — use `Timber.d/i/w/e()`

### Project Structure Notes

Files to create in this story:
```
app/src/main/java/com/weatherapp/
  data/calendar/
    CalendarRepository.kt             ← NEW: CalendarContract queries with full safety wrapping
    CalendarEvent.kt                  ← NEW: domain model (pure Kotlin, no Room annotations)
  data/db/
    entity/
      CalendarEventForecast.kt        ← NEW: calendar_event_forecast table entity
    dao/
      CalendarEventForecastDao.kt     ← NEW: upsert, getByEventId, deleteExpired

app/src/test/java/com/weatherapp/
  data/calendar/
    CalendarRepositoryTest.kt         ← NEW

app/src/androidTest/java/com/weatherapp/
  db/
    CalendarEventForecastDaoTest.kt   ← NEW
```

Files to modify in this story:
```
app/src/main/java/com/weatherapp/
  data/db/
    AppDatabase.kt                    ← MODIFY: add CalendarEventForecast, version 3, MIGRATION_2_3
  di/
    AppModule.kt                      ← MODIFY: add CalendarRepository provider
    DatabaseModule.kt                 ← MODIFY: add CalendarEventForecastDao provider
```

### Cross-Story Dependencies

- **Depends on Story 2.1**: `AppDatabase` version 2; migration pattern established
- **Depends on Story 3.1**: `KEY_IS_PREMIUM` gating ensures `CalendarScanWorker` (which calls `CalendarRepository`) only runs for premium users
- **Provides to Story 3.3**: `CalendarRepository.getUpcomingEvents()` provides the event list to `CalendarScanWorker`; `CalendarEventForecastDao` provides storage for weather-per-event results
- **Provides to Story 3.4**: `CalendarEvent.eventId` used as the `AlertStateRecord.eventId` for per-event alert state machine

### References

- [Source: architecture.md#CalendarContract Safety] — Exact wrapping pattern with SecurityException and general Exception
- [Source: architecture.md#Permission Boundary] — CalendarRepository owns all READ_CALENDAR checks
- [Source: architecture.md#Naming Patterns] — `snake_case` Room table/column names
- [Source: epics.md#Story 3.2] — All acceptance criteria including FR-014 malformed data safety

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

### Completion Notes List

### File List
