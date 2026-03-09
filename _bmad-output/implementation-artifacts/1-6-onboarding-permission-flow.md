# Story 1.6: Onboarding & Permission Flow

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a new user,
I want to grant permissions and have a live working widget on my home screen within 60 seconds of first opening the app,
so that I can start trusting WeatherApp without any friction or confusion.

## Acceptance Criteria

**AC-1:** Given a fresh install and first app launch when the app opens then `OnboardingScreen` is displayed (not the app home) because `DataStore[KEY_HAS_COMPLETED_ONBOARDING]` is `false`.

**AC-2:** Given the onboarding screen when the calendar permission rationale is displayed then it reads: "We use your calendar to tell you when weather matters to your plans." — before the system permission dialog appears.

**AC-3:** Given the user grants `ACCESS_COARSE_LOCATION` when the permission is granted then the permission is secured for later use by `ForecastRefreshWorker`; `ACCESS_FINE_LOCATION` is never requested at any point.

**AC-4:** Given the user grants `READ_CALENDAR` when both permissions are granted then `WorkManager` enqueues `ForecastRefreshWorker` for immediate one-time execution and the widget is pinned/instructed to appear on the home screen.

**AC-5:** Given the path from first app open to live widget on home screen when timed on a standard device with network available then it completes in ≤ 60 seconds with no additional user prompts after the permission grants.

**AC-6:** Given `POST_NOTIFICATIONS` permission when it is requested then it is requested only after the first widget render completes — not during the onboarding permission sequence.

**AC-7:** Given the user denies `ACCESS_COARSE_LOCATION` when onboarding continues then the user is prompted to enter a home location manually; `ACCESS_FINE_LOCATION` is never requested.

**AC-8:** Given the user denies `READ_CALENDAR` when onboarding continues then the app proceeds in weather-only mode; all free-tier widget features remain available; no in-app re-prompt is shown.

**AC-9:** Given `READ_CALENDAR` was previously granted and is revoked by the user in system settings when the app detects this on next launch then the app reverts to weather-only mode without crashing, and all free-tier features remain functional.

**AC-10:** Given `DataStore[KEY_HAS_COMPLETED_ONBOARDING]` is `true` when the app launches then `OnboardingScreen` is skipped and the app navigates directly to the main/settings screen.

## Tasks / Subtasks

- [ ] Task 1: Create routing logic in `MainActivity` based on onboarding state (AC: 1, 10)
  - [ ] 1.1 In `MainActivity.kt`, read `DataStore[KEY_HAS_COMPLETED_ONBOARDING]` synchronously at startup using `runBlocking { dataStore.data.first()[PreferenceKeys.KEY_HAS_COMPLETED_ONBOARDING] ?: false }`
  - [ ] 1.2 If `false` → navigate to `OnboardingScreen` as the start destination in NavHost
  - [ ] 1.3 If `true` → navigate to main screen (SettingsScreen or a home route) as the start destination
  - [ ] 1.4 Inject `DataStore<Preferences>` into `MainActivity` via Hilt `@AndroidEntryPoint`

- [ ] Task 2: Create `OnboardingViewModel.kt` (AC: 1–10)
  - [ ] 2.1 Create `app/src/main/java/com/weatherapp/ui/onboarding/OnboardingViewModel.kt`
  - [ ] 2.2 Annotate with `@HiltViewModel`, inject `DataStore<Preferences>` and `WorkManager`
  - [ ] 2.3 Expose `val uiState: StateFlow<UiState<OnboardingState>>` — NEVER raw nullable
  - [ ] 2.4 Define `data class OnboardingState(val step: OnboardingStep, val locationDenied: Boolean, val calendarDenied: Boolean, val manualLocation: String? = null)` and `enum class OnboardingStep { LOCATION_PERMISSION, CALENDAR_RATIONALE, CALENDAR_PERMISSION, MANUAL_LOCATION, COMPLETE }`
  - [ ] 2.5 Implement `fun onLocationGranted()` → advance to `CALENDAR_RATIONALE` step
  - [ ] 2.6 Implement `fun onLocationDenied()` → set `locationDenied = true`, advance to `MANUAL_LOCATION` step
  - [ ] 2.7 Implement `fun onManualLocationEntered(location: String)` → store for use by `LocationRepository`; advance to `CALENDAR_RATIONALE`
  - [ ] 2.8 Implement `fun onCalendarRationaleAcknowledged()` → advance to `CALENDAR_PERMISSION` step
  - [ ] 2.9 Implement `fun onCalendarGranted()` → call `completeOnboarding(calendarGranted = true)`
  - [ ] 2.10 Implement `fun onCalendarDenied()` → call `completeOnboarding(calendarGranted = false)` (weather-only mode)
  - [ ] 2.11 Implement `private suspend fun completeOnboarding(calendarGranted: Boolean)`:
    - Write `KEY_HAS_COMPLETED_ONBOARDING = true` to DataStore
    - Enqueue `ForecastRefreshWorker` as immediate one-time request
    - Enqueue periodic `ForecastRefreshWorker` (30-min interval, `KEEP` policy)
    - Call `WeatherWidget.update()` to trigger initial widget render
    - Advance to `COMPLETE` step

