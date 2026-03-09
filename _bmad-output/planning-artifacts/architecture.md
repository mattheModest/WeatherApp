---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8]
lastStep: 8
status: 'complete'
completedAt: '2026-03-08'
inputDocuments:
  - '_bmad-output/planning-artifacts/prd.md'
  - '_bmad-output/planning-artifacts/ux-design-specification.md'
workflowType: 'architecture'
project_name: 'WeatherApp'
user_name: 'Lafayette'
date: '2026-03-08'
---

# Architecture Decision Document

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each architectural decision together._

## Project Context Analysis

### Requirements Overview

**Functional Requirements:**

18 FRs across three tiers:

- **Free tier (FR-001–008):** Clothing-language verdict, contextual bring list, best outdoor window, all-clear state, mood line + shareable card, hourly detail tap-through, confirmation-first alerts, 60-second onboarding
- **Premium tier (FR-009–013):** Proactive calendar event widget shift, change-triggered alerts, stakes-scaled alert windows, silent travel pre-load (non-home event locations), calendar conflict detection and transparent surfacing
- **Reliability (FR-014–018):** CalendarContract malformed data handling, staleness signal (never silently stale), widget update within 30 min of calendar change, no upgrade prompts on widget, graceful permission denied/revoked degradation

**Architectural weight by FR group:**
- Free tier FRs are primarily *display logic* — verdict generation, bring list thresholds, copy tone
- Premium FRs are *background intelligence* — calendar scanning, event monitoring, proactive state shifts
- Reliability FRs are *fault tolerance boundaries* — every external integration (CalendarContract, Open-Meteo, permissions) must degrade gracefully

**Non-Functional Requirements:**

| NFR | Requirement | Architectural implication |
|---|---|---|
| NFR-001 | Widget freshness ≤ 30 min | WorkManager periodic refresh every 30 min; DataStore write must complete before widget reads |
| NFR-002 | Onboarding ≤ 60 seconds | First widget render must not block on network; location inference + initial API call must complete within budget |
| NFR-003 | Zero false-positive change alerts (30 days) | Alert state machine must be conservative and deterministic; 20% precipitation shift or 25mph wind threshold |
| NFR-004 | WorkManager invisible in battery stats | Minimal work per cycle; no redundant API calls; efficient CalendarContract queries |
| NFR-005 | ≤ 72,000 Open-Meteo calls/month at 1,000 MAU | Location-cluster caching required; likely server-side on Cloudflare Workers |

**Scale & Complexity:**

- Complexity level: **Low-medium** (per PRD classification, confirmed by FR count and domain)
- Primary domain: Android mobile + background processing + thin cloud proxy
- No multi-tenancy; single user per device; no real-time (polling-based every 30 min)
- ~5 major subsystems: Widget engine · Calendar integration · Weather data pipeline · Alert state machine · Premium/billing

### Technical Constraints & Dependencies

| Constraint | Detail |
|---|---|
| **Android API 34 minimum** | Jetpack Compose, Glance, WorkManager, CalendarContract all available; no legacy compat layers needed |
| **Kotlin + Jetpack Compose** | UI and widget implementation language fixed |
| **Open-Meteo** | Free weather data API; no key required; usage must stay within budget via caching |
| **Cloudflare Workers** | Thin proxy layer; handles location-cluster caching and shields Open-Meteo from per-device call volume |
| **Google Play Billing** | Premium subscription at $7.99/year; billing state must be queryable from WorkManager (background) context |
| **Infrastructure budget: ~$23/month** | Open-Meteo $10 + Cloudflare Workers $5 + Google Play amortized $8; architecture must not add cost |
| **Solo developer maintainability** | Architecture must be debuggable by one person; avoid operational complexity |

### Cross-Cutting Concerns Identified

1. **Permission state** — READ_CALENDAR, ACCESS_COARSE_LOCATION, POST_NOTIFICATIONS affect widget display, calendar features, alert delivery, and must be checked defensively at every call site without crashing
2. **Weather state** — current conditions + 3-day forecast shared across widget rendering, alert logic, and calendar event monitoring; must be consistent and cacheable
3. **Premium subscription state** — gates calendar features, change-triggered alerts, and widget modes; must be synchronously queryable from WorkManager without UI interaction
4. **Network/offline handling** — widget staleness signals, WorkManager retry on network restoration, API failure graceful degradation
5. **CalendarContract exception handling** — malformed data (non-ASCII, overlapping events, revoked mid-session) must be caught and handled at every call site; the app must never crash due to calendar data

## Starter Template Evaluation

### Primary Technology Domain

**Android Native Mobile** — Kotlin + Jetpack Compose. No cross-platform frameworks considered; PRD specifies Kotlin/Jetpack Compose explicitly, and CalendarContract + Jetpack Glance integration requires native Android APIs.

### Starter Options Considered

| Option | Verdict |
|---|---|
| React Native / Expo | Eliminated — CalendarContract and Jetpack Glance require native Android APIs |
| Flutter | Eliminated — Glance has no Flutter equivalent |
| **Android Studio "Empty Activity" (Compose)** | ✅ Correct foundation |
| Third-party Kotlin boilerplates | Not used — manual dependency configuration is standard practice for intentional dependency management |

### Selected Starter: Android Studio Empty Activity (Compose) + Manual Dependency Stack

**Initialization:**

