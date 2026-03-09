# Story 3.4: Change-Triggered Event Alerts

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a premium user,
I want to receive an alert only when the forecast for one of my specific calendar events changes materially after I've already been told it looks clear,
so that I can plan confidently knowing I'll be warned if something genuinely changes — without being spammed.

## Acceptance Criteria

**AC-1:** Given `AlertEvaluationWorker` runs after `CalendarScanWorker` for a premium user when it evaluates a calendar event with an existing `CONFIRMED_CLEAR` `AlertStateRecord` then it compares the current forecast snapshot against the stored `confirmed_forecast_snapshot` using the thresholds: >= 20% precipitation probability shift OR wind speed crossing 25 mph within the event's time window.

**AC-2:** Given the comparison shows a material change when the event's `AlertState` is `CONFIRMED_CLEAR` then the state transitions to `ALERT_SENT`, a per-event change-triggered notification is queued, and the new forecast snapshot is stored in the record.

**AC-3:** Given a premium change-triggered notification when delivered then it identifies the specific event by name and time (e.g., "Your Run (6:30am) - Rain now likely. Conditions changed since last check.") — never a generic location-based message.

**AC-4:** Given an event with title keyword signals indicating high stakes (e.g., "marathon", "wedding", "match") when `AlertEvaluationWorker` determines the monitoring window then the alert lead time is extended beyond the 2-hour minimum, scaled to the inferred importance of the event.

**AC-5:** Given any change-triggered premium alert when it fires then it fires no earlier than 2 hours before the event's `startEpoch`; alerts for events starting in < 2 hours are suppressed.

**AC-6:** Given a premium alert evaluation when the `AlertState` is already `ALERT_SENT` then no duplicate notification is queued for the same material change; the state machine does not re-fire on subsequent cycles unless conditions improve back to `CONFIRMED_CLEAR` and then deteriorate again.

**AC-7:** Given a new `AlertStateRecord` for a premium calendar event when first created by `AlertEvaluationWorker` then it uses the `eventId` from `CalendarEvent` as the primary key — not a location-based key — ensuring per-event tracking independent of location.

**AC-8:** Given `POST_NOTIFICATIONS` permission is denied when `AlertEvaluationWorker` runs for a premium user then state machine transitions still occur (records update correctly) but no notification is delivered; no crash occurs.

## Tasks / Subtasks

- [ ] Task 1: Extend `AlertEvaluationWorker` to handle premium per-event evaluation (AC: 1, 7)
  - [ ] 1.1 Update `app/src/main/java/com/weatherapp/worker/AlertEvaluationWorker.kt`
  - [ ] 1.2 Inject `CalendarRepository` and `CalendarEventForecastDao` via `@AssistedInject`
  - [ ] 1.3 At the start of `doWork()`, read `isPremium` from DataStore
  - [ ] 1.4 If `isPremium = true`: after running free-tier evaluation, also run premium per-event evaluation
  - [ ] 1.5 Premium evaluation path:
    - Call `calendarRepository.getUpcomingEvents(7)` (permission-safe, returns empty list if revoked)
    - Filter to outdoor-potential events (reuse `CalendarScanWorker.isOutdoorPotential()` — extract to shared utility or duplicate logic in `AlertEvaluationWorker`)
    - For each event: call `evaluatePremiumEventAlert(event)`

