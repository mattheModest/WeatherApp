# Story 3.3: Calendar Scan Worker & Proactive Widget

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a premium user,
I want the widget to shift to show event-specific weather before I even think to check,
so that I experience the "how did it know?" moment that makes the premium tier feel genuinely intelligent.

## Acceptance Criteria

**AC-1:** Given `DataStore[KEY_IS_PREMIUM]` is `true` when `ForecastRefreshWorker` completes successfully then it enqueues `CalendarScanWorker` as a chained one-time request.

**AC-2:** Given `CalendarScanWorker` runs when `CalendarRepository.getUpcomingEvents(7)` returns events then it identifies events with outdoor potential using title keyword signals (e.g., "BBQ", "run", "match", "picnic", "game", "walk") and event duration >= 30 minutes.

**AC-2b:** Given a calendar event title matches an outdoor keyword but the event duration is < 30 minutes when `CalendarScanWorker` evaluates it then the event is ignored and not treated as an outdoor-potential event — keyword match alone is insufficient without the duration requirement.

**AC-3:** Given a single upcoming outdoor-potential event is identified when `CalendarScanWorker` completes then `DataStore[KEY_WIDGET_VERDICT]` is overwritten with an event-specific string (e.g., "Your BBQ (12pm) is clear.") and the widget updates to display it.

**AC-4:** Given two or more outdoor-potential events overlap in time when `CalendarScanWorker` detects the conflict then `DataStore[KEY_WIDGET_VERDICT]` is set to a conflict message (e.g., "2 outdoor events at 3pm - Light rain - Check both.") and the worker never silently picks one event.

**AC-5:** Given a calendar event has a non-home `location` field when `CalendarScanWorker` processes it then it passes that location's coordinates (snapped via `snapToGrid()`) to `WeatherRepository` to pre-fetch weather for that location; the result is written to `CalendarEventForecast` in Room.

**AC-6:** Given a calendar event location pre-fetch when the coordinates are constructed then `snapToGrid()` is applied before any network call — raw event location coordinates never reach `WeatherApi`.

**AC-7:** Given a user modifies or removes a calendar event when the next `CalendarScanWorker` cycle runs (within 30 minutes via the chained WorkManager schedule) then `DataStore[KEY_WIDGET_VERDICT]` reflects the updated event state and the widget re-renders accordingly.

**AC-8:** Given `CalendarScanWorker` completes when reviewed then it calls `WeatherWidget.update()` after writing to DataStore, ensuring the widget reflects the latest state without waiting for the next Glance refresh cycle.

## Tasks / Subtasks

- [ ] Task 1: Create `CalendarScanWorker.kt` skeleton with Hilt injection (AC: 1, 2)
  - [ ] 1.1 Create `app/src/main/java/com/weatherapp/worker/CalendarScanWorker.kt`
  - [ ] 1.2 Annotate with `@HiltWorker`, extend `CoroutineWorker`
  - [ ] 1.3 Inject via `@AssistedInject`: `CalendarRepository`, `WeatherRepository`, `CalendarEventForecastDao`, `DataStore<Preferences>`, `WorkManager`, `@ApplicationContext context: Context`
  - [ ] 1.4 In `doWork()`: check `KEY_IS_PREMIUM` from DataStore first (defense in depth — ForecastRefreshWorker already checks, but this protects against direct enqueue):
    ```kotlin
    val isPremium = dataStore.data.first()[PreferenceKeys.KEY_IS_PREMIUM] ?: false
    if (!isPremium) {
        Timber.w("CalendarScanWorker triggered for non-premium user — skipping")
        return Result.success()
    }
    ```

- [ ] Task 2: Implement outdoor event identification (AC: 2)
  - [ ] 2.1 Define `OUTDOOR_KEYWORDS` set in `CalendarScanWorker` companion object:
    ```kotlin
    private val OUTDOOR_KEYWORDS = setOf(
        "bbq", "barbecue", "run", "match", "picnic", "game", "walk",
        "hike", "cycling", "cycle", "outdoor", "garden", "park",
        "football", "cricket", "tennis", "marathon", "race",
        "wedding", "graduation", "festival", "concert", "fair"
    )
    ```
  - [ ] 2.2 Implement `fun isOutdoorPotential(event: CalendarEvent): Boolean`:
    - Title keyword match: `event.title.lowercase()` contains any keyword in `OUTDOOR_KEYWORDS`
    - Duration requirement: `(event.endEpoch - event.startEpoch) / 60 >= 30` (at least 30 minutes)
  - [ ] 2.3 Filter `CalendarRepository.getUpcomingEvents(7)` with `isOutdoorPotential()`
  - [ ] 2.4 Focus on events starting within the next 24 hours for widget display; scan 7 days for `AlertEvaluationWorker` purposes