```
Android Studio → New Project → Empty Activity
  Language: Kotlin
  Minimum SDK: API 34 (Android 14)
  Build configuration language: Kotlin DSL
```

**Gradle dependency stack (verified stable versions, March 2026):**

```kotlin
// build.gradle.kts (project level)
plugins {
    id("com.android.application") version "9.1.0" apply false
    id("org.jetbrains.kotlin.android") version "2.1.20" apply false
    id("com.google.dagger.hilt.android") version "2.56" apply false
    id("com.google.devtools.ksp") version "2.1.20-1.0.29" apply false
}

// build.gradle.kts (app level) — key dependencies
val composeBom = platform("androidx.compose:compose-bom:2026.02.01")

dependencies {
    // Compose + Material3
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.10.1")

    // Jetpack Glance (widget)
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.glance:glance-material3:1.1.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.hilt:hilt-work:1.2.0")

    // Hilt (DI)
    implementation("com.google.dagger:hilt-android:2.56")
    ksp("com.google.dagger:hilt-android-compiler:2.56")

    // DataStore (widget state + user preferences)
    implementation("androidx.datastore:datastore-preferences:1.1.2")

    // Room (forecast cache)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Network (Cloudflare Workers proxy)
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Google Play Billing (premium subscription)
    implementation("com.android.billingclient:billing-ktx:7.1.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.work:work-testing:2.10.0")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

### Architectural Decisions Provided by This Stack

| Layer | Decision |
|---|---|
| **Language & Runtime** | Kotlin 2.1.20, JVM target 17, coroutines via kotlinx.coroutines |
| **UI** | Jetpack Compose via BOM 2026.02.01 + Material3; no XML layouts |
| **Widget** | Jetpack Glance 1.1.1 — separate composable tree, DataStore-only communication |
| **Background** | WorkManager 2.10.0 + HiltWorker — CoroutineWorker for refresh, calendar scan, alert eval |
| **DI** | Hilt 2.56 — injected into Worker, ViewModel, Repository |
| **Local storage (state)** | DataStore Preferences — widget display state, user settings, last-known verdict |
| **Local storage (cache)** | Room 2.6.1 — structured forecast cache, calendar event forecast records |
| **Networking** | Retrofit 3.0.0 → Cloudflare Workers proxy; no direct Open-Meteo device calls |
| **Billing** | Google Play Billing 7.1.1 — subscription state cached locally; queryable from WorkManager |
| **Build system** | Gradle Kotlin DSL + KSP for annotation processing |

**Note:** Project initialization and Gradle configuration is the first implementation story.

## Core Architectural Decisions

### Decision Priority Analysis

**Critical Decisions (Block Implementation):**
- App architecture pattern → MVVM + Repository + StateFlow
- Data storage split → DataStore (state/prefs) + Room (structured cache + alert records)
- Alert state machine design → 4-state per-event model in Room
- Weather data pipeline → Cloudflare Worker + KV + 0.1° grid clustering with device-side coordinate snapping

**Important Decisions (Shape Architecture):**
- Module structure → Single-module for v1
- Location privacy approach → Device snaps coordinates before transmission; Worker never receives raw GPS

**Deferred Decisions (Post-MVP):**
- Multi-module extraction (`:widget` module) — if Glance compilation becomes a bottleneck
- Temperature display in widget — Growth Feature; v1 widget is verdict-only, tap-through shows temperature

### Data Architecture

**Decision:** MVVM + Repository + StateFlow

```
UI (Composable) ← StateFlow ← ViewModel ← Repository ← [DataStore / Room / Network]
```

WorkManager sits outside this chain — it writes to DataStore/Room directly. The Repository observes via Flow. The Glance widget reads DataStore directly without going through ViewModel.

**Decision:** DataStore vs Room split

| Data | Storage | Rationale |
|---|---|---|
| Current widget verdict, bring list, mood line, weather state | DataStore | Glance reads DataStore directly; fast key-value access |
| Last update timestamp, staleness flag | DataStore | Needed by widget without Room query |
| User settings (units, notifications on/off) | DataStore | Simple preferences |
| `isPremium`, `lastBillingCheck` | DataStore | Synchronously queryable from WorkManager |
| `hasCompletedOnboarding` | DataStore | Simple boolean |
| Hourly forecast rows (48h cache) | Room | Structured, queryable by time window |
| Calendar event forecast records | Room | Structured, needs event-ID keyed lookup |
| Alert state machine records | Room | Per-event state tracking across WorkManager runs |

### Authentication & Security

**Decision:** No user authentication in v1. Users are identified by device only. Premium state is validated via Google Play Billing — no custom auth server required.

**Decision:** Location privacy — device-side coordinate snapping
- Raw GPS coordinates are snapped to the nearest 0.1° grid cell (~11km) **on-device** before any network request is constructed
- The Cloudflare Worker receives only `{lat_grid, lon_grid, date}` — precise coordinates never leave the device
- This satisfies the PRD's prominent disclosure requirement: "never uploaded, stored on servers, or shared"
- KV cache key: `forecast:{lat_grid}:{lon_grid}:{date}` — no PII at any point in the pipeline

### API & Communication Patterns

**Decision:** Cloudflare Worker + KV location-cluster caching

Worker responsibilities:
1. Receive `{lat_grid, lon_grid, date}` — already anonymised by device
2. Check KV for `forecast:{lat_grid}:{lon_grid}:{date}` — return if fresh (< 30 min TTL)
3. On cache miss: call Open-Meteo, store in KV with 30-min TTL, return to device
4. Result: 1,000 MAU in same city = ~1–5 unique clusters → well within 72k/month budget

Tech: Cloudflare Worker (TypeScript) + Cloudflare KV. Deployed via Wrangler CLI.

**Decision:** Alert State Machine

```
UNCHECKED
  → CONFIRMED_CLEAR   (all-clear threshold met; confirmation notification sent)
  → ALERT_SENT        (material change: ≥20% precip shift OR wind >25mph within event window)
  → CONFIRMED_CLEAR   (conditions improved back to clear)
  → RESOLVED          (event end time passed; record archived)
