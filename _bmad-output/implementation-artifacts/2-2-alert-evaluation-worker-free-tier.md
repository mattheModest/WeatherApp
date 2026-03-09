# Story 2.2: Alert Evaluation Worker (Free Tier)

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a developer,
I want `AlertEvaluationWorker` to evaluate current forecast conditions against alert thresholds after each refresh cycle,
so that the system knows when to send a confirmation-first alert and when to stay silent.

## Acceptance Criteria

**AC-1:** Given `AlertEvaluationWorker` is chained to run after `ForecastRefreshWorker` when it runs then it reads the latest forecast from Room and the current `AlertStateRecord` for the day's location-based key.

**AC-2:** Given the forecast meets the all-clear threshold (no material adverse conditions) when the current `AlertState` is `UNCHECKED` then the state transitions to `CONFIRMED_CLEAR`, the confirmed forecast snapshot is written to the record, and a confirmation-first notification is queued.

**AC-3:** Given the forecast meets all-clear threshold but `CONFIRMED_CLEAR` was already sent today when the worker evaluates then no additional notification is queued — confirmation fires at most once per day per location.

**AC-4:** Given a previously `CONFIRMED_CLEAR` record exists and the forecast now shows a material change (≥ 20% precipitation probability shift OR wind speed crossing 25 mph) when the worker evaluates then the state transitions to `ALERT_SENT` and a change-triggered notification is queued.

**AC-5:** Given the forecast does not meet all-clear threshold and no prior `CONFIRMED_CLEAR` exists when the worker evaluates then the state remains `UNCHECKED`; no notification is queued.

**AC-6:** Given the event end time has passed when `resolveExpired()` is called then the record transitions to `RESOLVED` and is excluded from future evaluation cycles.

**AC-7:** Given `AlertEvaluationWorker` code when reviewed then it checks `POST_NOTIFICATIONS` permission before queuing any notification; if permission is denied, evaluation completes silently with no crash.

## Tasks / Subtasks

- [ ] Task 1: Create `AlertEvaluationWorker.kt` (AC: 1–7)
  - [ ] 1.1 Create `app/src/main/java/com/weatherapp/worker/AlertEvaluationWorker.kt`
  - [ ] 1.2 Annotate with `@HiltWorker`, extend `CoroutineWorker`
  - [ ] 1.3 Inject `AlertStateDao`, `ForecastDao`, `DataStore<Preferences>`, `NotificationManager` (or use `context.getSystemService()`) via `@AssistedInject`
  - [ ] 1.4 In `doWork()`:
    - Step 1: Read `KEY_NOTIFICATIONS_ENABLED` from DataStore; if false, return `Result.success()` silently
    - Step 2: Compute today's location-based `eventId` = `"location:{latGrid}:{lonGrid}:{date}"`
    - Step 3: Call `alertStateDao.resolveExpired(yesterdayEpoch)` to clean up yesterday's records
    - Step 4: Load `AlertStateRecord` for today's key
    - Step 5: Load today's forecast hours from `ForecastDao`
    - Step 6: Evaluate conditions and execute state machine transition
    - Step 7: Return `Result.success()`

- [ ] Task 2: Implement all-clear threshold evaluation (AC: 2, 3, 5)
  - [ ] 2.1 Define all-clear criteria: `precipProb < AlertThresholds.ALL_CLEAR_PRECIP_MAX (0.20)` AND `windKmh < AlertThresholds.ALL_CLEAR_WIND_MAX_KMH (30.0)` for ALL daylight hours
  - [ ] 2.2 Implement `fun isAllClear(hours: List<ForecastHour>): Boolean` — returns true only if ALL hours meet the criteria (conservative)
  - [ ] 2.3 If `isAllClear = true` AND `currentRecord.state == UNCHECKED` → transition to `CONFIRMED_CLEAR`, queue confirmation notification, write snapshot
  - [ ] 2.4 If `isAllClear = true` AND `currentRecord.state == CONFIRMED_CLEAR` → do nothing (at-most-once rule)
  - [ ] 2.5 If `isAllClear = false` AND `currentRecord.state == UNCHECKED` → do nothing, state remains UNCHECKED

