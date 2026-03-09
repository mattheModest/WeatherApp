# Story 2.3: Notification Delivery & Permission Flow

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a user,
I want to receive at most one daily weather alert that proactively confirms good news or warns of a genuine change,
so that I feel looked after rather than notified at — and can trust that every alert is worth reading.

## Acceptance Criteria

**AC-1:** Given the app's first launch after `POST_NOTIFICATIONS` has not yet been requested when the first widget render completes then the system `POST_NOTIFICATIONS` permission dialog is shown — not during onboarding.

**AC-2:** Given the user grants `POST_NOTIFICATIONS` when a confirmation-first alert fires then it is delivered via the "Weather Alerts" notification channel with priority `CATEGORY_RECOMMENDATION`.

**AC-3:** Given a confirmation-first notification when it is delivered then the content reads as good news (e.g., "You're clear today. Go live your day.") — not a warning.

**AC-4:** Given a change-triggered notification (free tier: location-level material change) when it is delivered then the content identifies what changed and why it matters (e.g., "Rain moving in this afternoon — conditions changed since this morning.").

**AC-5:** Given the user has denied `POST_NOTIFICATIONS` when the alert evaluation determines a notification should fire then no notification is delivered; the app does not re-prompt; widget-only mode continues silently.

**AC-6:** Given the notification channel "Weather Alerts" when created then it is a single channel, user-dismissable in Android system settings, with importance `IMPORTANCE_DEFAULT` and `CATEGORY_RECOMMENDATION`.

**AC-7:** Given free-tier alert logic when reviewed then at most one alert fires per day per location, regardless of how many WorkManager cycles run.

## Tasks / Subtasks

- [ ] Task 1: Formalize the "Weather Alerts" notification channel (AC: 2, 6)
  - [ ] 1.1 Create `app/src/main/java/com/weatherapp/util/NotificationChannels.kt`
  - [ ] 1.2 Define:
    ```kotlin
    object NotificationChannels {
        const val CHANNEL_ID_WEATHER_ALERTS = "weather_alerts"
        const val CHANNEL_NAME = "Weather Alerts"
        const val CHANNEL_DESCRIPTION = "Daily weather alerts and condition changes"
    }
    ```
  - [ ] 1.3 Create idempotent channel registration function: `fun ensureWeatherAlertsChannel(context: Context)` — callable from multiple places safely
  - [ ] 1.4 Channel importance: `NotificationManager.IMPORTANCE_DEFAULT`
  - [ ] 1.5 `setShowBadge(false)` — no badge on app icon for weather alerts

- [ ] Task 2: Register notification channel on app startup (AC: 6)
  - [ ] 2.1 Call `NotificationChannels.ensureWeatherAlertsChannel(this)` in `WeatherApp.Application.onCreate()`
  - [ ] 2.2 This ensures the channel exists before any Worker runs; Workers should call it too for resilience
  - [ ] 2.3 Verify: in Android Settings → App → Notifications, "Weather Alerts" appears as a toggleable channel

- [ ] Task 3: Implement `POST_NOTIFICATIONS` deferred permission request flow (AC: 1, 5)
  - [ ] 3.1 Add `val KEY_NOTIFICATIONS_PERMISSION_REQUESTED = booleanPreferencesKey("notifications_permission_requested")` to `PreferenceKeys.kt`
  - [ ] 3.2 In `ForecastRefreshWorker.doWork()`, after successful `WeatherWidget.update()`: check if `KEY_NOTIFICATIONS_PERMISSION_REQUESTED` is `false`, if so write `KEY_SHOULD_REQUEST_NOTIFICATIONS = true` to DataStore
  - [ ] 3.3 In `MainActivity.onResume()`:
    ```kotlin
    val shouldRequest = runBlocking { dataStore.data.first()[PreferenceKeys.KEY_SHOULD_REQUEST_NOTIFICATIONS] ?: false }
    if (shouldRequest && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        dataStore.edit { prefs -> prefs[PreferenceKeys.KEY_SHOULD_REQUEST_NOTIFICATIONS] = false }
        dataStore.edit { prefs -> prefs[PreferenceKeys.KEY_NOTIFICATIONS_PERMISSION_REQUESTED] = true }
        requestNotificationsPermission() // launches ActivityResultLauncher
    }
    ```
  - [ ] 3.4 Use `rememberLauncherForActivityResult(RequestPermission())` in MainActivity for `Manifest.permission.POST_NOTIFICATIONS` (API 33+)
  - [ ] 3.5 When permission is denied by user: log `Timber.d("POST_NOTIFICATIONS denied — widget-only mode")`, do NOT re-prompt

