---
stepsCompleted: ['step-01-validate-prerequisites', 'step-02-design-epics', 'step-03-epic-1', 'step-03-epic-2', 'step-03-epic-3', 'step-04-final-validation']
status: complete
completedAt: '2026-03-08'
inputDocuments:
  - '_bmad-output/planning-artifacts/prd.md'
  - '_bmad-output/planning-artifacts/architecture.md'
  - '_bmad-output/planning-artifacts/ux-design-specification.md'
---

# WeatherApp - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for WeatherApp, decomposing the requirements from the PRD, UX Design, and Architecture into implementable stories.

## Requirements Inventory

### Functional Requirements

**Free Tier**

FR-001: Users can read the day's weather verdict in clothing language (e.g., "light jacket weather") without seeing raw temperature values.

FR-002: Users receive a contextual bring list item (umbrella, sunscreen) only when precipitation probability or UV index crosses a threshold warranting that item for that day.

FR-003: Users can view the best outdoor window for the day (e.g., "Best time outside: 11am–2pm") derived from hourly forecast data.

FR-004: Users see an explicit all-clear state ("You're good. Go live your day.") when no weather action is warranted.

FR-005: Users can view a mood line and generate a shareable weather card from the app.

FR-006: Users can access hourly forecast detail with one tap from the widget verdict; the hourly view is not the default surface.

FR-007: Users receive confirmation-first alerts proactively on good news; the alert system escalates only when a previously confirmed forecast materially changes.

FR-008: Users complete onboarding — granting READ_CALENDAR permission and seeing a live widget — in a single session requiring no additional prompts after the permission grant.

**Premium Tier**

FR-009: Premium users: the widget shifts state to reflect a specific upcoming calendar event (e.g., "Your BBQ (12pm) is clear.") before the user opens the app.

FR-010: Premium users receive a change-triggered alert when the forecast for a specific calendar event window changes materially after a prior all-clear confirmation.

FR-011: Premium users receive alerts with monitoring windows scaled to inferred event importance, derived from event duration and title keyword signals (e.g., "BBQ", "run", "match").

FR-012: Premium users: the widget silently pre-loads and displays weather for the location of upcoming non-home calendar events without any user action.

FR-013: Premium users: when two or more calendar events with outdoor potential overlap in time, the widget surfaces the conflict explicitly (e.g., "2 outdoor events at 3pm · Light rain · Check both.") rather than selecting one event silently.

**Reliability & Operations**

FR-014: When the CalendarContract query returns malformed or non-ASCII event data, the app sanitizes the input and continues widget operation without stalling or crashing.

FR-015: When forecast data cannot be refreshed, the widget displays a staleness signal indicating the age of the last successful update; it never silently displays stale data as current.

FR-016: After a user modifies or removes a calendar event, the widget reflects the updated state within 30 minutes.

FR-017: The free tier widget surface displays no upgrade prompts, degraded states, or feature previews; the premium upgrade path is accessible only through the in-app settings screen.

FR-018: When READ_CALENDAR permission is denied or revoked, the app operates in weather-only mode without crashing, preserving all free tier features.

### NonFunctional Requirements

NFR-001 (Widget Freshness): The widget shall reflect the current forecast within 30 minutes at all times, as measured by the delta between the last WorkManager refresh timestamp and the time displayed in the widget staleness signal.

NFR-002 (Onboarding Speed): The system shall complete the path from first app launch to a live widget in ≤ 60 seconds, requiring no more than one permission prompt during that flow.

NFR-003 (Alert Precision): The system shall produce zero false-positive change-triggered alerts in the first 30 days of production (material change = ≥ 20% shift in precipitation probability, or wind speed crossing 25 mph, within a calendar event window).

NFR-004 (Background Battery Impact): The WorkManager refresh cycle shall not appear in the Android battery usage breakdown under normal refresh frequency (every 30 minutes).

NFR-005 (API Call Budget): The system shall not exceed 72,000 Open-Meteo API calls per month at 1,000 MAU, enforced through location-cluster caching that groups requests by geographic proximity (0.1° grid).

### Additional Requirements

**From Architecture:**