- [ ] Task 3: Implement material change detection (AC: 4)
  - [ ] 3.1 Material change criteria: `|currentPrecipProb - snapshotPrecipProb| >= 0.20` OR `currentMaxWindKmh >= AlertThresholds.WIND_SPEED_ALERT_KMH (40.0)` AND `snapshotMaxWindKmh < 40.0`
  - [ ] 3.2 Only evaluate material change when `currentRecord.state == CONFIRMED_CLEAR`
  - [ ] 3.3 Deserialize `confirmedForecastSnapshot` using Gson → `ForecastSnapshot`
  - [ ] 3.4 Compute current forecast metrics for the same time window as the snapshot
  - [ ] 3.5 If material change detected → transition to `ALERT_SENT`, queue change-triggered notification
  - [ ] 3.6 If no material change → do nothing

- [ ] Task 4: Implement state transitions (AC: 2, 4, 5, 6)
  - [ ] 4.1 All transitions use `alertStateDao.insertRecord(newRecord)` — never UPDATE SQL
  - [ ] 4.2 UNCHECKED → CONFIRMED_CLEAR:
    ```kotlin
    val snapshot = buildForecastSnapshot(hours)
    alertStateDao.insertRecord(AlertStateRecord(
        eventId = todayKey,
        state = AlertState.CONFIRMED_CLEAR,
        confirmedForecastSnapshot = Gson().toJson(snapshot),
        lastTransitionAt = nowEpoch
    ))
    ```
  - [ ] 4.3 CONFIRMED_CLEAR → ALERT_SENT:
    ```kotlin
    alertStateDao.insertRecord(AlertStateRecord(
        eventId = todayKey,
        state = AlertState.ALERT_SENT,
        confirmedForecastSnapshot = existing.confirmedForecastSnapshot, // preserve original snapshot
        lastTransitionAt = nowEpoch
    ))
    ```
  - [ ] 4.4 Call `alertStateDao.resolveExpired(startOfYesterdayEpoch)` at the start of each cycle to clean up old records

- [ ] Task 5: Implement notification queuing with permission check (AC: 7)
  - [ ] 5.1 Before queuing ANY notification, check `ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED`
  - [ ] 5.2 If permission not granted: `Timber.d("POST_NOTIFICATIONS not granted — skipping notification")` and continue without crash
  - [ ] 5.3 Create notification channel "Weather Alerts" if not already created (idempotent):
    ```kotlin
    val channel = NotificationChannel(
        CHANNEL_ID,
        "Weather Alerts",
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply { description = "Daily weather alerts"; setShowBadge(false) }
    notificationManager.createNotificationChannel(channel)
    ```
  - [ ] 5.4 Build confirmation-first notification:
    ```kotlin
    NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_weather_notification)
        .setContentTitle("You're clear today")
        .setContentText("Go live your day.")
        .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()
    ```
  - [ ] 5.5 Build change-triggered notification:
    ```kotlin
    NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_weather_notification)
        .setContentTitle("Conditions changed")
        .setContentText("Rain moving in this afternoon — conditions changed since this morning.")
        .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()
    ```
  - [ ] 5.6 Use `NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)`
  - [ ] 5.7 Notification ID: `NOTIFICATION_ID = 1001` for confirmation, `NOTIFICATION_ID = 1002` for change-triggered — using the same ID means only one notification is shown at a time per type

- [ ] Task 6: Chain `AlertEvaluationWorker` after `ForecastRefreshWorker` (AC: 1)
  - [ ] 6.1 In `ForecastRefreshWorker.doWork()`, at the very end (after `WeatherWidget.update()`):
    ```kotlin
    val alertWork = OneTimeWorkRequestBuilder<AlertEvaluationWorker>().build()
    workManager.enqueue(alertWork)
    ```
  - [ ] 6.2 Inject `WorkManager` into `ForecastRefreshWorker` via `@AssistedInject`
  - [ ] 6.3 Alternatively, use WorkManager chaining at enqueue time: `WorkManager.getInstance(context).beginWith(refreshWork).then(alertWork).enqueue()` — but this is more complex for periodic work. Simple `enqueue()` at the end of `doWork()` is acceptable for v1.