- [ ] Task 3: Create `OnboardingScreen.kt` (AC: 1–10)
  - [ ] 3.1 Create `app/src/main/java/com/weatherapp/ui/onboarding/OnboardingScreen.kt`
  - [ ] 3.2 Collect `viewModel.uiState` and render the appropriate step UI
  - [ ] 3.3 `LOCATION_PERMISSION` step: show rationale text + "Grant Location" button; use `rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())` for `ACCESS_COARSE_LOCATION` only — never `ACCESS_FINE_LOCATION`
  - [ ] 3.4 `CALENDAR_RATIONALE` step: show text "We use your calendar to tell you when weather matters to your plans." and a "Continue" button (AC-2)
  - [ ] 3.5 `CALENDAR_PERMISSION` step: launch `READ_CALENDAR` permission request after rationale step
  - [ ] 3.6 `MANUAL_LOCATION` step (AC-7): show text field for manual city entry + "Continue" button; this step appears when location permission is denied
  - [ ] 3.7 `COMPLETE` step: trigger widget pin and navigate away from onboarding
  - [ ] 3.8 Attempt to pin widget via `AppWidgetManager.requestPinAppWidget()` on the `COMPLETE` step (API 26+)
  - [ ] 3.9 All text elements use `sp` font units; all buttons have minimum 48×48dp touch targets

- [ ] Task 4: Implement permission request flows using Activity Result API (AC: 3, 4, 6, 7, 8)
  - [ ] 4.1 Location: `rememberLauncherForActivityResult(RequestPermission()) { granted -> if (granted) viewModel.onLocationGranted() else viewModel.onLocationDenied() }` — pass `Manifest.permission.ACCESS_COARSE_LOCATION`
  - [ ] 4.2 Calendar: `rememberLauncherForActivityResult(RequestPermission()) { granted -> if (granted) viewModel.onCalendarGranted() else viewModel.onCalendarDenied() }` — pass `Manifest.permission.READ_CALENDAR`
  - [ ] 4.3 POST_NOTIFICATIONS: do NOT request during onboarding; this is deferred to after first widget render (handled in `ForecastRefreshWorker` completion or `MainActivity`)
  - [ ] 4.4 ACCESS_FINE_LOCATION: must NEVER appear anywhere in the app — check with grep before completing story

- [ ] Task 5: Implement `POST_NOTIFICATIONS` request deferred to post-first-render (AC: 6)
  - [ ] 5.1 In `ForecastRefreshWorker.doWork()`, after successful DataStore write and `WeatherWidget.update()`, check if `POST_NOTIFICATIONS` has been requested before (use a DataStore key or WorkManager one-time task)
  - [ ] 5.2 If not yet requested, send a local broadcast or set a DataStore flag `KEY_SHOULD_REQUEST_NOTIFICATIONS = true`
  - [ ] 5.3 In `MainActivity.onResume()`, check this flag and request `POST_NOTIFICATIONS` permission if set — then clear the flag
  - [ ] 5.4 This ensures the notification permission dialog appears after the widget has rendered, not during onboarding

- [ ] Task 6: Implement graceful `READ_CALENDAR` revocation detection (AC: 9)
  - [ ] 6.1 In `CalendarRepository.getUpcomingEvents()` (Story 3.2), `SecurityException` handling already returns empty list
  - [ ] 6.2 In `ForecastRefreshWorker.doWork()`, check `READ_CALENDAR` permission state at cycle start via `ContextCompat.checkSelfPermission()`
  - [ ] 6.3 If permission is revoked: log `Timber.w("READ_CALENDAR permission revoked — operating in weather-only mode")`, skip calendar-related work, continue with weather-only refresh
  - [ ] 6.4 App must NOT crash, must NOT show an error state, must NOT re-prompt for the permission

- [ ] Task 7: Implement manual location entry path (AC: 7)
  - [ ] 7.1 When user denies location and enters a manual location, store the city string in DataStore with key `KEY_MANUAL_LOCATION = stringPreferencesKey("manual_location")` in `PreferenceKeys.kt`
  - [ ] 7.2 In `LocationRepository.getSnappedLocation()`, if `ACCESS_COARSE_LOCATION` is denied, read `KEY_MANUAL_LOCATION` from DataStore and use a geocoding lookup or a hardcoded lat/lon map for common cities
  - [ ] 7.3 For MVP, a simple city→lat/lon lookup table in `LocationRepository` is acceptable