- [ ] Task 4: Audit and finalize confirmation-first notification content (AC: 3)
  - [ ] 4.1 Review `AlertEvaluationWorker.kt` (Story 2.2) confirmation notification builder
  - [ ] 4.2 Ensure content title is affirmative: "You're clear today" or "Looking good today"
  - [ ] 4.3 Ensure content text is action-oriented: "Go live your day." or "Conditions look good all day."
  - [ ] 4.4 Category must be `NotificationCompat.CATEGORY_RECOMMENDATION` — not `CATEGORY_STATUS` or `CATEGORY_ALARM`
  - [ ] 4.5 Priority: `NotificationCompat.PRIORITY_DEFAULT` — not HIGH or MAX

- [ ] Task 5: Audit and finalize change-triggered notification content (AC: 4)
  - [ ] 5.1 Review `AlertEvaluationWorker.kt` change-triggered notification builder
  - [ ] 5.2 Content must identify the change: "Rain moving in this afternoon — conditions changed since this morning."
  - [ ] 5.3 If wind: "Wind picking up this afternoon — stronger than expected."
  - [ ] 5.4 Content must NOT be generic ("Weather changed") — must say WHAT changed
  - [ ] 5.5 Build dynamic content text from the `ForecastSnapshot` comparison:
    ```kotlin
    fun buildChangeNotificationText(
        snapshot: ForecastSnapshot,
        currentHours: List<ForecastHour>
    ): String {
        val precipIncreased = currentHours.maxOf { it.precipitationProbability } - snapshot.precipProb >= 0.20
        val windCrossed = currentHours.maxOf { it.windSpeedKmh } >= 40.0
        return when {
            precipIncreased -> "Rain moving in — conditions changed since this morning."
            windCrossed     -> "Wind picking up significantly — conditions changed."
            else            -> "Forecast changed since your morning check."
        }
    }
    ```

- [ ] Task 6: Verify at-most-once-per-day enforcement (AC: 7)
  - [ ] 6.1 The at-most-once constraint is enforced by the `AlertState` machine: CONFIRMED_CLEAR can only be entered from UNCHECKED; once in CONFIRMED_CLEAR, the worker skips the confirmation notification on subsequent cycles
  - [ ] 6.2 Write a test: run `AlertEvaluationWorker` 5 times with all-clear conditions → verify notification is sent exactly once
  - [ ] 6.3 The `alert_state_record` record for today's key persists across WorkManager cycles; querying it at the start of each cycle ensures idempotency

- [ ] Task 7: Permission-denied silent mode (AC: 5)
  - [ ] 7.1 In `AlertEvaluationWorker`, the permission check before `NotificationManagerCompat.notify()` (Story 2.2) handles this
  - [ ] 7.2 Verify: when `POST_NOTIFICATIONS` is denied, `AlertEvaluationWorker`:
    - State machine transitions still occur (UNCHECKED → CONFIRMED_CLEAR is recorded in Room)
    - Notification is silently suppressed
    - `Result.success()` is returned (not failure)
    - No re-prompt for permission happens
  - [ ] 7.3 This means the user can grant permission later and receive future alerts immediately (state machine is up to date)