```

Stored in Room as `AlertStateRecord(eventId, state, confirmedForecastSnapshot, lastTransitionAt)`.
- **Free tier:** confirmation-first fires on UNCHECKED → CONFIRMED_CLEAR, at most once per day
- **Premium:** change-triggered fires on CONFIRMED_CLEAR → ALERT_SENT; minimum 2h lead time before event start
- One record per calendar event per day; evaluated on every WorkManager run

### Frontend Architecture

**Decision:** MVVM + Repository + StateFlow (Android/Compose standard)
- Single-module structure for v1; no Gradle multi-module complexity
- `LocalWeatherState` CompositionLocal propagates weather state to themed components
- `WeatherDesignTokens` shared object bridges Compose and Glance token systems
- Dynamic color (Material You) disabled; Adaptive Sky palette is product-controlled

### Infrastructure & Deployment

**Decision:** Single-module Android app + Cloudflare Worker microservice
- App: Google Play Store distribution; CI via GitHub Actions (lint + test + build)
- Worker: Cloudflare Pages/Workers; deployed via Wrangler; no separate staging environment for v1
- No custom backend; no database server; infrastructure cost ceiling ~$23/month

### Decision Impact Analysis

**Implementation Sequence:**
1. Gradle project setup + dependency configuration
2. DataStore schema + Room schema (entities, DAOs)
3. Repository layer + Hilt modules
4. Cloudflare Worker (TypeScript) + KV setup
5. WorkManager worker(s) — refresh cycle, calendar scan, alert evaluation
6. Glance widget composables + DataStore binding
7. Alert state machine implementation
8. Compose UI — onboarding, settings screen, hourly bottom sheet
9. Google Play Billing integration
10. CalendarContract integration (premium)

**Cross-Component Dependencies:**
- WorkManager → Room (writes forecast cache, alert records) → Repository (observes via Flow) → ViewModel → UI
- WorkManager → DataStore (writes widget display state) → Glance widget (reads directly)
- Billing state (DataStore) → WorkManager (reads at start of each cycle to determine premium features)
- Permission state (runtime check) → WorkManager (gates calendar scan + alert delivery)

## Implementation Patterns & Consistency Rules

### Naming Patterns

**Kotlin / Android Code:**
- Classes: `PascalCase` — `WeatherRepository`, `ForecastRefreshWorker`, `AlertStateRecord`
- Functions + properties: `camelCase` — `fetchForecast()`, `isPremium`, `lastUpdateAt`
- Constants + DataStore keys: `SCREAMING_SNAKE_CASE` in companion objects — `KEY_WIDGET_VERDICT`, `ALERT_THRESHOLD_PRECIP`
- Test classes: `ClassUnderTestTest` — `WeatherRepositoryTest`, `AlertStateMachineTest`

**Room (Database):**
- Table names: `snake_case` singular — `forecast_hour`, `alert_state_record`, `calendar_event_forecast`
- Column names: `snake_case` — `event_id`, `last_transition_at`, `lat_grid`
- Entity classes: `PascalCase` matching table — `ForecastHour`, `AlertStateRecord`
- DAO interfaces: `PascalCase` + `Dao` suffix — `ForecastDao`, `AlertStateDao`

**DataStore Keys:**
- Preference keys: `SCREAMING_SNAKE_CASE` string literals — `"widget_verdict"`, `"is_premium"`, `"last_update_epoch"`
- Defined centrally in `PreferenceKeys.kt` — never inline string literals at call sites

**Cloudflare Worker:**
- KV keys: `forecast:{lat_grid}:{lon_grid}:{date}` — colons as separators, date as `YYYY-MM-DD`
- Request/response JSON fields: `snake_case` — `lat_grid`, `lon_grid`, `hourly_forecasts`
- Worker file: `worker.ts`; entry point export: `default { fetch }`

**Files & Directories:**
- Kotlin source files: `PascalCase.kt` matching the primary class/interface declared within
- No `Utils.kt` god files — utilities co-located with their domain (e.g., `CoordinateUtils.kt` in `data/location/`)

### Structure Patterns

**Package Organization (feature-first within single module):**

```
com.weatherapp/
  ui/
    widget/          — Glance composables only
    onboarding/      — OnboardingScreen + ViewModel
    hourly/          — HourlyDetailBottomSheet + ViewModel
    settings/        — SettingsScreen + ViewModel
    theme/           — WeatherDesignTokens, AdaptiveSkyTheme, WeatherState enum
  data/
    weather/         — WeatherRepository, ForecastApi (Retrofit interface)
    calendar/        — CalendarRepository, CalendarContract queries
    billing/         — BillingRepository, BillingClientWrapper
    location/        — LocationRepository, CoordinateUtils
    datastore/       — PreferenceKeys, DataStoreExtensions
    db/              — AppDatabase, entity classes, DAOs
  worker/
    ForecastRefreshWorker.kt
    CalendarScanWorker.kt
    AlertEvaluationWorker.kt
  di/                — Hilt modules (AppModule, DatabaseModule, NetworkModule, WorkerModule)
  model/             — domain models shared across layers (WeatherCondition, AlertState enum, etc.)