- Starter Template: Android Studio "Empty Activity" (Compose) + Manual Dependency Stack; project initialization and Gradle configuration is the first implementation story
- Architecture Pattern: MVVM + Repository + StateFlow; single-module for v1
- Storage Split: DataStore Preferences (widget display state, settings, isPremium, hasCompletedOnboarding) + Room (hourly forecast cache, calendar event forecast records, alert state machine records)
- Alert State Machine: 4-state per-event model (UNCHECKED → CONFIRMED_CLEAR → ALERT_SENT → RESOLVED); append-only Room records
- Network Proxy: Cloudflare Worker (TypeScript) + KV cache with 30-min TTL and 0.1° grid clustering; deployed via Wrangler CLI
- Privacy: Device-side coordinate snapping via `.snapToGrid()` before any network request; raw GPS coordinates never transmitted
- Workers: ForecastRefreshWorker (30-min periodic), CalendarScanWorker (premium), AlertEvaluationWorker (chained after CalendarScan)
- DI: Hilt 2.56 with HiltWorker for WorkManager injection
- CI: GitHub Actions (lint + test + build on PR)
- Logging: Timber only — never `Log.*` or `println`
- Error Boundaries: Result<T> from all Repository methods; no raw throws across layer boundaries
- CalendarContract: Every query wrapped in try/catch(SecurityException); graceful empty-list degradation
- DataStore Keys: Defined centrally in PreferenceKeys.kt only; no inline string literals

**From UX Design:**

- Accessibility: WCAG AA compliance across all states; AAA where feasible on verdict and primary text
- TalkBack: Content descriptions on all Glance root elements; minimum 48×48dp touch targets throughout
- Font Scale: Use `sp` units throughout; test at 85%, 100%, 130%, 150%, 200% font scale
- Widget Sizes: 4×2 (full state with verdict + bring chips + mood line); 4×1 minimum (verdict-only, StalenessIndicator retained)
- Dark Mode: Support all 6 combinations (3 weather states × light/dark); Adaptive Sky palette, not Material You dynamic color
- Platform Scope: Android phones only for v1 — no tablet, no web, no desktop
- Rendering: No spinners after first widget render; content pre-cached before render; loading indicators use weatherAccent color
- Permission Sequencing: Location (onboarding) → Calendar (onboarding) → Notifications (after first widget render, not during onboarding)
- Typography: Verdict line uses distinct larger/confident-weight style; body text readable at glance distance
- Density: Generous whitespace on widget; standard M3 density on config screen

### FR Coverage Map

FR-001: Epic 1 — Clothing-language verdict
FR-002: Epic 1 — Contextual bring list
FR-003: Epic 1 — Best outdoor window
FR-004: Epic 1 — All-clear state
FR-005: Epic 1 — Mood line + shareable card
FR-006: Epic 1 — Hourly tap-through detail
FR-007: Epic 2 — Confirmation-first alerts
FR-008: Epic 1 — 60-second onboarding
FR-009: Epic 3 — Proactive calendar widget shift
FR-010: Epic 3 — Change-triggered alerts per event
FR-011: Epic 3 — Stakes-scaled alert windows
FR-012: Epic 3 — Silent travel pre-load
FR-013: Epic 3 — Calendar conflict detection & surfacing
FR-014: Epic 3 — CalendarContract malformed data safety
FR-015: Epic 1 — Staleness signal (never silently stale)
FR-016: Epic 3 — Widget update within 30 min of calendar change
FR-017: Epic 1 — No upgrade prompts on free widget
FR-018: Epic 1 — Permission denied/revoked graceful degradation

## Epic List

### Epic 1: Core Free Weather Experience
Users can install WeatherApp, complete onboarding in ≤ 60 seconds, and immediately have a home screen widget delivering clothing-language verdicts, a contextual bring list, best outdoor window, all-clear state, mood line, and hourly tap-through detail — all updating automatically every 30 minutes.
**FRs covered:** FR-001, FR-002, FR-003, FR-004, FR-005, FR-006, FR-008, FR-015, FR-017, FR-018
**NFRs addressed:** NFR-001, NFR-002, NFR-004, NFR-005

### Epic 2: Smart Alert System
Users receive proactive confirmation-first alerts when the day looks clear, and change-triggered notifications only when a forecast materially changes — earning every interrupt or staying silent.
**FRs covered:** FR-007
**NFRs addressed:** NFR-003

### Epic 3: Premium Calendar Intelligence
Premium subscribers see event-specific weather on the widget before they think to check, receive alerts calibrated to event stakes, get travel pre-load for non-home locations, and see transparent conflict surfacing when outdoor events overlap — all driven silently from calendar access granted once.
**FRs covered:** FR-009, FR-010, FR-011, FR-012, FR-013, FR-014, FR-016

---

## Epic 1: Core Free Weather Experience

Users can install WeatherApp, complete onboarding in ≤ 60 seconds, and immediately have a home screen widget delivering clothing-language verdicts, a contextual bring list, best outdoor window, all-clear state, mood line, and hourly tap-through detail — all updating automatically every 30 minutes.

### Story 1.1: Project Foundation & CI Setup