- [ ] Task 8: Add `KEY_SHOULD_REQUEST_NOTIFICATIONS` and `KEY_NOTIFICATIONS_PERMISSION_REQUESTED` to PreferenceKeys.kt (AC: 1)
  - [ ] 8.1 Update `app/src/main/java/com/weatherapp/data/datastore/PreferenceKeys.kt`:
    ```kotlin
    val KEY_SHOULD_REQUEST_NOTIFICATIONS = booleanPreferencesKey("should_request_notifications")
    val KEY_NOTIFICATIONS_PERMISSION_REQUESTED = booleanPreferencesKey("notifications_permission_requested")
    ```
  - [ ] 8.2 Verify no inline string literals — all DataStore keys through PreferenceKeys

- [ ] Task 9: Add small notification icon (AC: 2)
  - [ ] 9.1 Create `app/src/main/res/drawable/ic_weather_notification.xml` — a simple vector drawable (sun or cloud icon)
  - [ ] 9.2 The icon must work at small sizes (24dp) and in monochrome (Android uses the alpha channel for notification icons)
  - [ ] 9.3 Reference as `R.drawable.ic_weather_notification` in all notification builders

- [ ] Task 10: End-to-end integration test (AC: 1–7)
  - [ ] 10.1 Manual test plan:
    - Fresh install → onboarding → complete without requesting notifications
    - Widget renders → notification permission dialog appears in MainActivity.onResume()
    - Grant notification → `ForecastRefreshWorker` runs → `AlertEvaluationWorker` runs → notification delivered
    - Second `AlertEvaluationWorker` run → no duplicate notification
    - Revoke notification permission in settings → third run → no crash, no notification, state machine records correctly

## Dev Notes

### Critical Architecture Rules for This Story

**1. POST_NOTIFICATIONS — API 33+ Only**

`POST_NOTIFICATIONS` permission only exists on Android 13+ (API 33, codename Tiramisu). Always guard:

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    // request or check POST_NOTIFICATIONS
}
```

On Android 12 (API 32) and below, notifications are delivered without needing runtime permission. The app targets API 34 minimum, so this is technically always needed — but keep the guard for forward compatibility.

**2. Notification Channel — Single Source of Truth**

```kotlin
// util/NotificationChannels.kt
object NotificationChannels {
    const val CHANNEL_ID_WEATHER_ALERTS = "weather_alerts"

    fun ensureWeatherAlertsChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID_WEATHER_ALERTS,
            "Weather Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily weather alerts and condition changes"
            setShowBadge(false)
        }
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }
}
```

Call from:
1. `WeatherApp.onCreate()` — ensures channel exists on app start
2. `AlertEvaluationWorker.doWork()` — ensures channel exists before notifying (defense in depth)

**3. Deferred Notification Permission Request — Exact Flow**

From epics.md: "POST_NOTIFICATIONS is requested only after the first widget render completes — not during onboarding."

Implementation sequence:
```
ForecastRefreshWorker.doWork() (first run after onboarding)
  → WeatherWidget.update() called
  → DataStore[KEY_SHOULD_REQUEST_NOTIFICATIONS] = true
    (if KEY_NOTIFICATIONS_PERMISSION_REQUESTED == false)

MainActivity.onResume() (next time app is foregrounded)
  → reads KEY_SHOULD_REQUEST_NOTIFICATIONS
  → if true: launches POST_NOTIFICATIONS permission request
  → clears KEY_SHOULD_REQUEST_NOTIFICATIONS
  → sets KEY_NOTIFICATIONS_PERMISSION_REQUESTED = true
  → (future resumptions will not re-request)