- [ ] Task 8: Wire WorkManager enqueue in onboarding completion (AC: 4, 5)
  - [ ] 8.1 One-time immediate request:
    ```kotlin
    WorkManager.getInstance(context).enqueue(
        OneTimeWorkRequestBuilder<ForecastRefreshWorker>().build()
    )
    ```
  - [ ] 8.2 Periodic request (keep if already running):
    ```kotlin
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "forecast_refresh",
        ExistingPeriodicWorkPolicy.KEEP,
        PeriodicWorkRequestBuilder<ForecastRefreshWorker>(30, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build())
            .build()
    )
    ```
  - [ ] 8.3 Both enqueues happen in `OnboardingViewModel.completeOnboarding()`

- [ ] Task 9: Write unit tests for `OnboardingViewModel` (AC: 1, 7, 8, 10)
  - [ ] 9.1 Create `app/src/test/java/com/weatherapp/ui/onboarding/OnboardingViewModelTest.kt`
  - [ ] 9.2 Test initial state when `KEY_HAS_COMPLETED_ONBOARDING = false` → `LOCATION_PERMISSION` step
  - [ ] 9.3 Test `onLocationGranted()` → state advances to `CALENDAR_RATIONALE`
  - [ ] 9.4 Test `onLocationDenied()` → state advances to `MANUAL_LOCATION` (not `CALENDAR_RATIONALE`)
  - [ ] 9.5 Test `onCalendarDenied()` → `completeOnboarding()` called with `calendarGranted = false`; `KEY_HAS_COMPLETED_ONBOARDING = true` written to DataStore
  - [ ] 9.6 Test that `ForecastRefreshWorker` is enqueued on `completeOnboarding()`

## Dev Notes

### Critical Architecture Rules for This Story

**1. Permission Sequencing — Must Follow Exactly**

From architecture.md:
```
Permission Sequencing:
  Location (onboarding) → Calendar (onboarding) → Notifications (after first widget render, NOT during onboarding)
```

Never deviate from this sequence. `POST_NOTIFICATIONS` during onboarding is an explicit anti-pattern.

**2. ACCESS_FINE_LOCATION Must Never Be Requested**

From epics.md AC: "`ACCESS_FINE_LOCATION` is never requested at any point."

Use only:
```kotlin
Manifest.permission.ACCESS_COARSE_LOCATION
```

Audit the entire codebase before closing this story to confirm no `ACCESS_FINE_LOCATION` string appears anywhere.

**3. OnboardingViewModel — StateFlow<UiState<T>> Pattern**

```kotlin
// OnboardingViewModel.kt
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<OnboardingState>>(
        UiState.Success(OnboardingState(step = OnboardingStep.LOCATION_PERMISSION))
    )
    val uiState: StateFlow<UiState<OnboardingState>> = _uiState.asStateFlow()

    fun onLocationGranted() {
        updateState { it.copy(step = OnboardingStep.CALENDAR_RATIONALE) }
    }

    fun onLocationDenied() {
        updateState { it.copy(step = OnboardingStep.MANUAL_LOCATION, locationDenied = true) }
    }

    private fun updateState(transform: (OnboardingState) -> OnboardingState) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(transform(current))
    }

    fun completeOnboarding(calendarGranted: Boolean) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.KEY_HAS_COMPLETED_ONBOARDING] = true
                }
                // Enqueue workers (see Task 8 for exact syntax)
                enqueueWorkers()
                _uiState.value = UiState.Success(
                    OnboardingState(step = OnboardingStep.COMPLETE, calendarDenied = !calendarGranted)
                )
            } catch (e: Exception) {
                Timber.e(e, "Onboarding completion failed")
                _uiState.value = UiState.Error("Setup failed. Please restart the app.")
            }
        }
    }
}
```

**4. Calendar Permission Rationale — Exact Text**

AC-2 specifies the exact rationale text. Do not paraphrase:
```
"We use your calendar to tell you when weather matters to your plans."
```

This text must appear as a separate UI step BEFORE the system permission dialog launches.

**5. Widget Pinning**

```kotlin
// OnboardingScreen.kt — COMPLETE step
LaunchedEffect(step) {
    if (step == OnboardingStep.COMPLETE) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val provider = ComponentName(context, WeatherWidgetReceiver::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            appWidgetManager.isRequestPinAppWidgetSupported) {
            appWidgetManager.requestPinAppWidget(provider, null, null)
        }
        // Navigate away from onboarding
        onOnboardingComplete()
    }
}
```

Note: `requestPinAppWidget` returns a boolean indicating whether the system will show a pin confirmation dialog. The actual widget pin is not guaranteed — the user must accept the system dialog. This is the expected behavior.

**6. 60-Second Flow Design**