As a developer,
I want a properly configured Android project with all dependencies pinned and a CI pipeline running,
So that the team can build, lint, and test WeatherApp reliably from day one.

**Acceptance Criteria:**

**Given** Android Studio with Kotlin 2.1.20
**When** the project is created using "Empty Activity (Compose)" with minimum SDK API 34 and Kotlin DSL
**Then** the project builds successfully with zero errors

**Given** the project-level `build.gradle.kts`
**When** reviewed
**Then** it contains AGP 9.1.0, Kotlin 2.1.20, Hilt 2.56, KSP 2.1.20-1.0.29 plugin declarations

**Given** the app-level `build.gradle.kts`
**When** reviewed
**Then** it contains Compose BOM 2026.02.01, Glance 1.1.1, WorkManager 2.10.0, Hilt 2.56, DataStore 1.1.2, Room 2.6.1, Retrofit 3.0.0, Play Billing 7.1.1, and all testing dependencies as specified in the Architecture document

**Given** `WeatherApp.kt` (Application class)
**When** the app launches in debug mode
**Then** Timber is planted via `Timber.DebugTree()` and Hilt is initialized via `@HiltAndroidApp`

**Given** the feature-first package structure
**When** the project is opened
**Then** directories exist for `ui/widget`, `ui/onboarding`, `ui/hourly`, `ui/settings`, `ui/theme`, `data/weather`, `data/calendar`, `data/billing`, `data/location`, `data/datastore`, `data/db`, `worker`, `di`, `model`, `util` under `com.weatherapp`

**Given** a pull request is opened to the main branch
**When** CI runs
**Then** GitHub Actions executes lint, unit tests, and a debug build — and the PR is blocked if any check fails

---

### Story 1.2: Cloudflare Weather Proxy

As a developer,
I want a Cloudflare Worker that proxies Open-Meteo with location-cluster KV caching,
So that the app can fetch weather data within budget while never transmitting raw GPS coordinates.

**Acceptance Criteria:**

**Given** `cloudflare-worker/src/worker.ts` deployed via Wrangler
**When** a request arrives as `GET /forecast?lat_grid=37.8&lon_grid=-122.4&date=2026-03-08`
**Then** the Worker checks KV for key `forecast:37.8:-122.4:2026-03-08` and returns the cached response if it exists and is < 30 minutes old

**Given** no cached entry exists in KV
**When** the Worker receives a forecast request
**Then** it calls Open-Meteo for the grid coordinates, stores the result in KV with a 30-minute TTL, and returns the response to the device

**Given** the Worker response
**When** parsed
**Then** it matches the JSON schema: `{ lat_grid, lon_grid, fetched_at (ISO 8601 UTC), hourly_forecasts: [{ hour_epoch, temperature_c, precipitation_probability, wind_speed_kmh, weather_code }] }`

**Given** Open-Meteo returns an error
**When** the Worker handles it
**Then** it returns an HTTP 502 with body `{"message": "upstream error"}` — no envelope wrapper on success responses

**Given** `wrangler.toml`
**When** reviewed
**Then** it declares the KV namespace binding and the Worker route; the Worker deploys successfully via `wrangler deploy`

---

### Story 1.3: Weather Data Layer

As a developer,
I want Room, DataStore, LocationRepository, and WeatherRepository wired together with a 30-minute WorkManager refresh cycle,
So that forecast data flows from the Cloudflare proxy into local storage and is continuously kept fresh.

**Acceptance Criteria:**

**Given** `PreferenceKeys.kt`
**When** reviewed
**Then** it is the single source of truth for all DataStore key strings; no inline string literals exist at any call site

**Given** `AppDatabase`
**When** initialized
**Then** it includes the `ForecastHour` entity (table: `forecast_hour`) with columns `hour_epoch`, `temperature_c`, `precipitation_probability`, `wind_speed_kmh`, `weather_code`

**Given** `ForecastDao`
**When** called
**Then** it supports `insert(List<ForecastHour>)`, `queryByTimeWindow(startEpoch, endEpoch): Flow<List<ForecastHour>>`, and `deleteExpired(beforeEpoch)`

**Given** raw GPS coordinates from `LocationRepository`
**When** passed to any network call
**Then** they are first snapped via `Double.snapToGrid(0.1)` in `CoordinateUtils.kt` — raw coordinates never reach `WeatherApi`

**Given** `WeatherRepository.fetchForecast()`
**When** the network succeeds
**Then** it writes hourly rows to Room via `ForecastDao` and returns `Result.success()`

**Given** the network fails with `IOException`
**When** `WeatherRepository.fetchForecast()` is called
**Then** it returns `Result.failure()` without throwing across the layer boundary