- [ ] Task 7: Implement location-based `eventId` construction (AC: 1, 2)
  - [ ] 7.1 Read `latGrid` and `lonGrid` from DataStore (stored by `LocationRepository` or `ForecastRefreshWorker`)
  - [ ] 7.2 Alternatively, re-read from `LocationRepository` at the start of `doWork()`
  - [ ] 7.3 Format: `"location:${latGrid}:${lonGrid}:${LocalDate.now(ZoneOffset.UTC)}"`
  - [ ] 7.4 This key is used for free tier; Story 3.4 will use `eventId` from `CalendarEvent` for premium

- [ ] Task 8: Write unit tests for `AlertEvaluationWorker` (AC: 1–7)
  - [ ] 8.1 Create `app/src/test/java/com/weatherapp/worker/AlertEvaluationWorkerTest.kt`
  - [ ] 8.2 Test: UNCHECKED + all-clear forecast → transitions to CONFIRMED_CLEAR, notification queued
  - [ ] 8.3 Test: UNCHECKED + not all-clear forecast → state stays UNCHECKED, no notification
  - [ ] 8.4 Test: CONFIRMED_CLEAR + same good forecast → no transition, no notification (at-most-once)
  - [ ] 8.5 Test: CONFIRMED_CLEAR + material change (≥20% precip shift) → ALERT_SENT, change notification queued
  - [ ] 8.6 Test: CONFIRMED_CLEAR + wind crosses 40 km/h threshold → ALERT_SENT
  - [ ] 8.7 Test: POST_NOTIFICATIONS denied → no notification queued, no crash, returns `Result.success()`
  - [ ] 8.8 Test: `isAllClear()` with mixed hours (some clear, some not) → returns false (conservative)

## Dev Notes

### Critical Architecture Rules for This Story

**1. Worker Structure — doWork() Pattern**

```kotlin
@HiltWorker
class AlertEvaluationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val alertStateDao: AlertStateDao,
    private val forecastDao: ForecastDao,
    private val dataStore: DataStore<Preferences>,
    private val workManager: WorkManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("AlertEvaluationWorker started")
        return try {
            // 1. Check notifications enabled preference
            val notificationsEnabled = dataStore.data.first()[PreferenceKeys.KEY_NOTIFICATIONS_ENABLED] ?: true
            if (!notificationsEnabled) {
                Timber.d("Notifications disabled by user — skipping alert evaluation")
                return Result.success()
            }

            // 2. Resolve expired records
            val startOfYesterday = /* compute yesterday midnight epoch */
            alertStateDao.resolveExpired(startOfYesterday)

            // 3. Build today's location key
            val todayKey = buildLocationKey()

            // 4. Get current state and forecast
            val existing = alertStateDao.getByEventId(todayKey)
            val todayHours = forecastDao.queryByTimeWindow(startOfToday, endOfToday).first()

            // 5. Run state machine
            evaluateAndTransition(todayKey, existing, todayHours)

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "AlertEvaluationWorker failed")
            Result.failure()
        }
    }
}
```

**2. Notification Permission Check — Required Before Every Notify**

```kotlin
private fun sendNotification(notification: Notification, id: Int) {
    if (ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Timber.d("POST_NOTIFICATIONS not granted — notification suppressed")
        return
    }
    NotificationManagerCompat.from(applicationContext).notify(id, notification)
}
```

**3. Material Change Thresholds**

```kotlin
// From architecture.md:
// "material change = ≥ 20% shift in precipitation probability, or wind speed crossing 25 mph"
// 25 mph ≈ 40 km/h

private fun isMaterialChange(
    snapshot: ForecastSnapshot,
    currentHours: List<ForecastHour>
): Boolean {
    val currentMaxPrecip = currentHours.maxOf { it.precipitationProbability }
    val currentMaxWind = currentHours.maxOf { it.windSpeedKmh }

    val precipShift = Math.abs(currentMaxPrecip - snapshot.precipProb)
    val windCrossed = currentMaxWind >= 40.0 && snapshot.windKmh < 40.0

    return precipShift >= 0.20 || windCrossed
}
```

**4. Notification Channel — Create Once**