```

**Test Organization:**
- Unit tests: `src/test/` mirroring source package structure
- Instrumented tests: `src/androidTest/` for Room, DataStore, WorkManager integration tests
- Test class co-location with source: `WeatherRepository` → `WeatherRepositoryTest` in same relative package

### Format Patterns

**Cloudflare Worker API Response:**

```json
{
  "lat_grid": 37.8,
  "lon_grid": -122.4,
  "fetched_at": "2026-03-08T14:00:00Z",
  "hourly_forecasts": [
    {
      "hour_epoch": 1741435200,
      "temperature_c": 14.2,
      "precipitation_probability": 0.15,
      "wind_speed_kmh": 18.0,
      "weather_code": 1
    }
  ]
}
```

- All timestamps: ISO 8601 UTC strings at the Worker boundary; converted to epoch `Long` on device
- Temperature: always Celsius from Worker; device converts to display unit per user preference
- No envelope wrapper (`data:`, `error:`) — HTTP status codes carry success/failure signal; error body is plain `{"message": "..."}` string

**UiState Sealed Class (mandatory pattern for all ViewModels):**

```kotlin
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val cause: Throwable? = null) : UiState<Nothing>()
}
```

All `StateFlow` in ViewModels must be typed `StateFlow<UiState<T>>` — never raw nullable or bare data class.

**Alert State Enum:**

```kotlin
enum class AlertState { UNCHECKED, CONFIRMED_CLEAR, ALERT_SENT, RESOLVED }
```

Transitions are append-only in Room — never update a row in place; insert a new record with updated state + `lastTransitionAt`.

### Communication Patterns

**WorkManager → DataStore → Glance:**
- Workers write display-ready strings to DataStore (not raw data); the widget reads and renders without transformation
- Widget NEVER calls a Repository or DAO — DataStore only
- Key write order: update content keys first, then `last_update_epoch`; widget reads `last_update_epoch` to detect fresh data

**Repository → ViewModel:**
- Repositories expose `Flow<T>` for observable data; `suspend fun` for one-shot mutations
- ViewModels collect via `viewModelScope.launch` + `stateIn(SharingStarted.WhileSubscribed(5_000))`
- No LiveData anywhere — StateFlow only

**Permission Checks:**
- All permission checks use `ContextCompat.checkSelfPermission()` — never assume a permission is granted between calls
- Revocation mid-session must be handled at every CalendarContract call site with `try/catch(SecurityException)`

### Process Patterns

**Error Handling:**

```kotlin
// Repository pattern — never let network exceptions propagate raw
suspend fun fetchForecast(lat: Double, lon: Double): Result<List<ForecastHour>> = runCatching {
    api.getForecast(lat.snapToGrid(), lon.snapToGrid())
}

// WorkManager — return Result.retry() on transient failure; Result.failure() on permanent
override suspend fun doWork(): Result {
    return try {
        weatherRepository.refresh()
        Result.success()
    } catch (e: IOException) {
        if (runAttemptCount < 3) Result.retry() else Result.failure()
    } catch (e: Exception) {
        Timber.e(e, "Unrecoverable worker failure")
        Result.failure()
    }
}
```

**Logging:**
- `Timber.d/i/w/e(...)` everywhere — never `Log.*` directly
- Timber planted in `Application.onCreate()` in debug builds only
- Log tags are implicit via Timber — never pass a manual tag string

**Coordinate Snapping (mandatory — enforced at network boundary):**

```kotlin
fun Double.snapToGrid(cellDegrees: Double = 0.1): Double =
    (this / cellDegrees).roundToInt() * cellDegrees