- [ ] Task 2: Implement `evaluatePremiumEventAlert()` (AC: 1, 2, 5, 6)
  - [ ] 2.1 Load `AlertStateRecord` for `event.eventId` from `AlertStateDao`
  - [ ] 2.2 If no record exists or state is `UNCHECKED`:
    - Check current forecast for event window: `forecastDao.queryByTimeWindow(event.startEpoch, event.endEpoch).first()`
    - If all-clear: `insertRecord(AlertStateRecord(event.eventId, CONFIRMED_CLEAR, snapshot, nowEpoch))`
    - Do NOT send confirmation notification for premium events on first detection (or optionally do — see note below)
  - [ ] 2.3 If state is `CONFIRMED_CLEAR`:
    - Deserialize `confirmedForecastSnapshot`
    - Compute fresh forecast for event window
    - Check material change thresholds (AC-1)
    - If material change detected AND event starts in ≥ 2 hours (AC-5): transition to `ALERT_SENT` and queue notification
    - If event starts in < 2 hours: suppress alert (`Timber.d("Event too close — suppressing alert")`), still record state transition
  - [ ] 2.4 If state is `ALERT_SENT`:
    - Check if conditions have improved back to all-clear
    - If yes: transition to `CONFIRMED_CLEAR` with new snapshot (allows future ALERT_SENT if conditions worsen again)
    - If no: do nothing — no duplicate notifications (AC-6)
  - [ ] 2.5 If state is `RESOLVED`: skip event

- [ ] Task 3: Implement stakes-scaled alert lead time (AC: 4)
  - [ ] 3.1 Define high-stakes keywords with extended monitoring windows:
    ```kotlin
    private val HIGH_STAKES_KEYWORDS = mapOf(
        "marathon" to 6,   // 6 hours lead time
        "wedding" to 12,   // 12 hours lead time
        "match" to 4,      // 4 hours lead time
        "race" to 6,       // 6 hours lead time
        "graduation" to 8, // 8 hours lead time
        "concert" to 4,    // 4 hours lead time
        "festival" to 4    // 4 hours lead time
    )
    ```
  - [ ] 3.2 Implement `fun getAlertLeadTimeHours(event: CalendarEvent): Int`:
    - Check if title contains any high-stakes keyword (case-insensitive)
    - Return the associated lead time, or 2 (minimum) if no match
  - [ ] 3.3 In `evaluatePremiumEventAlert()`, check: `(event.startEpoch - nowEpoch) / 3600 >= alertLeadTime` before sending notification
  - [ ] 3.4 Example: for a wedding at 4pm, if lead time is 12 hours, alerts are suppressed until 4am

- [ ] Task 4: Build per-event notification content (AC: 3)
  - [ ] 4.1 Notification title: `"Forecast change: ${event.title}"`
  - [ ] 4.2 Notification content: `"Your ${event.title} (${formatHour(event.startEpoch)}) — ${changeDescription}. Conditions changed since last check."`
  - [ ] 4.3 Build `changeDescription` dynamically:
    ```kotlin
    private fun buildChangeDescription(
        snapshot: ForecastSnapshot,
        freshHours: List<ForecastHour>
    ): String {
        val newMaxPrecip = freshHours.maxOf { it.precipitationProbability }
        val newMaxWind = freshHours.maxOf { it.windSpeedKmh }
        return when {
            newMaxPrecip - snapshot.precipProb >= 0.20 -> "Rain now likely"
            newMaxWind >= 40.0 && snapshot.windKmh < 40.0 -> "Wind now significant"
            else -> "Conditions worsened"
        }
    }
    ```
  - [ ] 4.4 Example output: `"Your Run (6:30am) — Rain now likely. Conditions changed since last check."`
  - [ ] 4.5 Use notification ID: `event.eventId.hashCode()` — unique per event, no collision between events