- [ ] Task 3: Implement conflict detection (AC: 4)
  - [ ] 3.1 Implement `fun detectConflicts(events: List<CalendarEvent>): List<List<CalendarEvent>>` — groups overlapping events
  - [ ] 3.2 Two events overlap if: `event1.startEpoch < event2.endEpoch && event2.startEpoch < event1.endEpoch`
  - [ ] 3.3 Return groups of 2+ overlapping events; non-overlapping events return as single-event groups
  - [ ] 3.4 For widget display: if ANY group has 2+ events → conflict mode
  - [ ] 3.5 Conflict message format: `"2 outdoor events at 3pm - Light rain - Check both."` (where "3pm" is the start of the earliest conflicting event and "Light rain" is the condition from the forecast)

- [ ] Task 4: Implement event-specific widget string generation (AC: 3, 4)
  - [ ] 4.1 For single outdoor event: `"Your ${event.title} (${formatHour(event.startEpoch)}) is clear."` or `"Your ${event.title} (${formatHour(event.startEpoch)}) — light rain expected."`
  - [ ] 4.2 Weather condition for the event: query forecast from `ForecastDao.queryByTimeWindow(event.startEpoch, event.endEpoch)` and apply simple classification
  - [ ] 4.3 For conflict: `"${conflictCount} outdoor events at ${formatHour(earliestStart)} · ${weatherSummary} · Check both."`
  - [ ] 4.4 Write result to `DataStore[KEY_WIDGET_VERDICT]` — this overwrites the general daily verdict for premium users
  - [ ] 4.5 Also write to `DataStore[KEY_ALL_CLEAR]` appropriately based on event weather

- [ ] Task 5: Implement travel pre-fetch for non-home event locations (AC: 5, 6)
  - [ ] 5.1 For each outdoor-potential event where `event.location != null`:
    - Attempt geocoding of `event.location` string to lat/lon coordinates
    - For MVP v1: use a simple city-name lookup map (same approach as LocationRepository manual location)
    - Apply `snapToGrid()` BEFORE any network call — raw event coordinates never reach `WeatherApi`
    - Call `WeatherRepository.fetchForecast(snappedLat, snappedLon, date)` for the event's date
    - Write result to `CalendarEventForecastDao.upsert(CalendarEventForecast(...))`
  - [ ] 5.2 If geocoding fails: log `Timber.w("Could not geocode event location: ${event.location}")` and skip pre-fetch for that event; do NOT crash
  - [ ] 5.3 Home location detection: if `event.location` geocodes to coordinates within 0.1° of the snapped home location → skip (already have weather for home)

- [ ] Task 6: Implement `WeatherWidget.update()` call at end of `CalendarScanWorker` (AC: 8)
  - [ ] 6.1 After all DataStore writes are complete, call `WeatherWidget.update(applicationContext)`
  - [ ] 6.2 This forces Glance to re-read DataStore and re-render the widget immediately
  - [ ] 6.3 If `WeatherWidget.update()` throws: log `Timber.e(e, "Failed to trigger widget update")` and return `Result.success()` — the widget will update on its next natural cycle

- [ ] Task 7: Verify 30-minute update cycle for calendar changes (AC: 7)
  - [ ] 7.1 `CalendarScanWorker` is already chained after `ForecastRefreshWorker` (which runs every 30 minutes)
  - [ ] 7.2 This means calendar changes are reflected within ≤ 30 minutes via the existing periodic work
  - [ ] 7.3 No additional scheduling mechanism is needed — document this in the implementation

- [ ] Task 8: Handle `CalendarEventForecastDao.deleteExpired()` cleanup (AC: 7)
  - [ ] 8.1 At the start of `CalendarScanWorker.doWork()`, call `calendarEventForecastDao.deleteExpired(nowEpoch)`
  - [ ] 8.2 This removes entries for events that have already started (past events) — `event_start_epoch < nowEpoch`
  - [ ] 8.3 After deletion, fresh data is written for current/upcoming events