// Usage — always snap before constructing any network request
val latGrid = rawLat.snapToGrid()
val lonGrid = rawLon.snapToGrid()
```

**Agents MUST NEVER pass raw GPS coordinates to any network call.** Snapping happens in `LocationRepository` before the value is handed to `WeatherRepository`.

**CalendarContract Safety:**

```kotlin
// Every CalendarContract query wrapped — SecurityException + general Exception
try {
    contentResolver.query(CalendarContract.Events.CONTENT_URI, ...)?.use { cursor ->
        // process cursor
    }
} catch (e: SecurityException) {
    Timber.w(e, "Calendar permission revoked mid-session")
    // degrade gracefully — return empty list, do not crash
} catch (e: Exception) {
    Timber.e(e, "Unexpected CalendarContract failure")
}
```

**Staleness Signal:**
- Widget always displays `lastUpdateAt` timestamp; if `lastUpdateAt` is > 60 min ago, display staleness indicator
- WorkManager sets staleness flag in DataStore before attempting refresh; clears it on success
- Widget NEVER silently shows stale data without the timestamp

### Enforcement Guidelines

**All AI Agents MUST:**

1. Use `Timber` — never `Log.*` or `println`
2. Snap coordinates via `.snapToGrid()` before any network call — no exceptions
3. Wrap every CalendarContract query in `try/catch(SecurityException)`
4. Type all ViewModel state as `StateFlow<UiState<T>>`
5. Write display-ready strings to DataStore from Workers — not raw model objects
6. Use `snake_case` for Room table/column names, `SCREAMING_SNAKE_CASE` for DataStore keys
7. Define DataStore keys only in `PreferenceKeys.kt` — no inline string literals
8. Return `Result<T>` from Repository methods — no raw throws across layer boundaries
9. Check `isPremium` from DataStore at the start of every WorkManager cycle before executing premium paths
10. Never call Repository or DAO from a Glance composable — DataStore reads only

**Anti-Patterns:**

- ❌ `Log.d("TAG", "message")` → ✅ `Timber.d("message")`
- ❌ `api.getForecast(rawLat, rawLon)` → ✅ `api.getForecast(rawLat.snapToGrid(), rawLon.snapToGrid())`
- ❌ `var state: WeatherData? = null` in ViewModel → ✅ `StateFlow<UiState<WeatherData>>`
- ❌ Querying CalendarContract without try/catch → ✅ Always wrap with SecurityException handler
- ❌ Writing `Room` entity objects directly to DataStore → ✅ Serialize to display string first
- ❌ Calling `repository.fetchForecast()` from a Glance composable → ✅ Read from DataStore keys only
- ❌ Hardcoded `"widget_verdict"` string at call site → ✅ `PreferenceKeys.WIDGET_VERDICT`

## Project Structure & Boundaries

### Complete Project Directory Structure

```
WeatherApp/
├── .github/
│   └── workflows/
│       └── ci.yml                    # lint + test + build on PR
├── cloudflare-worker/
│   ├── package.json
│   ├── wrangler.toml                 # Worker name, KV binding, route
│   ├── tsconfig.json
│   └── src/
│       └── worker.ts                 # Single-file Worker: fetch handler + KV logic
├── app/
│   ├── build.gradle.kts              # App-level dependencies (as documented in Step 2)
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   └── java/com/weatherapp/
│   │   │       ├── WeatherApp.kt             # Application class — Timber + Hilt
│   │   │       ├── MainActivity.kt           # Single activity; hosts Compose NavHost
│   │   │       │
│   │   │       ├── di/
│   │   │       │   ├── AppModule.kt          # DataStore, Retrofit, BillingClient bindings
│   │   │       │   ├── DatabaseModule.kt     # Room AppDatabase, DAO bindings
│   │   │       │   ├── NetworkModule.kt      # Retrofit + OkHttp + LoggingInterceptor
│   │   │       │   └── WorkerModule.kt       # HiltWorkerFactory binding
│   │   │       │
│   │   │       ├── model/
│   │   │       │   ├── WeatherCondition.kt   # Sealed class: Clear, Overcast, Rain, Storm
│   │   │       │   ├── AlertState.kt         # Enum: UNCHECKED, CONFIRMED_CLEAR, ALERT_SENT, RESOLVED
│   │   │       │   ├── BringItem.kt          # Data class: label, iconResId
│   │   │       │   └── WidgetDisplayState.kt # Data class written to DataStore; read by Glance
│   │   │       │
│   │   │       ├── data/
│   │   │       │   ├── datastore/
│   │   │       │   │   ├── PreferenceKeys.kt          # All DataStore key constants — single source of truth
│   │   │       │   │   └── DataStoreExtensions.kt     # suspend fns: readWidgetState(), writeWidgetState()
│   │   │       │   │
│   │   │       │   ├── db/
│   │   │       │   │   ├── AppDatabase.kt             # Room DB — version 1; includes all entities
│   │   │       │   │   ├── entity/
│   │   │       │   │   │   ├── ForecastHour.kt        # Table: forecast_hour
│   │   │       │   │   │   ├── AlertStateRecord.kt    # Table: alert_state_record
│   │   │       │   │   │   └── CalendarEventForecast.kt # Table: calendar_event_forecast
│   │   │       │   │   └── dao/
│   │   │       │   │       ├── ForecastDao.kt         # insert, queryByTimeWindow, deleteExpired
│   │   │       │   │       ├── AlertStateDao.kt       # insertRecord, getByEventId, resolveExpired
│   │   │       │   │       └── CalendarEventForecastDao.kt
│   │   │       │   │
│   │   │       │   ├── weather/
│   │   │       │   │   ├── WeatherRepository.kt       # Fetches → Room cache; exposes Flow<List<ForecastHour>>
│   │   │       │   │   ├── WeatherApi.kt              # Retrofit interface → Cloudflare Worker
│   │   │       │   │   └── dto/
│   │   │       │   │       ├── ForecastResponse.kt    # Maps Worker JSON → domain model
│   │   │       │   │       └── HourlyForecastDto.kt
│   │   │       │   │
│   │   │       │   ├── location/
│   │   │       │   │   ├── LocationRepository.kt      # Coarse location → snap → expose snapped pair
│   │   │       │   │   └── CoordinateUtils.kt         # snapToGrid() extension function
│   │   │       │   │
│   │   │       │   ├── calendar/
│   │   │       │   │   ├── CalendarRepository.kt      # CalendarContract queries; SecurityException handling
│   │   │       │   │   └── CalendarEvent.kt           # Domain model: eventId, title, startEpoch, endEpoch, location
│   │   │       │   │
│   │   │       │   └── billing/
│   │   │       │       ├── BillingRepository.kt       # BillingClient wrapper; writes isPremium to DataStore
│   │   │       │       └── BillingClientWrapper.kt    # BillingClient lifecycle management
│   │   │       │
│   │   │       ├── worker/
│   │   │       │   ├── ForecastRefreshWorker.kt       # Periodic 30-min; fetch + write DataStore + Room
│   │   │       │   ├── CalendarScanWorker.kt          # Premium only; scans next 7 days of events
│   │   │       │   └── AlertEvaluationWorker.kt       # Chained after CalendarScan; runs state machine
│   │   │       │
│   │   │       ├── ui/
│   │   │       │   ├── theme/
│   │   │       │   │   ├── WeatherDesignTokens.kt     # Adaptive Sky color tokens (shared Compose + Glance)
│   │   │       │   │   ├── AdaptiveSkyTheme.kt        # MaterialTheme wrapper; switches token set by WeatherCondition
│   │   │       │   │   └── Type.kt                    # Typography scale
│   │   │       │   │
│   │   │       │   ├── widget/
│   │   │       │   │   ├── WeatherWidget.kt            # GlanceAppWidget subclass
│   │   │       │   │   ├── WeatherWidgetContent.kt     # Glance composable: verdict + bring chips + mood line
│   │   │       │   │   ├── WeatherWidgetReceiver.kt    # GlanceAppWidgetReceiver
│   │   │       │   │   └── WidgetStateReader.kt        # DataStore → WidgetDisplayState; used in update()
│   │   │       │   │
│   │   │       │   ├── onboarding/
│   │   │       │   │   ├── OnboardingScreen.kt        # 60-second flow; location + notification permission
│   │   │       │   │   └── OnboardingViewModel.kt
│   │   │       │   │
│   │   │       │   ├── hourly/
│   │   │       │   │   ├── HourlyDetailBottomSheet.kt # Tap-through; verdict per hour + temperature secondary
│   │   │       │   │   ├── HourlyDetailViewModel.kt
│   │   │       │   │   └── HourlyDetailRow.kt         # Single row composable: verdict primary, temp secondary
│   │   │       │   │
│   │   │       │   └── settings/
│   │   │       │       ├── SettingsScreen.kt
│   │   │       │       └── SettingsViewModel.kt
│   │   │       │
│   │   │       └── util/
│   │   │           └── UiState.kt                     # Sealed class: Loading, Success<T>, Error
│   │   │
│   │   ├── test/java/com/weatherapp/
│   │   │   ├── data/
│   │   │   │   ├── weather/WeatherRepositoryTest.kt
│   │   │   │   ├── location/CoordinateUtilsTest.kt    # snapToGrid edge cases
│   │   │   │   ├── calendar/CalendarRepositoryTest.kt
│   │   │   │   └── billing/BillingRepositoryTest.kt
│   │   │   ├── worker/
│   │   │   │   ├── ForecastRefreshWorkerTest.kt
│   │   │   │   └── AlertEvaluationWorkerTest.kt       # State machine transition coverage
│   │   │   └── ui/
│   │   │       └── onboarding/OnboardingViewModelTest.kt
│   │   │
│   │   └── androidTest/java/com/weatherapp/
│   │       ├── db/
│   │       │   ├── ForecastDaoTest.kt
│   │       │   └── AlertStateDaoTest.kt
│   │       └── datastore/
│   │           └── DataStoreExtensionsTest.kt
│   │
│   └── res/
│       ├── xml/
│       │   └── weather_widget_info.xml               # AppWidgetProviderInfo
│       ├── drawable/
│       │   └── widget_preview.png
│       └── values/
│           ├── strings.xml
│           └── colors.xml                            # Static fallbacks only; tokens live in Kotlin
│
├── build.gradle.kts                                  # Project-level: plugin versions
├── settings.gradle.kts                               # Module includes
├── gradle.properties
└── .gitignore
```

### Architectural Boundaries

**Network Boundary (device → Cloudflare Worker):**
- Entry point: `WeatherApi.kt` (Retrofit interface)
- Coordinates are snapped in `LocationRepository.kt` before being passed to `WeatherRepository`
- Worker endpoint: `GET /forecast?lat_grid={lat}&lon_grid={lon}&date={YYYY-MM-DD}`
- No other network calls exist in the app — all weather data flows through this single boundary

**DataStore Boundary (Workers ↔ Widget):**
- Only `ForecastRefreshWorker`, `CalendarScanWorker`, and `AlertEvaluationWorker` write to DataStore
- Only `WeatherWidget` / `WidgetStateReader` reads DataStore for display
- `PreferenceKeys.kt` is the single source of truth for all key strings

**Room Boundary (Workers ↔ Repository ↔ ViewModels):**
- Workers write to Room via DAOs (injected via Hilt)
- Repositories expose Room data as `Flow<T>` — ViewModels consume Repository flows only

**Permission Boundary:**
- `CalendarRepository` owns all `READ_CALENDAR` checks
- `LocationRepository` owns all `ACCESS_COARSE_LOCATION` checks
- `AlertEvaluationWorker` owns `POST_NOTIFICATIONS` checks before sending
- No other class performs permission checks

### Requirements to Structure Mapping

| FR Group | Files/Directories |
|---|---|
| FR-001–003 (verdict, bring list, outdoor window) | `worker/ForecastRefreshWorker.kt`, `model/WidgetDisplayState.kt`, `ui/widget/WeatherWidgetContent.kt` |
| FR-004 (all-clear state) | `ui/widget/WeatherWidgetContent.kt` (empty/minimal render branch) |
| FR-005 (mood line + shareable card) | `ui/widget/WeatherWidgetContent.kt`, `model/WidgetDisplayState.kt` |
| FR-006 (hourly detail tap-through) | `ui/hourly/` — all three files |
| FR-007 (confirmation-first alerts) | `worker/AlertEvaluationWorker.kt`, `data/db/dao/AlertStateDao.kt` |
| FR-008 (60-second onboarding) | `ui/onboarding/` |
| FR-009–010 (calendar widget shift, change alerts) | `worker/CalendarScanWorker.kt`, `worker/AlertEvaluationWorker.kt`, `data/calendar/` |
| FR-011 (stakes-scaled alert windows) | `worker/AlertEvaluationWorker.kt` — threshold logic per stakes level |
| FR-012 (travel pre-load) | `worker/CalendarScanWorker.kt` — non-home location detection + pre-fetch |
| FR-013 (calendar conflict detection) | `data/calendar/CalendarRepository.kt` + `worker/CalendarScanWorker.kt` |
| FR-014–015 (CalendarContract safety, staleness) | `data/calendar/CalendarRepository.kt`, `data/datastore/DataStoreExtensions.kt` |
| FR-016 (widget update on calendar change) | `worker/CalendarScanWorker.kt` (chained to widget update) |
| FR-017 (no upgrade prompts on widget) | `ui/widget/WeatherWidgetContent.kt` — premium gate never renders in widget |
| FR-018 (permission degradation) | `data/location/LocationRepository.kt`, `data/calendar/CalendarRepository.kt` |

**Cross-Cutting Concerns:**

| Concern | Location |
|---|---|
| Coordinate snapping | `data/location/CoordinateUtils.kt` — `snapToGrid()` extension |
| UiState pattern | `util/UiState.kt` — single definition, all ViewModels import |
| WeatherDesignTokens | `ui/theme/WeatherDesignTokens.kt` — Compose + Glance share this object |
| Premium gating | `data/billing/BillingRepository.kt` writes `is_premium` to DataStore; Workers read at cycle start |
| Logging | Timber planted in `WeatherApp.kt`; all classes use `Timber.*` |

### Integration Points

**Internal Data Flow:**

```
Device GPS
  → LocationRepository (snap to 0.1° grid)
  → WeatherRepository
  → WeatherApi (Retrofit)
  → Cloudflare Worker (KV cache check)
  → Open-Meteo (on cache miss)
  → ForecastDao (Room cache write)
  → DataStoreExtensions (write display state)
  → WeatherWidget (Glance reads DataStore)