**Given** `ForecastRefreshWorker` registered as a `PeriodicWorkRequest` with a 30-minute interval
**When** it runs
**Then** it calls `WeatherRepository.fetchForecast()`, writes the staleness flag to DataStore before the call and clears it on success, and returns `Result.success()`, `Result.retry()` (up to 3 attempts on `IOException`), or `Result.failure()` as appropriate

**Given** `ForecastRefreshWorker` fails after 3 retries
**When** the staleness flag check runs
**Then** the DataStore staleness flag remains set, and the widget will display the staleness indicator

---

### Story 1.4: Verdict Generation Engine

As a developer,
I want `ForecastRefreshWorker` to translate raw hourly forecast data into clothing-language verdict text, bring list, best outdoor window, all-clear state, and mood line — writing display-ready strings to DataStore,
So that the widget can render the complete free-tier experience without any data transformation.

**Acceptance Criteria:**

**Given** hourly forecast data where the day's peak feels-like temperature maps to a "light jacket" range
**When** the verdict is generated
**Then** `DataStore[KEY_WIDGET_VERDICT]` contains a clothing-language string (e.g., "Light jacket weather") — never a raw temperature value

**Given** precipitation probability exceeds the bring-umbrella threshold for any window during the day
**When** the bring list is evaluated
**Then** `DataStore[KEY_BRING_LIST]` contains "Bring an umbrella" (or equivalent) and the widget displays it

**Given** UV index exceeds the sunscreen threshold for any afternoon window
**When** the bring list is evaluated
**Then** `DataStore[KEY_BRING_LIST]` contains "Sunscreen today"

**Given** no precipitation and UV index below threshold
**When** the bring list is evaluated
**Then** `DataStore[KEY_BRING_LIST]` is empty and no bring chip appears on the widget

**Given** hourly forecast showing a clear window of ≥ 2 consecutive hours during daylight
**When** the best outdoor window is calculated
**Then** `DataStore[KEY_BEST_WINDOW]` contains a time-range string (e.g., "Best time outside: 11am–2pm")

**Given** no weather action is warranted for the day (clear, no rain, moderate conditions)
**When** the verdict is generated
**Then** `DataStore[KEY_ALL_CLEAR]` is `true` and `DataStore[KEY_WIDGET_VERDICT]` contains an all-clear message (e.g., "You're good. Go live your day.")

**Given** any weather condition
**When** a mood line is generated
**Then** `DataStore[KEY_MOOD_LINE]` contains a human, conversational line appropriate to the conditions (e.g., "Honestly lovely today. Eat lunch outside.")

**Given** the Worker completes a successful write cycle
**When** content keys are written
**Then** `KEY_LAST_UPDATE_EPOCH` is written last; the widget uses this key to detect fresh data

**Given** last successful update was > 60 minutes ago
**When** the widget reads DataStore
**Then** the staleness indicator is displayed alongside the last-update timestamp

---

### Story 1.5: Home Screen Widget

As a free-tier user,
I want a home screen widget that shows the weather verdict in plain language with any relevant bring items and mood line,
So that I can glance at my home screen and know exactly what my day holds without opening the app.

**Acceptance Criteria:**

**Given** the widget is placed on the home screen and DataStore contains a fresh verdict
**When** the user glances at the widget in 4×2 size
**Then** it displays: verdict line (primary, large confident-weight text), bring chip(s) if warranted, best outdoor window if applicable, mood line — in that visual order

**Given** the widget is placed in 4×1 minimum size
**When** rendered
**Then** only the verdict line and staleness indicator are shown; all other elements are hidden

**Given** `DataStore[KEY_ALL_CLEAR]` is `true`
**When** the widget renders
**Then** it displays the all-clear message in a minimal, confident visual state — no empty-looking broken state

**Given** `DataStore[KEY_LAST_UPDATE_EPOCH]` is > 60 minutes ago
**When** the widget renders
**Then** a staleness indicator showing time since last update is displayed; the widget never silently shows old data as current

**Given** the widget is rendered
**When** inspected by TalkBack
**Then** the Glance root element has a `contentDescription` summarising the full widget state (verdict + bring items if any)

**Given** the widget is tapped
**When** the user taps anywhere on the widget
**Then** the app opens to the hourly detail view (Story 1.7)

**Given** the widget renders in light mode with a Clear weather condition
**When** AdaptiveSkyTheme is applied
**Then** the Adaptive Sky color tokens for "clear/light" are used — not Material You dynamic color

**Given** dark mode is enabled on the device
**When** the widget renders
**Then** the correct Adaptive Sky dark-mode palette is applied for the current weather condition