- [ ] Task 5: Wire `AlertEvaluationWorker` to run after `CalendarScanWorker` for premium users (AC: 1)
  - [ ] 5.1 Currently, `AlertEvaluationWorker` is chained after `ForecastRefreshWorker` (Story 2.2)
  - [ ] 5.2 For premium users, the correct chain is: `ForecastRefreshWorker` → `CalendarScanWorker` → `AlertEvaluationWorker`
  - [ ] 5.3 Update `CalendarScanWorker.doWork()` to enqueue `AlertEvaluationWorker` at the very end:
    ```kotlin
    val alertWork = OneTimeWorkRequestBuilder<AlertEvaluationWorker>().build()
    workManager.enqueue(alertWork)
    Timber.d("AlertEvaluationWorker enqueued after CalendarScanWorker")
    ```
  - [ ] 5.4 For free-tier users (where `CalendarScanWorker` doesn't run), `ForecastRefreshWorker` still chains `AlertEvaluationWorker` directly — no change needed for free tier

- [ ] Task 6: Update state machine record for event-level vs location-level records (AC: 7)
  - [ ] 6.1 Free-tier records use key: `"location:{latGrid}:{lonGrid}:{date}"`
  - [ ] 6.2 Premium event records use key: `event.eventId` (raw CalendarContract event ID, a numeric string)
  - [ ] 6.3 Both record types coexist in the same `alert_state_record` table — they are distinguished by their `eventId` format
  - [ ] 6.4 `AlertEvaluationWorker` must resolve expired records for BOTH free-tier (date-based expiry) and premium (event end time-based expiry):
    ```kotlin
    // Free-tier: resolve records older than yesterday
    alertStateDao.resolveExpired(startOfYesterdayEpoch)
    // Premium: resolve records where the event has ended
    // The resolveExpired query uses last_transition_at; for event records,
    // we need to track event end time separately or use event.endEpoch < nowEpoch
    ```

- [ ] Task 7: Implement POST_NOTIFICATIONS permission check for premium alerts (AC: 8)
  - [ ] 7.1 Same permission check pattern as Story 2.2: check before every `notify()` call
  - [ ] 7.2 State machine transitions (Room writes) must still occur even when permission is denied
  - [ ] 7.3 Verify: with `POST_NOTIFICATIONS` denied, `AlertEvaluationWorker` still transitions `UNCHECKED → CONFIRMED_CLEAR` for event records correctly

- [ ] Task 8: Write comprehensive unit tests for premium alert logic (AC: 1–8)
  - [ ] 8.1 Update `app/src/test/java/com/weatherapp/worker/AlertEvaluationWorkerTest.kt`
  - [ ] 8.2 Test: CONFIRMED_CLEAR event + material precip change → ALERT_SENT + notification queued
  - [ ] 8.3 Test: CONFIRMED_CLEAR event + material wind change → ALERT_SENT + notification queued
  - [ ] 8.4 Test: CONFIRMED_CLEAR event + event starts in 1 hour → alert suppressed, state still transitions to ALERT_SENT
  - [ ] 8.5 Test: ALERT_SENT state + continued bad conditions → no duplicate notification
  - [ ] 8.6 Test: ALERT_SENT state + conditions improved → CONFIRMED_CLEAR (allows future re-alert)
  - [ ] 8.7 Test: "marathon" event → lead time = 6 hours; alert suppressed if < 6 hours to start
  - [ ] 8.8 Test: "Doctor appointment" event (non-outdoor) → not evaluated in premium path (filtered out)
  - [ ] 8.9 Test: `POST_NOTIFICATIONS` denied → state transitions occur, no crash, no notification
  - [ ] 8.10 Test: notification content for rain change → "Your Run (6:30am) — Rain now likely. Conditions changed since last check."
  - [ ] 8.11 Test: event-based eventId (numeric string) vs location-based eventId — both coexist in DB without collision

## Dev Notes

### Critical Architecture Rules for This Story

**1. Per-Event AlertStateRecord — eventId is CalendarEvent.eventId**

```kotlin
// PREMIUM path — per-event key
val eventRecord = alertStateDao.getByEventId(event.eventId) // e.g., "67890"

// FREE TIER path — per-location key
val locationKey = "location:37.8:-122.4:2026-03-08"
val locationRecord = alertStateDao.getByEventId(locationKey)
```

These two record types coexist in the same table; their key format distinguishes them.

**2. State Machine Transitions — Always INSERT, Never UPDATE**

```kotlin
// CORRECT — premium UNCHECKED → CONFIRMED_CLEAR
val snapshot = buildForecastSnapshot(eventHours, event)
alertStateDao.insertRecord(AlertStateRecord(
    eventId = event.eventId,       // CalendarEvent.eventId
    state = AlertState.CONFIRMED_CLEAR,
    confirmedForecastSnapshot = Gson().toJson(snapshot),
    lastTransitionAt = nowEpoch
))

// CORRECT — premium CONFIRMED_CLEAR → ALERT_SENT
alertStateDao.insertRecord(AlertStateRecord(
    eventId = event.eventId,
    state = AlertState.ALERT_SENT,
    confirmedForecastSnapshot = existingRecord.confirmedForecastSnapshot, // preserve original
    lastTransitionAt = nowEpoch
))
```

**3. 2-Hour Minimum Lead Time**

```kotlin
private fun isWithinAlertWindow(event: CalendarEvent): Boolean {
    val nowEpoch = System.currentTimeMillis() / 1000L
    val hoursUntilEvent = (event.startEpoch - nowEpoch) / 3600.0
    val requiredLeadHours = getAlertLeadTimeHours(event)
    val isWithinWindow = hoursUntilEvent <= requiredLeadHours
    val isNotTooClose = hoursUntilEvent >= 2.0
    Timber.d("Event '${event.title}': ${hoursUntilEvent}h until start, lead=${requiredLeadHours}h, alert=${isWithinWindow && isNotTooClose}")
    return isWithinWindow && isNotTooClose
}
```

Wait — re-reading the AC: "fires no earlier than 2 hours before the event's `startEpoch`; alerts for events starting in < 2 hours are suppressed."

This means: alert window = [startEpoch - leadTime, startEpoch - 2h]. Alerts fire ONLY within this window.

```kotlin
private fun shouldSendAlert(event: CalendarEvent): Boolean {
    val nowEpoch = System.currentTimeMillis() / 1000L
    val hoursUntilEvent = (event.startEpoch - nowEpoch) / 3600.0
    val leadHours = getAlertLeadTimeHours(event).toDouble()
    // Must be: within lead time AND at least 2 hours away
    return hoursUntilEvent <= leadHours && hoursUntilEvent >= 2.0
}
```

**4. doWork() Structure for Premium Path**

```kotlin
override suspend fun doWork(): Result {
    val isPremium = dataStore.data.first()[PreferenceKeys.KEY_IS_PREMIUM] ?: false

    // Free tier evaluation (always)
    runFreeTierEvaluation()

    // Premium per-event evaluation (only if premium)
    if (isPremium) {
        val events = calendarRepository.getUpcomingEvents(7)
            .filter { isOutdoorPotential(it) }
        events.forEach { event ->
            evaluatePremiumEventAlert(event)
        }
    }

    return Result.success()
}
```

**5. Notification ID — Per-Event Unique**

To prevent notifications for different events from overwriting each other:

```kotlin
val notificationId = event.eventId.hashCode().let {
    // Ensure positive and avoid collision with free-tier IDs (1001, 1002)
    if (it < 0) (it * -1) + 2000 else it + 2000
}
```

This generates a unique notification ID per calendar event.

**6. Premium Confirmation Notification — Optional**

The epics.md does not explicitly specify whether premium users receive a "you're clear" confirmation notification for calendar events. Based on FR-009 ("widget shifts state to reflect event") and FR-010 ("change-triggered alert when forecast changes materially"), the primary value is in the change-triggered alert. For MVP, skip the confirmation notification for premium events (free tier already sends a daily confirmation). Only send the CHANGE-TRIGGERED notification for premium events.

Document this decision:
```kotlin
// DESIGN NOTE: For premium calendar events, we skip the CONFIRMED_CLEAR notification
// because the widget already proactively shows the event-specific state.
// Only ALERT_SENT notifications are sent for premium events.
// If the product team wants to add a confirmation "Your BBQ looks clear!" notification,
// this is where it would be added.
if (state == AlertState.UNCHECKED && isAllClearForEvent) {
    insertRecord(CONFIRMED_CLEAR, snapshot)
    // Intentionally not queuing a notification here for premium events
    Timber.i("Event '${event.title}' confirmed clear — no notification (premium mode)")
}
```

**7. Re-Confirmation After ALERT_SENT**

If conditions improve after an alert was sent (e.g., rain forecast shifted away), the state machine can transition back to CONFIRMED_CLEAR. This allows a subsequent deterioration to trigger another ALERT_SENT. This prevents "one alert per event ever" — which would be too conservative:

```kotlin
// ALERT_SENT state: check if conditions improved
if (existingRecord.state == AlertState.ALERT_SENT && isAllClearForEvent) {
    Timber.i("Event '${event.title}' recovered to all-clear after alert")
    insertRecord(CONFIRMED_CLEAR, freshSnapshot) // Fresh snapshot for new baseline
    // Optionally: send "All clear again" notification? For MVP, skip.
}
```

**8. Anti-Patterns to Avoid**

- NEVER use a location-based key for premium per-event records — always use `event.eventId`
- NEVER send a duplicate notification when `AlertState` is already `ALERT_SENT`
- NEVER ignore the 2-hour minimum lead time rule
- NEVER crash when `POST_NOTIFICATIONS` is denied — state machine must still update
- NEVER update Room records with SQL UPDATE — always INSERT with REPLACE strategy
- NEVER use `Log.*` — use `Timber.d/i/w/e()`
- NEVER query `CalendarRepository` from a Glance composable — Workers only

### Project Structure Notes

Files to modify in this story:
```
app/src/main/java/com/weatherapp/
  worker/
    AlertEvaluationWorker.kt          ← MODIFY: add premium per-event evaluation path;
                                               inject CalendarRepository, CalendarEventForecastDao;
                                               add evaluatePremiumEventAlert(), shouldSendAlert(),
                                               getAlertLeadTimeHours(), buildChangeDescription()
    CalendarScanWorker.kt             ← MODIFY: chain AlertEvaluationWorker at end of doWork()
                                               for premium users

app/src/test/java/com/weatherapp/
  worker/
    AlertEvaluationWorkerTest.kt      ← MODIFY: add all premium alert test cases (Tasks 8.2–8.11)
```

No new files are created in this story — all changes are additions to existing workers and tests.

### Cross-Story Dependencies

- **Depends on Story 2.1**: `AlertState` enum, `AlertStateRecord` entity, `AlertStateDao`
- **Depends on Story 2.2**: `AlertEvaluationWorker` skeleton; free-tier evaluation logic; permission check pattern
- **Depends on Story 2.3**: Notification channel `CHANNEL_ID_WEATHER_ALERTS`, notification builder pattern
- **Depends on Story 3.1**: `KEY_IS_PREMIUM` gating in `doWork()` start
- **Depends on Story 3.2**: `CalendarRepository.getUpcomingEvents()`, `CalendarEvent.eventId`, `CalendarEventForecastDao`
- **Depends on Story 3.3**: `CalendarScanWorker` runs before `AlertEvaluationWorker` in the premium chain; `CalendarEventForecast` records with `lastWeatherSnapshot` are available
- **This is the final story** — no other stories depend on Story 3.4

### References

- [Source: architecture.md#API & Communication Patterns] — Alert state machine: CONFIRMED_CLEAR → ALERT_SENT → re-CONFIRMED_CLEAR cycle
- [Source: architecture.md#Process Patterns] — Append-only Room records (INSERT, never UPDATE SQL)
- [Source: architecture.md#Enforcement Guidelines] — Rule 9 (isPremium check at worker start), Rule 3 (CalendarContract safety)
- [Source: epics.md#Story 3.4] — All acceptance criteria including stakes-scaled lead time, 2-hour minimum, per-event notification content

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

### Completion Notes List

### File List