To meet NFR-002 (≤ 60 seconds from first launch to live widget), the design requires:
- Onboarding UI loads instantly (no blocking calls on main thread)
- Permission dialogs appear immediately after rationale text (no animation delays)
- `ForecastRefreshWorker` runs immediately (OneTimeWorkRequest, CONNECTED constraint only)
- DataStore write completes within seconds of worker run
- `WeatherWidget.update()` forces immediate re-render after DataStore write

Do NOT add: loading spinners in onboarding UI, network calls before permissions are granted, or any artificial delays.

**7. KEY_MANUAL_LOCATION Addition to PreferenceKeys.kt**

This story adds a new key to `PreferenceKeys.kt`:

```kotlin
// Add to PreferenceKeys.kt
val KEY_MANUAL_LOCATION = stringPreferencesKey("manual_location")
val KEY_SHOULD_REQUEST_NOTIFICATIONS = booleanPreferencesKey("should_request_notifications")
```

**8. READ_CALENDAR Revocation Graceful Degradation**

The app must never crash when `READ_CALENDAR` is revoked mid-session. The protection happens at two levels:
1. `CalendarRepository`: every query wrapped in `try/catch(SecurityException)` (Story 3.2)
2. `ForecastRefreshWorker`: checks permission at cycle start, skips calendar work silently if revoked

For this story (free-tier, no calendar features yet), the primary concern is that onboarding correctly marks the state when calendar is denied and doesn't re-prompt.

**9. Anti-Patterns to Avoid**

- NEVER request `ACCESS_FINE_LOCATION` — check entire codebase
- NEVER request `POST_NOTIFICATIONS` during onboarding flow
- NEVER call `CalendarContract` queries in the UI layer or ViewModel
- **CRITICAL — ANR RISK**: Task 1.1 specifies `runBlocking { dataStore.data.first()[...] }` on the main thread. This WILL cause an ANR on any device where DataStore I/O takes >5 seconds (cold start, storage pressure). Replace with a `SplashScreen` or loading state approach: render a neutral launch screen, read DataStore asynchronously in a coroutine, then navigate once the read completes. Never call `runBlocking` on the main thread for DataStore or any disk I/O.
- NEVER block the main thread for DataStore reads at launch — use `runBlocking` only for the initial routing decision, then switch to coroutines
- NEVER re-prompt for denied permissions in-app — system handles this via settings
- NEVER use `Log.*` — use `Timber.d/i/w/e()`
- NEVER assume permission is granted between permission check and actual use

### Project Structure Notes

Files to create in this story:
```
app/src/main/java/com/weatherapp/
  ui/
    onboarding/
      OnboardingScreen.kt         ← NEW: step-based permission flow UI
      OnboardingViewModel.kt      ← NEW: StateFlow<UiState<OnboardingState>>

app/src/test/java/com/weatherapp/
  ui/
    onboarding/
      OnboardingViewModelTest.kt  ← NEW: unit tests for all state transitions
```

Files to modify in this story:
```
app/src/main/java/com/weatherapp/
  data/datastore/
    PreferenceKeys.kt              ← MODIFY: add KEY_MANUAL_LOCATION, KEY_SHOULD_REQUEST_NOTIFICATIONS
  data/location/
    LocationRepository.kt          ← MODIFY: add manual location fallback path
  worker/
    ForecastRefreshWorker.kt        ← MODIFY: add KEY_SHOULD_REQUEST_NOTIFICATIONS flag after first render
  MainActivity.kt                   ← MODIFY: initial routing logic, POST_NOTIFICATIONS deferred request
```

### Cross-Story Dependencies

- **Depends on Story 1.3**: `PreferenceKeys.kt`, `DataStore<Preferences>`, `ForecastRefreshWorker` class, `WorkManager` setup
- **Depends on Story 1.5**: `WeatherWidget` class and `WeatherWidgetReceiver` class for widget pinning; `WeatherWidget.update()` for triggering initial render
- **Provides to Story 1.7**: `DataStore[KEY_HAS_COMPLETED_ONBOARDING]` is `true` after completion, enabling main app navigation to hourly detail
- **Provides to Story 2.3**: `KEY_SHOULD_REQUEST_NOTIFICATIONS` flag pattern; `POST_NOTIFICATIONS` deferred request in `MainActivity`
- **Provides to Story 3.1**: `WorkManager` periodic worker setup that Story 3.1 will gate with `isPremium` check

### References

- [Source: architecture.md#Permission Boundary] — Each repository owns its own permission checks; no cross-class permission checking
- [Source: architecture.md#Enforcement Guidelines] — Rules 3 (CalendarContract safety), 9 (isPremium check)
- [Source: epics.md#Story 1.6] — All acceptance criteria including exact permission sequencing
- [Source: architecture.md#Requirements to Structure Mapping] — FR-008, FR-018

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

### Completion Notes List

### File List