**Given** the widget code
**When** reviewed
**Then** no upgrade prompts, premium feature previews, or degraded states appear anywhere in `WeatherWidgetContent.kt`

**Given** the Glance composable
**When** reviewed
**Then** it reads only from DataStore via `WidgetStateReader`; no Repository or DAO calls exist in any widget composable

---

### Story 1.6: Onboarding & Permission Flow

As a new user,
I want to grant permissions and have a live working widget on my home screen within 60 seconds of first opening the app,
So that I can start trusting WeatherApp without any friction or confusion.

**Acceptance Criteria:**

**Given** a fresh install and first app launch
**When** the app opens
**Then** `OnboardingScreen` is displayed (not the app home) because `DataStore[KEY_HAS_COMPLETED_ONBOARDING]` is `false`

**Given** the onboarding screen
**When** the calendar permission rationale is displayed
**Then** it reads: "We use your calendar to tell you when weather matters to your plans." — before the system permission dialog appears

**Given** the user grants `ACCESS_COARSE_LOCATION`
**When** the permission is granted
**Then** the permission is secured for later use by `ForecastRefreshWorker`; `ACCESS_FINE_LOCATION` is never requested at any point

**Given** the user grants `READ_CALENDAR`
**When** both permissions are granted
**Then** `WorkManager` enqueues `ForecastRefreshWorker` for immediate one-time execution and the widget is pinned/instructed to appear on the home screen

**Given** the path from first app open to live widget on home screen
**When** timed on a standard device with network available
**Then** it completes in ≤ 60 seconds with no additional user prompts after the permission grants

**Given** `POST_NOTIFICATIONS` permission
**When** it is requested
**Then** it is requested only after the first widget render completes — not during the onboarding permission sequence

**Given** the user denies `ACCESS_COARSE_LOCATION`
**When** onboarding continues
**Then** the user is prompted to enter a home location manually; `ACCESS_FINE_LOCATION` is never requested

**Given** the user denies `READ_CALENDAR`
**When** onboarding continues
**Then** the app proceeds in weather-only mode; all free-tier widget features remain available; no in-app re-prompt is shown

**Given** `READ_CALENDAR` was previously granted and is revoked by the user in system settings
**When** the app detects this on next launch
**Then** the app reverts to weather-only mode without crashing, and all free-tier features remain functional

**Given** `DataStore[KEY_HAS_COMPLETED_ONBOARDING]` is `true`
**When** the app launches
**Then** `OnboardingScreen` is skipped and the app navigates directly to the main/settings screen

---

### Story 1.7: Hourly Detail View

As a user,
I want to tap the widget and see an hourly weather breakdown for today,
So that I can look up the detail behind the verdict when I want it, without it being the default view.

**Acceptance Criteria:**

**Given** the user taps the home screen widget
**When** the tap action fires
**Then** the app opens and `HourlyDetailBottomSheet` is displayed, showing today's hourly forecast

**Given** `HourlyDetailBottomSheet` is open
**When** the user views each hour row via `HourlyDetailRow`
**Then** each row displays: hour label, clothing-language verdict (primary, prominent), temperature in °C (secondary, smaller) — raw temperature is never the primary element

**Given** 48 hours of forecast data in Room
**When** the hourly view renders
**Then** it shows hours from the current hour through end of day; past hours are not shown

**Given** `HourlyDetailViewModel`
**When** it exposes state
**Then** it is typed as `StateFlow<UiState<List<HourlyDetailRow>>>` — never a raw nullable or bare list

**Given** the hourly bottom sheet is open
**When** the user swipes down to dismiss
**Then** the bottom sheet closes and the user is returned to the underlying screen

**Given** the device has TalkBack active
**When** the user navigates the hourly list
**Then** each row is announced with hour + verdict + temperature in logical reading order

---

### Story 1.8: Settings Screen & Mood Card Sharing

As a user,
I want a settings screen where I can configure preferences, access the premium upgrade, and share today's mood card,
So that I can customise the experience and spread the app to friends.

**Acceptance Criteria:**

**Given** `SettingsScreen` is opened via deliberate navigation
**When** reviewed
**Then** it contains: temperature unit toggle (°C/°F), notification preference toggle, premium upgrade entry point, and a "Share Today's Mood" action

**Given** the premium upgrade entry point in Settings
**When** viewed by a free-tier user
**Then** it is displayed as a calm, informational option — not a nag, not a locked-door visual; no upgrade prompt appears anywhere in the widget

**Given** the user taps "Share Today's Mood"
**When** the share action fires
**Then** Android's share sheet opens with the mood card content (mood line text + app attribution) ready to send via any installed sharing app