```

**External Integrations:**

| Integration | Owned By | Failure Mode |
|---|---|---|
| Open-Meteo (via Worker) | `WeatherApi.kt` → Cloudflare Worker | `Result.retry()` up to 3x; staleness flag set in DataStore |
| Cloudflare KV | Worker-side only | Cache miss → Worker calls Open-Meteo directly |
| CalendarContract | `CalendarRepository.kt` | `SecurityException` → empty list returned; no crash |
| Google Play Billing | `BillingRepository.kt` | Connection failure → reads last cached `isPremium` from DataStore |
| Android location | `LocationRepository.kt` | Permission denied → null location; widget shows location-unknown state |

## Architecture Validation Results

### Coherence Validation ✅

**Decision Compatibility:**

| Check | Status | Notes |
|---|---|---|
| Kotlin 2.1.20 + KSP 2.1.20-1.0.29 | ✅ | Exact KSP version pinned to Kotlin version |
| AGP 9.1.0 + Compose BOM 2026.02.01 | ✅ | Both track stable channels |
| Hilt 2.56 + hilt-work 1.2.0 | ✅ | Compatible; hilt-work enables `@HiltWorker` |
| Room 2.6.1 + KSP | ✅ | Room's KSP support stable since 2.5.x |
| Retrofit 3.0.0 (bundles OkHttp 4.x) | ✅ | OkHttp logging interceptor 4.12.0 compatible |
| Glance 1.1.1 outside Compose BOM | ✅ | Manages own versioning; compatible with Compose 1.6 |
| Play Billing 7.1.1 | ✅ | Standalone; no Compose dependency |
| DataStore 1.1.2 | ✅ | Glance supports DataStore Flow observation in `update()` |

**Pattern Consistency:** MVVM + Repository + StateFlow is consistent throughout. Workers bypass ViewModels (correct for background context). Glance bypasses Repository (correct — DataStore direct read only). No contradictions found.

**Structure Alignment:** Feature-first packages match single-module decision. All three Workers, all DAOs, all Repositories have exactly one home. Permission ownership is clean and non-overlapping.

### Requirements Coverage Validation ✅

**All 18 FRs covered:**

| FR | Architectural Support |
|---|---|
| FR-001–003 | `ForecastRefreshWorker` → `WidgetDisplayState` → `WeatherWidgetContent` |
| FR-004 | Empty-state render branch in `WeatherWidgetContent` |
| FR-005 | `WidgetDisplayState.moodLine` + `WeatherWidgetContent` |
| FR-006 | `ui/hourly/` — `HourlyDetailBottomSheet`, `HourlyDetailRow`, `HourlyDetailViewModel` |
| FR-007 | 4-state machine in `AlertEvaluationWorker` + `AlertStateDao` |
| FR-008 | `ui/onboarding/`; first WorkManager run initiated on onboarding completion |
| FR-009–010 | `CalendarScanWorker` + `AlertEvaluationWorker` + `data/calendar/` |
| FR-011 | Stakes-scaled threshold logic in `AlertEvaluationWorker` |
| FR-012 | Non-home location detection in `CalendarScanWorker` → `LocationRepository` → pre-fetch |
| FR-013 | Overlap detection in `CalendarRepository.getUpcomingEvents()` |
| FR-014 | `SecurityException` + general `Exception` wrapping in `CalendarRepository` |
| FR-015 | Staleness flag in DataStore; `lastUpdateAt` always displayed |
| FR-016 | `CalendarScanWorker` chains to `WeatherWidget.update()` on completion |
| FR-017 | Premium feature gate explicitly excluded from `WeatherWidgetContent` |
| FR-018 | `LocationRepository` + `CalendarRepository` return empty/null on permission denied; widget degrades without crash |

**All 5 NFRs covered:**

| NFR | Architectural Mechanism |
|---|---|
| NFR-001 (≤30 min freshness) | WorkManager periodic constraint every 30 min; DataStore write-before-render ordering |
| NFR-002 (≤60s onboarding) | `ui/onboarding/` does not block on network; first Worker run queued after permission grant |
| NFR-003 (zero false-positive alerts) | Conservative 4-state machine; hard thresholds (20% precip, 25mph wind); append-only Room records |
| NFR-004 (battery invisible) | Minimal work per cycle; `isPremium` DataStore read guards expensive calendar path; no redundant API calls |
| NFR-005 (≤72k API calls/month) | Cloudflare Worker + KV 30-min TTL + 0.1° grid clustering; 1,000 MAU → 1–5 unique clusters |

### Gap Analysis

**Critical Gaps:** None.

**Nice-to-Have (deferred by design):**
1. **Shareable card mechanism (FR-005)** — Share Intent / screenshot logic is a display concern, left to the implementation story for `WeatherWidgetContent`
2. **Verdict generation algorithm** — Pure domain logic in `ForecastRefreshWorker`; no cross-component consistency requirements; belongs in implementation story
3. **WorkManager chain configuration** — Exact chaining syntax is an implementation detail, not an architectural decision

### Architecture Completeness Checklist

**✅ Requirements Analysis**
- [x] 18 FRs analyzed and weight-classified by group
- [x] 5 NFRs with quantified targets documented
- [x] Complexity level assessed (low-medium)
- [x] 5 cross-cutting concerns identified and mapped

**✅ Architectural Decisions**
- [x] MVVM + Repository + StateFlow selected and justified
- [x] DataStore vs Room split documented with per-field rationale
- [x] 4-state alert state machine designed with transitions
- [x] Cloudflare Worker + KV + 0.1° grid clustering specified
- [x] Location privacy: device-side snapping, Worker never receives raw GPS
- [x] All dependency versions verified stable (March 2026)

**✅ Implementation Patterns**
- [x] Naming conventions (Kotlin, Room, DataStore, Worker JSON)
- [x] Package structure (feature-first, single-module)
- [x] `UiState<T>` sealed class defined
- [x] Repository `Result<T>` error boundary pattern
- [x] WorkManager retry/failure pattern
- [x] Timber logging enforced — never `Log.*`
- [x] `snapToGrid()` extension — mandatory at network boundary
- [x] CalendarContract safety wrapping
- [x] 10 mandatory agent rules + anti-patterns with corrections

**✅ Project Structure**
- [x] Complete directory tree — every file named and annotated
- [x] All 18 FRs mapped to specific files/directories
- [x] Cross-cutting concerns mapped to ownership
- [x] All external integration failure modes specified
- [x] Test organization defined (unit + instrumented)

### Architecture Readiness Assessment

**Overall Status: READY FOR IMPLEMENTATION**

**Confidence Level: High** — single-technology domain (Android native), well-understood stack, no experimental dependencies, all NFRs have clear mechanical solutions.

**Key Strengths:**
- Privacy-by-design: coordinate snapping on-device is architecturally enforced, not a guideline
- Fault tolerance first: every external integration has an explicit graceful degradation mode
- Glance constraint respected: DataStore-only widget communication prevents a common Android widget pitfall
- Budget-conscious: NFR-005 solved at architecture level via Cloudflare KV clustering

**Areas for Future Enhancement:**
- Multi-module extraction if Glance compilation becomes a bottleneck (`:widget` module)
- Temperature as a primary widget element (v2 Growth Feature, deferred by design)
- Additional weather states beyond Clear/Overcast/Rain (v2)

### Implementation Handoff

**First Implementation Priority:**

```
Android Studio → New Project → Empty Activity
  Language: Kotlin
  Minimum SDK: API 34
  Build configuration language: Kotlin DSL
```

Then configure `build.gradle.kts` exactly as specified in Step 2 before writing any source code. Implementation sequence follows the Decision Impact Analysis order from Step 4.

**AI Agent Guidelines:**
- Follow all architectural decisions exactly as documented
- Use implementation patterns consistently across all components
- Respect project structure and component boundaries
- All architectural questions are answered by this document — do not invent alternatives