The notification channel must be created idempotently. Creating the same channel ID twice is safe on Android (it's a no-op):

```kotlin
companion object {
    const val CHANNEL_ID = "weather_alerts"
    const val NOTIFICATION_ID_CONFIRMATION = 1001
    const val NOTIFICATION_ID_CHANGE = 1002
}

private fun ensureNotificationChannel() {
    val channel = NotificationChannel(
        CHANNEL_ID,
        "Weather Alerts",
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description = "Weather alerts for your day"
        setShowBadge(false)
    }
    (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        .createNotificationChannel(channel)
}
```

**5. All-Clear Evaluation — Conservative**

The all-clear evaluation must be conservative to avoid false positives (NFR-003):

```kotlin
private fun isAllClear(hours: List<ForecastHour>): Boolean {
    if (hours.isEmpty()) return false
    // Filter to daylight hours (7am to 8pm)
    val daylightHours = hours.filter { hour ->
        val localHour = Instant.ofEpochSecond(hour.hourEpoch)
            .atZone(ZoneId.systemDefault()).hour
        localHour in 7..20
    }
    if (daylightHours.isEmpty()) return false
    // ALL daylight hours must meet the all-clear threshold
    return daylightHours.all { hour ->
        hour.precipitationProbability < AlertThresholds.ALL_CLEAR_PRECIP_MAX &&
        hour.windSpeedKmh < AlertThresholds.ALL_CLEAR_WIND_MAX_KMH
    }
}
```

**6. Location Key Construction**

The location key for free tier uses snapped coordinates. The `AlertEvaluationWorker` needs the snapped lat/lon. Options:
- Store them in DataStore after LocationRepository returns them in `ForecastRefreshWorker`
- Or re-call `LocationRepository.getSnappedLocation()` in `AlertEvaluationWorker`

Recommended: store snapped coordinates in DataStore after first successful fetch:
```kotlin
// PreferenceKeys.kt — add these two keys
val KEY_SNAPPED_LAT = stringPreferencesKey("snapped_lat")
val KEY_SNAPPED_LON = stringPreferencesKey("snapped_lon")
```

**7. Anti-Patterns to Avoid**

- NEVER send a notification without checking `POST_NOTIFICATIONS` permission first
- NEVER send more than one confirmation notification per day per location (at-most-once)
- NEVER use `UPDATE` SQL for state transitions — always `INSERT OR REPLACE` the entire record
- NEVER expose alert evaluation logic in ViewModel or UI layer — Workers only
- NEVER use `Log.*` — use `Timber.d/i/w/e()`

### Project Structure Notes

Files to create in this story:
```
app/src/main/java/com/weatherapp/
  worker/
    AlertEvaluationWorker.kt         ← NEW: state machine evaluation + notification queuing
  model/
    AlertThresholds.kt               ← NEW (if not already created): threshold constants

app/src/test/java/com/weatherapp/
  worker/
    AlertEvaluationWorkerTest.kt     ← NEW
```

Files to modify in this story:
```
app/src/main/java/com/weatherapp/
  worker/
    ForecastRefreshWorker.kt          ← MODIFY: chain AlertEvaluationWorker at end of doWork()
  data/datastore/
    PreferenceKeys.kt                 ← MODIFY: add KEY_SNAPPED_LAT, KEY_SNAPPED_LON (if using DataStore approach)
  di/
    WorkerModule.kt                   ← MODIFY: ensure AlertEvaluationWorker is injectable via Hilt
```

### Cross-Story Dependencies

- **Depends on Story 1.3**: `ForecastDao.queryByTimeWindow()`, `ForecastRefreshWorker` (to be chained from)
- **Depends on Story 1.8**: `KEY_NOTIFICATIONS_ENABLED` DataStore key
- **Depends on Story 2.1**: `AlertStateDao`, `AlertStateRecord`, `AlertState` enum, `ForecastSnapshot` model
- **Provides to Story 2.3**: `AlertEvaluationWorker` queues notifications using the channel defined in Story 2.3; the channel must be created in Story 2.3 or here (Story 2.3 formalizes the notification delivery contract)
- **Provides to Story 3.4**: `AlertEvaluationWorker` will be extended in Story 3.4 to handle premium per-event evaluation using the same state machine

### References

- [Source: architecture.md#API & Communication Patterns] — Alert State Machine transitions
- [Source: architecture.md#Process Patterns] — WorkManager error handling pattern
- [Source: architecture.md#Enforcement Guidelines] — All 10 rules apply
- [Source: epics.md#Story 2.2] — All acceptance criteria

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

### Completion Notes List

### File List