```

**4. Notification Content — Good News Tone**

The product requires confirmation-first notifications to feel like good news, not alerts:

| Type | Title | Content |
|------|-------|---------|
| Confirmation | "You're clear today" | "Go live your day." |
| Confirmation (alt) | "Looking good today" | "Conditions stay clear through the afternoon." |
| Change - Rain | "Conditions changed" | "Rain moving in this afternoon — different from this morning." |
| Change - Wind | "Conditions changed" | "Wind picking up significantly — check your plans." |

NEVER use: "Warning", "Alert", "Danger", "Bad weather" in confirmation notifications.

**5. At-Most-Once Enforcement — State Machine**

```
First cycle (UNCHECKED + all-clear):
  → insertRecord(CONFIRMED_CLEAR) — notification queued ✓

Second cycle (CONFIRMED_CLEAR + still all-clear):
  → getByEventId → state == CONFIRMED_CLEAR
  → skip notification (already sent today) ✓

Third cycle (CONFIRMED_CLEAR + rain developing):
  → material change detected
  → insertRecord(ALERT_SENT) — change notification queued ✓

Fourth cycle (ALERT_SENT + still raining):
  → state == ALERT_SENT
  → no action (alert already sent) ✓
```

**6. AndroidManifest.xml — Required Declarations**

```xml
<!-- Required for POST_NOTIFICATIONS on Android 13+ -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Optional: vibration for notifications -->
<uses-permission android:name="android.permission.VIBRATE" />
```

**7. Anti-Patterns to Avoid**

- NEVER request POST_NOTIFICATIONS during onboarding
- NEVER show a re-prompt dialog when POST_NOTIFICATIONS is denied
- NEVER send more than one confirmation notification per day per location
- NEVER use HIGH or MAX notification priority — PRIORITY_DEFAULT only
- NEVER use Log.* — use Timber.d/i/w/e()
- NEVER create a second notification channel — one channel named "Weather Alerts" only

### Project Structure Notes

Files to create in this story:
```
app/src/main/java/com/weatherapp/
  util/
    NotificationChannels.kt          ← NEW: channel constants + ensureWeatherAlertsChannel()

app/src/main/res/
  drawable/
    ic_weather_notification.xml      ← NEW: monochrome notification icon vector
```

Files to modify in this story:
```
app/src/main/java/com/weatherapp/
  WeatherApp.kt                       ← MODIFY: call ensureWeatherAlertsChannel() in onCreate()
  MainActivity.kt                     ← MODIFY: POST_NOTIFICATIONS request in onResume()
  data/datastore/
    PreferenceKeys.kt                 ← MODIFY: add KEY_SHOULD_REQUEST_NOTIFICATIONS,
                                               KEY_NOTIFICATIONS_PERMISSION_REQUESTED
  worker/
    ForecastRefreshWorker.kt          ← MODIFY: set KEY_SHOULD_REQUEST_NOTIFICATIONS flag after first render
    AlertEvaluationWorker.kt          ← MODIFY: use NotificationChannels.CHANNEL_ID_WEATHER_ALERTS;
                                               refine notification content per this story's specs
app/src/main/AndroidManifest.xml    ← MODIFY: add POST_NOTIFICATIONS permission declaration
```

### Cross-Story Dependencies

- **Depends on Story 1.3**: `PreferenceKeys.kt`, `ForecastRefreshWorker` (to set the notification flag)
- **Depends on Story 1.5**: `WeatherWidget.update()` triggers the "first render" flag
- **Depends on Story 1.6**: Onboarding explicitly skips POST_NOTIFICATIONS; flag initialized after onboarding
- **Depends on Story 2.2**: `AlertEvaluationWorker` queues notifications; this story formalizes the channel and content
- **Provides to Story 3.4**: The "Weather Alerts" channel is reused for premium per-event change-triggered notifications

### References

- [Source: architecture.md#Enforcement Guidelines] — Rule 3: No unguarded CalendarContract queries; analogously, no unguarded notification sends
- [Source: epics.md#Story 2.3] — All acceptance criteria including post-first-render timing
- [Source: epics.md#Additional Requirements - From UX Design] — Permission Sequencing: "Notifications (after first widget render, not during onboarding)"

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

### Completion Notes List

### File List