- [ ] Task 9: Write unit tests for `CalendarScanWorker` logic (AC: 2, 3, 4)
  - [ ] 9.1 Create `app/src/test/java/com/weatherapp/worker/CalendarScanWorkerTest.kt`
  - [ ] 9.2 Test: `isOutdoorPotential()` with "BBQ at the park" (30+ min) → true
  - [ ] 9.3 Test: `isOutdoorPotential()` with "Doctor appointment" (60 min) → false (no keyword)
  - [ ] 9.4 Test: `isOutdoorPotential()` with "run" (15 min) → false (too short)
  - [ ] 9.5 Test: single outdoor event → `KEY_WIDGET_VERDICT` = event-specific string
  - [ ] 9.6 Test: two overlapping outdoor events → conflict message (not single event message)
  - [ ] 9.7 Test: `isPremium = false` → worker exits immediately without querying calendar
  - [ ] 9.8 Test: `detectConflicts()` correctly identifies overlapping events

## Dev Notes

### Critical Architecture Rules for This Story

**1. isPremium Check — First in doWork()**

Even though `ForecastRefreshWorker` gates the enqueue with `isPremium`, `CalendarScanWorker` must also check at the start of its own `doWork()`. This protects against:
- Direct test invocations
- Future code paths that might enqueue without the gate
- Defense-in-depth principle

```kotlin
override suspend fun doWork(): Result {
    val isPremium = dataStore.data.first()[PreferenceKeys.KEY_IS_PREMIUM] ?: false
    if (!isPremium) {
        Timber.w("CalendarScanWorker: not premium — exiting")
        return Result.success()
    }
    // ... rest of worker
}
```

**2. Coordinate Snapping for Event Locations — Mandatory**

```kotlin
// CORRECT — event location pre-fetch
val (rawLat, rawLon) = geocodeLocation(event.location) ?: return@forEachEvent
val snappedLat = rawLat.snapToGrid()
val snappedLon = rawLon.snapToGrid()
weatherRepository.fetchForecast(snappedLat, snappedLon, eventDate)

// WRONG — never do this
weatherRepository.fetchForecast(rawLat, rawLon, eventDate) // ❌ raw coords
```

**3. Widget Verdict Write for Premium Events**

The premium widget verdict overwrites the free-tier verdict in DataStore. This is intentional:

```kotlin
// CalendarScanWorker — after determining event weather
dataStore.edit { prefs ->
    prefs[PreferenceKeys.KEY_WIDGET_VERDICT] = eventVerdictString
    // Keep KEY_MOOD_LINE, KEY_BRING_LIST etc. from ForecastRefreshWorker
    // Only KEY_WIDGET_VERDICT is overwritten for premium event display
}
```

The free-tier verdict is computed by `ForecastRefreshWorker` and written first. `CalendarScanWorker` runs second and overwrites only `KEY_WIDGET_VERDICT` with the premium event-specific string.

**4. Event Verdict String Format**

```kotlin
private fun buildEventVerdictString(
    event: CalendarEvent,
    hours: List<ForecastHour>
): String {
    val timeLabel = formatHour(event.startEpoch)
    val weatherDesc = when {
        hours.any { it.precipitationProbability >= 0.40 } -> "Rain expected"
        hours.any { it.windSpeedKmh >= 40 } -> "Windy"
        else -> "is clear"
    }
    return "Your ${event.title} ($timeLabel) $weatherDesc."
}

// Single event, clear: "Your BBQ (12pm) is clear."
// Single event, rain: "Your BBQ (12pm) Rain expected."
// Conflict: "2 outdoor events at 3pm · Light rain · Check both."
```

**5. Conflict Detection Logic**

```kotlin
private fun detectConflicts(events: List<CalendarEvent>): Boolean {
    for (i in events.indices) {
        for (j in i + 1 until events.size) {
            val a = events[i]
            val b = events[j]
            if (a.startEpoch < b.endEpoch && b.startEpoch < a.endEpoch) {
                return true // Overlap found
            }
        }
    }
    return false
}

private fun buildConflictVerdictString(
    events: List<CalendarEvent>,
    hours: List<ForecastHour>
): String {
    val count = events.size
    val earliestStart = events.minOf { it.startEpoch }
    val timeLabel = formatHour(earliestStart)
    val weatherDesc = if (hours.any { it.precipitationProbability >= 0.30 }) "Light rain" else "Mixed"
    return "$count outdoor events at $timeLabel · $weatherDesc · Check both."
}
```