**Given** `SettingsViewModel`
**When** it exposes state
**Then** it is typed as `StateFlow<UiState<SettingsState>>` — never raw nullable

**Given** the user toggles the temperature unit
**When** the setting is saved
**Then** `DataStore[KEY_TEMP_UNIT]` is updated and the hourly detail view reflects the new unit on next render

**Given** `SettingsScreen`
**When** reviewed
**Then** it is accessible only through deliberate navigation — there is no path from the widget to the settings screen that surfaces premium prompts unexpectedly

---

## Epic 2: Smart Alert System

Users receive proactive confirmation-first alerts when the day looks clear, and change-triggered notifications only when a forecast materially changes — earning every interrupt or staying silent.

### Story 2.1: Alert State Machine & Room Schema

As a developer,
I want the alert state machine entities, DAOs, and enum defined in Room,
So that the system has a persistent, per-location record of alert state across every WorkManager cycle.

**Acceptance Criteria:**

**Given** `AlertState.kt`
**When** reviewed
**Then** it declares `enum class AlertState { UNCHECKED, CONFIRMED_CLEAR, ALERT_SENT, RESOLVED }`

**Given** `AlertStateRecord.kt` (Room entity)
**When** reviewed
**Then** it maps to table `alert_state_record` with columns `event_id` (String, primary key), `state` (AlertState), `confirmed_forecast_snapshot` (String — serialised threshold values at time of confirmation), `last_transition_at` (Long epoch)

**Given** `AlertStateDao`
**When** called
**Then** it supports `insertRecord(AlertStateRecord)`, `getByEventId(eventId): AlertStateRecord?`, and `resolveExpired(beforeEpoch)` (marks records RESOLVED where event end time has passed)

**Given** an alert state transition occurs
**When** the record is written
**Then** a new row is inserted with the updated state and `lastTransitionAt` timestamp — existing rows are never updated in place (append-only)

**Given** `AppDatabase`
**When** reviewed
**Then** it includes `AlertStateRecord` in its entities list; database version is incremented appropriately

---

### Story 2.2: Alert Evaluation Worker (Free Tier)

As a developer,
I want `AlertEvaluationWorker` to evaluate current forecast conditions against alert thresholds after each refresh cycle,
So that the system knows when to send a confirmation-first alert and when to stay silent.

**Acceptance Criteria:**

**Given** `AlertEvaluationWorker` is chained to run after `ForecastRefreshWorker`
**When** it runs
**Then** it reads the latest forecast from Room and the current `AlertStateRecord` for the day's location-based key

**Given** the forecast meets the all-clear threshold (no material adverse conditions)
**When** the current `AlertState` is `UNCHECKED`
**Then** the state transitions to `CONFIRMED_CLEAR`, the confirmed forecast snapshot is written to the record, and a confirmation-first notification is queued

**Given** the forecast meets all-clear threshold but `CONFIRMED_CLEAR` was already sent today
**When** the worker evaluates
**Then** no additional notification is queued — confirmation fires at most once per day per location

**Given** a previously `CONFIRMED_CLEAR` record exists and the forecast now shows a material change (≥ 20% precipitation probability shift OR wind speed crossing 25 mph)
**When** the worker evaluates
**Then** the state transitions to `ALERT_SENT` and a change-triggered notification is queued

**Given** the forecast does not meet all-clear threshold and no prior `CONFIRMED_CLEAR` exists
**When** the worker evaluates
**Then** the state remains `UNCHECKED`; no notification is queued

**Given** the event end time has passed
**When** `resolveExpired()` is called
**Then** the record transitions to `RESOLVED` and is excluded from future evaluation cycles

**Given** `AlertEvaluationWorker` code
**When** reviewed
**Then** it checks `POST_NOTIFICATIONS` permission before queuing any notification; if permission is denied, evaluation completes silently with no crash

---

### Story 2.3: Notification Delivery & Permission Flow

As a user,
I want to receive at most one daily weather alert that proactively confirms good news or warns of a genuine change,
So that I feel looked after rather than notified at — and can trust that every alert is worth reading.

**Acceptance Criteria:**

**Given** the app's first launch after `POST_NOTIFICATIONS` has not yet been requested
**When** the first widget render completes
**Then** the system `POST_NOTIFICATIONS` permission dialog is shown — not during onboarding

**Given** the user grants `POST_NOTIFICATIONS`
**When** a confirmation-first alert fires
**Then** it is delivered via the "Weather Alerts" notification channel with priority `CATEGORY_RECOMMENDATION`

**Given** a confirmation-first notification
**When** it is delivered
**Then** the content reads as good news (e.g., "You're clear today. Go live your day.") — not a warning