**6. CalendarEventForecast — Upsert Pattern**

For each processed event, store the weather snapshot:

```kotlin
calendarEventForecastDao.upsert(
    CalendarEventForecast(
        eventId = event.eventId,
        lastWeatherSnapshot = Gson().toJson(
            ForecastSnapshot(
                precipProb = hours.maxOf { it.precipitationProbability },
                windKmh = hours.maxOf { it.windSpeedKmh },
                windowStart = event.startEpoch,
                windowEnd = event.endEpoch
            )
        ),
        widgetDisplayString = verdictString,
        lastUpdatedEpoch = nowEpoch,
        eventStartEpoch = event.startEpoch
    )
)
```

This record is later read by `AlertEvaluationWorker` (Story 3.4) to compare against fresh forecast data.

**7. formatHour Helper**

```kotlin
private fun formatHour(epochSeconds: Long): String {
    val localHour = Instant.ofEpochSecond(epochSeconds)
        .atZone(ZoneId.systemDefault()).hour
    return when {
        localHour == 0  -> "12am"
        localHour < 12  -> "${localHour}am"
        localHour == 12 -> "12pm"
        else            -> "${localHour - 12}pm"
    }
}
```

**8. Anti-Patterns to Avoid**

- NEVER skip `snapToGrid()` when geocoding event locations
- NEVER silently pick one event when multiple outdoor events conflict — always show conflict message
- NEVER call `CalendarRepository` from a ViewModel or Glance composable
- NEVER forget to call `WeatherWidget.update()` after DataStore writes
- NEVER use `Log.*` — use `Timber.d/i/w/e()`
- NEVER let `CalendarScanWorker` crash on geocoding failure — log and continue

### Project Structure Notes

Files to create in this story:
```
app/src/main/java/com/weatherapp/
  worker/
    CalendarScanWorker.kt            ← NEW: outdoor event detection, conflict detection,
                                              travel pre-fetch, widget verdict update

app/src/test/java/com/weatherapp/
  worker/
    CalendarScanWorkerTest.kt        ← NEW
```

Files to modify in this story:
```
app/src/main/java/com/weatherapp/
  worker/
    ForecastRefreshWorker.kt          ← MODIFY: confirm CalendarScanWorker enqueue when isPremium=true
                                               (this was added in Story 3.1; verify it's correct here)
  di/
    WorkerModule.kt                   ← MODIFY: ensure CalendarScanWorker is injectable via @HiltWorker
```

### Cross-Story Dependencies

- **Depends on Story 1.3**: `ForecastDao.queryByTimeWindow()`, `DataStore<Preferences>`, `PreferenceKeys.*`
- **Depends on Story 1.5**: `WeatherWidget.update()` companion method; `KEY_WIDGET_VERDICT` DataStore key
- **Depends on Story 3.1**: `KEY_IS_PREMIUM` gating, `ForecastRefreshWorker` enqueuing `CalendarScanWorker` when premium
- **Depends on Story 3.2**: `CalendarRepository.getUpcomingEvents()`, `CalendarEvent` domain model, `CalendarEventForecastDao`, `CalendarEventForecast` entity
- **Provides to Story 3.4**: `CalendarEventForecast` records (with `lastWeatherSnapshot`) consumed by `AlertEvaluationWorker` for material change detection; `CalendarEvent.eventId` as the per-event alert state machine key

### References

- [Source: architecture.md#Communication Patterns] — WorkManager → DataStore → Glance pipeline
- [Source: architecture.md#Process Patterns] — Coordinate snapping mandatory at network boundary
- [Source: architecture.md#Enforcement Guidelines] — Rules 2 (snapToGrid), 5 (display-ready strings), 9 (isPremium check), 10 (no Repository in Glance)
- [Source: epics.md#Story 3.3] — All acceptance criteria including conflict detection, travel pre-load, within-30-min updates

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

### Completion Notes List

### File List