**Given** a change-triggered notification (free tier: location-level material change)
**When** it is delivered
**Then** the content identifies what changed and why it matters (e.g., "Rain moving in this afternoon — conditions changed since this morning.")

**Given** the user has denied `POST_NOTIFICATIONS`
**When** the alert evaluation determines a notification should fire
**Then** no notification is delivered; the app does not re-prompt; widget-only mode continues silently

**Given** the notification channel "Weather Alerts"
**When** created
**Then** it is a single channel, user-dismissable in Android system settings, with importance `IMPORTANCE_DEFAULT` and `CATEGORY_RECOMMENDATION`

**Given** free-tier alert logic
**When** reviewed
**Then** at most one alert fires per day per location, regardless of how many WorkManager cycles run

---

## Epic 3: Premium Calendar Intelligence

Premium subscribers see event-specific weather on the widget before they think to check, receive alerts calibrated to event stakes, get travel pre-load for non-home locations, and see transparent conflict surfacing when outdoor events overlap — all driven silently from calendar access granted once.

### Story 3.1: Premium Subscription & Billing

As a user,
I want to subscribe to WeatherApp Premium and have the app immediately unlock calendar-powered features,
So that I can access proactive event intelligence without any delay after subscribing.

**Acceptance Criteria:**

**Given** `BillingRepository` and `BillingClientWrapper`
**When** a successful subscription purchase completes via Google Play Billing 7.1.1
**Then** `DataStore[KEY_IS_PREMIUM]` is set to `true` and `DataStore[KEY_LAST_BILLING_CHECK]` is updated with the current epoch

**Given** `DataStore[KEY_IS_PREMIUM]` is `true`
**When** `ForecastRefreshWorker` starts its cycle
**Then** it reads `isPremium` from DataStore at the very start of `doWork()` and gates all premium paths (CalendarScanWorker enqueue) behind this check

**Given** `DataStore[KEY_IS_PREMIUM]` is `false`
**When** `ForecastRefreshWorker` runs
**Then** `CalendarScanWorker` is not enqueued; no calendar queries are made; no premium UI appears on the widget

**Given** the Google Play Billing client connection fails
**When** `BillingRepository` attempts to verify subscription state
**Then** it reads the last cached `DataStore[KEY_IS_PREMIUM]` value without crashing; it does not revert premium status on a transient connection failure

**Given** the premium upgrade entry point in `SettingsScreen`
**When** the user taps it
**Then** the Google Play in-app purchase flow is launched for the $7.99/year subscription SKU

**Given** the user's subscription lapses or is cancelled
**When** `BillingRepository` detects this on the next billing check
**Then** `DataStore[KEY_IS_PREMIUM]` is set to `false` and the widget reverts to free-tier display on the next WorkManager cycle

---

### Story 3.2: Calendar Data Layer

As a developer,
I want `CalendarRepository` to safely query Android's CalendarContract and surface upcoming events as domain models,
So that the calendar scan worker has reliable, sanitised event data to work with even when calendar data is malformed or permission is revoked mid-session.

**Acceptance Criteria:**

**Given** `CalendarRepository.getUpcomingEvents(daysAhead: Int)`
**When** called with `READ_CALENDAR` permission granted
**Then** it returns a `List<CalendarEvent>` covering the next `daysAhead` days, each with `eventId`, `title`, `startEpoch`, `endEpoch`, `location` (nullable)

**Given** `CalendarRepository.getUpcomingEvents()`
**When** the `CalendarContract` query returns a row with a non-ASCII or malformed event title
**Then** the title is sanitised to a valid UTF-8 string; the event is included in results; the worker does not stall or crash

**Given** `CalendarRepository.getUpcomingEvents()`
**When** `READ_CALENDAR` permission is revoked between the permission check and the cursor query
**Then** the `SecurityException` is caught, a `Timber.w` log is emitted, and an empty list is returned — no crash, no propagated exception

**Given** any `CalendarContract` query
**When** reviewed
**Then** it is wrapped in `try { ... } catch (e: SecurityException) { ... } catch (e: Exception) { ... }` — no unguarded query exists anywhere in `CalendarRepository`

**Given** `CalendarEvent.kt` domain model
**When** reviewed
**Then** it contains `eventId: String`, `title: String`, `startEpoch: Long`, `endEpoch: Long`, `location: String?`

**Given** `CalendarEventForecast.kt` (Room entity)
**When** reviewed
**Then** it maps to table `calendar_event_forecast` with `event_id` as primary key, storing the last weather snapshot and widget display string for that event

**Given** `CalendarEventForecastDao`
**When** called
**Then** it supports `upsert(CalendarEventForecast)`, `getByEventId(eventId): CalendarEventForecast?`, and `deleteExpired(beforeEpoch)`

---

### Story 3.3: Calendar Scan Worker & Proactive Widget

As a premium user,
I want the widget to shift to show event-specific weather before I even think to check,
So that I experience the "how did it know?" moment that makes the premium tier feel genuinely intelligent.

**Acceptance Criteria:**

**Given** `DataStore[KEY_IS_PREMIUM]` is `true`
**When** `ForecastRefreshWorker` completes successfully
**Then** it enqueues `CalendarScanWorker` as a chained one-time request

**Given** `CalendarScanWorker` runs
**When** `CalendarRepository.getUpcomingEvents(7)` returns events
**Then** it identifies events with outdoor potential using title keyword signals (e.g., "BBQ", "run", "match", "picnic", "game", "walk") and event duration >= 30 minutes

**Given** a single upcoming outdoor-potential event is identified
**When** `CalendarScanWorker` completes
**Then** `DataStore[KEY_WIDGET_VERDICT]` is overwritten with an event-specific string (e.g., "Your BBQ (12pm) is clear.") and the widget updates to display it

**Given** two or more outdoor-potential events overlap in time
**When** `CalendarScanWorker` detects the conflict
**Then** `DataStore[KEY_WIDGET_VERDICT]` is set to a conflict message (e.g., "2 outdoor events at 3pm - Light rain - Check both.") and the worker never silently picks one event

**Given** a calendar event has a non-home `location` field
**When** `CalendarScanWorker` processes it
**Then** it passes that location's coordinates (snapped via `snapToGrid()`) to `WeatherRepository` to pre-fetch weather for that location; the result is written to `CalendarEventForecast` in Room

**Given** a calendar event location pre-fetch
**When** the coordinates are constructed
**Then** `snapToGrid()` is applied before any network call — raw event location coordinates never reach `WeatherApi`

**Given** a user modifies or removes a calendar event
**When** the next `CalendarScanWorker` cycle runs (within 30 minutes via the chained WorkManager schedule)
**Then** `DataStore[KEY_WIDGET_VERDICT]` reflects the updated event state and the widget re-renders accordingly

**Given** `CalendarScanWorker` completes
**When** reviewed
**Then** it calls `WeatherWidget.update()` after writing to DataStore, ensuring the widget reflects the latest state without waiting for the next Glance refresh cycle

---

### Story 3.4: Change-Triggered Event Alerts

As a premium user,
I want to receive an alert only when the forecast for one of my specific calendar events changes materially after I've already been told it looks clear,
So that I can plan confidently knowing I'll be warned if something genuinely changes — without being spammed.

**Acceptance Criteria:**

**Given** `AlertEvaluationWorker` runs after `CalendarScanWorker` for a premium user
**When** it evaluates a calendar event with an existing `CONFIRMED_CLEAR` `AlertStateRecord`
**Then** it compares the current forecast snapshot against the stored `confirmed_forecast_snapshot` using the thresholds: >= 20% precipitation probability shift OR wind speed crossing 25 mph within the event's time window

**Given** the comparison shows a material change
**When** the event's `AlertState` is `CONFIRMED_CLEAR`
**Then** the state transitions to `ALERT_SENT`, a per-event change-triggered notification is queued, and the new forecast snapshot is stored in the record

**Given** a premium change-triggered notification
**When** delivered
**Then** it identifies the specific event by name and time (e.g., "Your Run (6:30am) - Rain now likely. Conditions changed since last check.") — never a generic location-based message

**Given** an event with title keyword signals indicating high stakes (e.g., "marathon", "wedding", "match")
**When** `AlertEvaluationWorker` determines the monitoring window
**Then** the alert lead time is extended beyond the 2-hour minimum, scaled to the inferred importance of the event

**Given** any change-triggered premium alert
**When** it fires
**Then** it fires no earlier than 2 hours before the event's `startEpoch`; alerts for events starting in < 2 hours are suppressed

**Given** a premium alert evaluation
**When** the `AlertState` is already `ALERT_SENT`
**Then** no duplicate notification is queued for the same material change; the state machine does not re-fire on subsequent cycles unless conditions improve back to `CONFIRMED_CLEAR` and then deteriorate again

**Given** a new `AlertStateRecord` for a premium calendar event
**When** first created by `AlertEvaluationWorker`
**Then** it uses the `eventId` from `CalendarEvent` as the primary key — not a location-based key — ensuring per-event tracking independent of location

**Given** `POST_NOTIFICATIONS` permission is denied
**When** `AlertEvaluationWorker` runs for a premium user
**Then** state machine transitions still occur (records update correctly) but no notification is delivered; no crash occurs
