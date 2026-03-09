# Story 1.5: Home Screen Widget

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a free-tier user,
I want a home screen widget that shows the weather verdict in plain language with any relevant bring items and mood line,
so that I can glance at my home screen and know exactly what my day holds without opening the app.

## Acceptance Criteria

**AC-1:** Given the widget is placed on the home screen and DataStore contains a fresh verdict when the user glances at the widget in 4×2 size then it displays: verdict line (primary, large confident-weight text), bring chip(s) if warranted, best outdoor window if applicable, mood line — in that visual order.

**AC-2:** Given the widget is placed in 4×1 minimum size when rendered then only the verdict line and staleness indicator are shown; all other elements are hidden.

**AC-3:** Given `DataStore[KEY_ALL_CLEAR]` is `true` when the widget renders then it displays the all-clear message in a minimal, confident visual state — no empty-looking broken state.

**AC-4:** Given `DataStore[KEY_LAST_UPDATE_EPOCH]` is > 30 minutes ago when the widget renders then a staleness indicator showing time since last update is displayed; the widget never silently shows old data as current.

**AC-5:** Given the widget is rendered when inspected by TalkBack then the Glance root element has a `contentDescription` summarising the full widget state (verdict + bring items if any).

**AC-6:** Given the widget is tapped when the user taps anywhere on the widget then the app opens to the hourly detail view (Story 1.7).

**AC-7:** Given the widget renders in light mode with a Clear weather condition when AdaptiveSkyTheme is applied then the Adaptive Sky color tokens for "clear/light" are used — not Material You dynamic color.

**AC-8:** Given dark mode is enabled on the device when the widget renders then the correct Adaptive Sky dark-mode palette is applied for the current weather condition.

**AC-9:** Given the widget code when reviewed then no upgrade prompts, premium feature previews, or degraded states appear anywhere in `WeatherWidgetContent.kt`.

**AC-10:** Given the Glance composable when reviewed then it reads only from DataStore via `WidgetStateReader`; no Repository or DAO calls exist in any widget composable.

## Tasks / Subtasks

- [ ] Task 1: Create `WeatherDesignTokens.kt` with Adaptive Sky color palette (AC: 7, 8)
  - [ ] 1.1 Create `app/src/main/java/com/weatherapp/ui/theme/WeatherDesignTokens.kt`
  - [ ] 1.2 Define `WeatherState` enum (if not already in `model/`): `CLEAR`, `OVERCAST`, `RAIN`, `STORM`
  - [ ] 1.3 Define token sets for each WeatherState × light/dark combination (6 total combinations)
  - [ ] 1.4 Expose `fun getTokens(state: WeatherState, isDark: Boolean): WeatherColorTokens` where `WeatherColorTokens` is a data class with `background`, `verdictText`, `secondaryText`, `chipBackground`, `chipText`, `accentColor` fields
  - [ ] 1.5 These tokens are shared between Compose (in-app) and Glance (widget) — define as pure Color values

- [ ] Task 2: Create `AdaptiveSkyTheme.kt` (AC: 7, 8)
  - [ ] 2.1 Create `app/src/main/java/com/weatherapp/ui/theme/AdaptiveSkyTheme.kt`
  - [ ] 2.2 Implement as a `@Composable` function wrapping `MaterialTheme` with `dynamicColor = false`
  - [ ] 2.3 Accept `weatherState: WeatherState` and `darkTheme: Boolean` parameters
  - [ ] 2.4 Apply `WeatherDesignTokens.getTokens(weatherState, darkTheme)` to colorScheme

- [ ] Task 3: Create `WidgetStateReader.kt` (AC: 10)
  - [ ] 3.1 Create `app/src/main/java/com/weatherapp/ui/widget/WidgetStateReader.kt`
  - [ ] 3.2 Implement `suspend fun DataStore<Preferences>.readWidgetDisplayState(): WidgetDisplayState` reading all required keys: `KEY_WIDGET_VERDICT`, `KEY_BRING_LIST`, `KEY_BEST_WINDOW`, `KEY_ALL_CLEAR`, `KEY_MOOD_LINE`, `KEY_LAST_UPDATE_EPOCH`, `KEY_STALENESS_FLAG`
  - [ ] 3.3 Parse bring list from pipe-delimited string: `bringListStr.split("|").filter { it.isNotEmpty() }`
  - [ ] 3.4 Compute `isStale: Boolean` from `currentTimeSeconds - lastUpdateEpoch > 1800` or `stalenessFlag == true`
  - [ ] 3.5 Determine `weatherState: WeatherState` from verdict text or a stored weather code key

- [ ] Task 4: Create `WidgetDisplayState.kt` model (AC: 1–10)
  - [ ] 4.1 Create `app/src/main/java/com/weatherapp/model/WidgetDisplayState.kt`
  - [ ] 4.2 `data class WidgetDisplayState(val verdict: String, val bringItems: List<String>, val bestWindow: String?, val isAllClear: Boolean, val moodLine: String, val lastUpdateEpoch: Long, val isStale: Boolean, val weatherState: WeatherState)`
  - [ ] 4.3 Provide a sensible default/empty state: `companion object { val EMPTY = WidgetDisplayState("Loading...", emptyList(), null, false, "", 0L, false, WeatherState.CLEAR) }`

- [ ] Task 5: Create `WeatherWidgetContent.kt` Glance composable (AC: 1–5, 7–10)
  - [ ] 5.1 Create `app/src/main/java/com/weatherapp/ui/widget/WeatherWidgetContent.kt`
  - [ ] 5.2 This is a Glance composable (NOT a standard Compose composable) — use `androidx.glance` imports
  - [ ] 5.3 Root element must have `contentDescription` combining verdict + bring items for TalkBack (AC-5)
  - [ ] 5.4 4×2 layout: `Column` with verdict text (primary, `FontWeight.Bold`, large size) → bring chips (if list not empty) → best window text (if not null/empty) → mood line
  - [ ] 5.5 4×1 layout: detect `LocalSize.current` — if height < 110.dp, show ONLY verdict text + staleness indicator
  - [ ] 5.6 All-clear state (AC-3): when `isAllClear = true`, render with a minimal, confident visual state — use a distinct background token, show the all-clear message prominently
  - [ ] 5.7 Staleness indicator (AC-4): when `isStale = true`, show "Last updated X min ago" in small secondary text below the verdict
  - [ ] 5.8 Apply Adaptive Sky colors from `WeatherDesignTokens` based on `weatherState` + dark mode (`LocalContext.current` → `isSystemInDarkTheme()`)
  - [ ] 5.9 NO upgrade prompts, premium previews, or degraded states — this is a hard requirement (AC-9)
  - [ ] 5.10 NO calls to Repository or DAO — reads only from the passed `WidgetDisplayState` (AC-10)

- [ ] Task 6: Create `WeatherWidget.kt` GlanceAppWidget subclass (AC: 6, 10)
  - [ ] 6.1 Create `app/src/main/java/com/weatherapp/ui/widget/WeatherWidget.kt`
  - [ ] 6.2 Extend `GlanceAppWidget`
  - [ ] 6.3 Override `suspend fun provideGlance(context: Context, id: GlanceId)`: read DataStore via `WidgetStateReader`, then call `provideContent { WeatherWidgetContent(state) }`
  - [ ] 6.4 DataStore is accessed via `context.applicationContext` — do NOT inject Repository or DAO
  - [ ] 6.5 Implement `companion object { suspend fun update(context: Context) }` for forcing widget refresh after WorkManager writes

- [ ] Task 7: Create `WeatherWidgetReceiver.kt` (AC: 6)
  - [ ] 7.1 Create `app/src/main/java/com/weatherapp/ui/widget/WeatherWidgetReceiver.kt`
  - [ ] 7.2 Extend `GlanceAppWidgetReceiver`
  - [ ] 7.3 Override `val glanceAppWidget: GlanceAppWidget = WeatherWidget()`

- [ ] Task 8: Register widget in `AndroidManifest.xml` and `weather_widget_info.xml` (AC: 1, 2)
  - [ ] 8.1 Add `<receiver>` declaration for `WeatherWidgetReceiver` in `AndroidManifest.xml` with `<intent-filter>` for `android.appwidget.action.APPWIDGET_UPDATE`
  - [ ] 8.2 Create `app/src/main/res/xml/weather_widget_info.xml` with `minWidth="250dp"`, `minHeight="110dp"`, `targetCellWidth="4"`, `targetCellHeight="2"`, `minResizeHeight="55dp"` for 4×1 support
  - [ ] 8.3 Reference `weather_widget_info.xml` in the `<receiver>` meta-data

- [ ] Task 9: Implement tap action to open hourly detail (AC: 6)
  - [ ] 9.1 In `WeatherWidgetContent.kt`, set `clickable` on the root container using `actionStartActivity<MainActivity>()` with an extra to indicate deep-link destination (hourly detail)
  - [ ] 9.2 Define the Intent extra key as a constant: `const val EXTRA_OPEN_HOURLY = "open_hourly"` in `MainActivity` companion object
  - [ ] 9.3 In `MainActivity.onCreate()`, check for this extra and navigate to the hourly detail bottom sheet (navigation handled in Story 1.7, but wire the Intent extra here)

- [ ] Task 10: Wire `WeatherWidget.update()` call at end of `ForecastRefreshWorker` (AC: 1)
  - [ ] 10.1 In `ForecastRefreshWorker.doWork()`, after the DataStore write block, call `WeatherWidget.update(applicationContext)`
  - [ ] 10.2 This ensures the widget re-renders immediately after new data is written, not waiting for the next Glance refresh cycle

- [ ] Task 11: Accessibility and touch target compliance (AC: 5)
  - [ ] 11.1 Verify all interactive elements have minimum 48×48dp touch targets
  - [ ] 11.2 Verify content descriptions on all meaningful elements
  - [ ] 11.3 All text uses `sp` units (not `dp`) for font size — Glance Text uses `TextUnit` with `sp` values
  - [ ] 11.4 Test at 130% and 200% font scale that text does not overflow or truncate incorrectly

- [ ] Task 12: Dark mode and Adaptive Sky palette tests (AC: 7, 8)
  - [ ] 12.1 Verify all 6 combinations render correctly: Clear×Light, Clear×Dark, Overcast×Light, Overcast×Dark, Rain×Light, Rain×Dark
  - [ ] 12.2 Confirm `dynamicColor = false` in `AdaptiveSkyTheme.kt` — Material You never activated

## Dev Notes

### Critical Architecture Rules for This Story

**1. Glance reads DataStore ONLY — No Repository, No DAO**

This is a hard architectural rule:

```kotlin
// CORRECT — WeatherWidget.kt
override suspend fun provideGlance(context: Context, id: GlanceId) {
    val dataStore = context.applicationContext.getDataStore() // extension or DI
    val state = dataStore.readWidgetDisplayState()
    provideContent {
        WeatherWidgetContent(state)
    }
}

// WRONG — never do this in a Glance composable or WeatherWidget
val repo = WeatherRepository(...) // ❌ never
val hours = dao.queryByTimeWindow(...) // ❌ never
```

**2. Glance Composable Structure — 4×2 vs 4×1**

```kotlin
// WeatherWidgetContent.kt — note: this is a Glance composable, not standard Compose
@Composable
fun WeatherWidgetContent(state: WidgetDisplayState) {
    val size = LocalSize.current
    val isMinimal = size.height < 110.dp

    GlanceTheme {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(/* token-based color */)
                .clickable(actionStartActivity<MainActivity>(
                    actionParametersOf(ActionParameters.Key<Boolean>("open_hourly") to true)
                ))
                .semantics { contentDescription = buildContentDescription(state) }
        ) {
            if (isMinimal) {
                // 4×1: verdict only + staleness
                MinimalWidgetLayout(state)
            } else {
                // 4×2: full layout
                FullWidgetLayout(state)
            }
        }
    }
}
```

**3. TalkBack Content Description**

Build a single content description for the widget root:

```kotlin
private fun buildContentDescription(state: WidgetDisplayState): String {
    val sb = StringBuilder()
    sb.append(state.verdict)
    if (state.bringItems.isNotEmpty()) {
        sb.append(". ${state.bringItems.joinToString(", ")}")
    }
    if (state.bestWindow != null) {
        sb.append(". ${state.bestWindow}")
    }
    if (state.isStale) {
        sb.append(". Data may be outdated.")
    }
    return sb.toString()
}
```

**4. Staleness Indicator**

```kotlin
// Compute staleness for display
private fun formatStaleness(lastUpdateEpoch: Long): String {
    val minutesAgo = (System.currentTimeMillis() / 1000L - lastUpdateEpoch) / 60
    return "Last updated ${minutesAgo}m ago"
}

// In layout:
if (state.isStale) {
    Text(
        text = formatStaleness(state.lastUpdateEpoch),
        style = TextStyle(fontSize = 10.sp, color = /* secondary token */)
    )
}
```

**5. Adaptive Sky Color Tokens — Concrete Example**

Define all 6 combinations. The exact colors are product design decisions — use these as starting values:

```kotlin
// WeatherDesignTokens.kt
data class WeatherColorTokens(
    val background: Color,
    val verdictText: Color,
    val secondaryText: Color,
    val accentColor: Color,
    val chipBackground: Color,
    val chipText: Color
)

object WeatherDesignTokens {
    val clearLight = WeatherColorTokens(
        background = Color(0xFFE8F4FD),
        verdictText = Color(0xFF1A3A5C),
        secondaryText = Color(0xFF4A6A8A),
        accentColor = Color(0xFF2196F3),
        chipBackground = Color(0xFFBBDEFB),
        chipText = Color(0xFF1565C0)
    )
    val clearDark = WeatherColorTokens(
        background = Color(0xFF0D2137),
        verdictText = Color(0xFFE3F2FD),
        secondaryText = Color(0xFF90CAF9),
        accentColor = Color(0xFF64B5F6),
        chipBackground = Color(0xFF1565C0),
        chipText = Color(0xFFE3F2FD)
    )
    val rainLight = WeatherColorTokens(
        background = Color(0xFFECEFF1),
        verdictText = Color(0xFF263238),
        secondaryText = Color(0xFF546E7A),
        accentColor = Color(0xFF607D8B),
        chipBackground = Color(0xFFCFD8DC),
        chipText = Color(0xFF37474F)
    )
    // ... define overcastLight, overcastDark, rainDark, stormLight, stormDark

    fun getTokens(state: WeatherState, isDark: Boolean): WeatherColorTokens = when (state) {
        WeatherState.CLEAR    -> if (isDark) clearDark else clearLight
        WeatherState.OVERCAST -> if (isDark) overcastDark else overcastLight
        WeatherState.RAIN     -> if (isDark) rainDark else rainLight
        WeatherState.STORM    -> if (isDark) stormDark else stormLight
    }
}
```

**6. WeatherWidget.update() — Forcing Refresh**

```kotlin
// WeatherWidget.kt
companion object {
    suspend fun update(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(WeatherWidget::class.java)
        glanceIds.forEach { glanceId ->
            WeatherWidget().update(context, glanceId)
        }
    }
}
```

Call this from `ForecastRefreshWorker` after writing DataStore:
```kotlin
WeatherWidget.update(applicationContext)
```

**7. No Premium Content in Widget — Enforcement**

Review `WeatherWidgetContent.kt` to confirm zero occurrences of:
- "premium", "upgrade", "subscribe", "unlock" in any string
- Any `if (isPremium)` conditional rendering
- Any UI element that is grayed out or locked-looking

The premium upgrade path exists ONLY in `SettingsScreen`.

**8. AndroidManifest.xml Widget Registration**

```xml
<receiver
    android:name=".ui.widget.WeatherWidgetReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/weather_widget_info" />
</receiver>
```

**9. weather_widget_info.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="250dp"
    android:minHeight="110dp"
    android:targetCellWidth="4"
    android:targetCellHeight="2"
    android:minResizeWidth="250dp"
    android:minResizeHeight="55dp"
    android:resizeMode="horizontal|vertical"
    android:updatePeriodMillis="1800000"
    android:initialLayout="@layout/glance_default_loading_layout"
    android:description="@string/widget_description"
    android:widgetCategory="home_screen"
    android:widgetFeatures="reconfigurable" />
```

**10. Anti-Patterns to Avoid**

- NEVER import `androidx.compose.runtime.*` in `WeatherWidgetContent.kt` — use `androidx.glance.*` composables
- NEVER call `WeatherRepository`, `ForecastDao`, or any data layer from widget composables
- NEVER use Material You / dynamic color — `dynamicColor = false` always
- NEVER show premium/upgrade UI in the widget
- NEVER let the widget display stale data without the staleness indicator
- NEVER use `Log.*` — use `Timber.d/i/w/e()`

### Project Structure Notes

Files to create in this story:
```
app/src/main/java/com/weatherapp/
  model/
    WidgetDisplayState.kt          ← NEW: data class for widget state
  ui/
    theme/
      WeatherDesignTokens.kt       ← NEW: Adaptive Sky color tokens
      AdaptiveSkyTheme.kt          ← NEW: MaterialTheme wrapper
      Type.kt                      ← NEW: Typography scale (if not in Story 1.1)
    widget/
      WeatherWidget.kt              ← NEW: GlanceAppWidget subclass
      WeatherWidgetContent.kt       ← NEW: Glance composable (full UI)
      WeatherWidgetReceiver.kt      ← NEW: GlanceAppWidgetReceiver
      WidgetStateReader.kt          ← NEW: DataStore → WidgetDisplayState

app/src/main/res/
  xml/
    weather_widget_info.xml         ← NEW: AppWidgetProviderInfo
  drawable/
    widget_preview.png              ← NEW: placeholder widget preview image
  values/
    strings.xml                     ← MODIFY: add widget_description string
```

Files to modify:
```
app/src/main/AndroidManifest.xml   ← MODIFY: add WeatherWidgetReceiver registration
app/src/main/java/com/weatherapp/
  worker/
    ForecastRefreshWorker.kt        ← MODIFY: call WeatherWidget.update() after DataStore write
  MainActivity.kt                   ← MODIFY: handle EXTRA_OPEN_HOURLY intent extra
```

### Cross-Story Dependencies

- **Depends on Story 1.3**: `PreferenceKeys.*` constants, `DataStore<Preferences>` provider, `ForecastRefreshWorker` for calling `WeatherWidget.update()`
- **Depends on Story 1.4**: All DataStore keys populated with display-ready strings; `KEY_STALENESS_FLAG`, `KEY_LAST_UPDATE_EPOCH`, `KEY_ALL_CLEAR`, `KEY_BRING_LIST`, `KEY_BEST_WINDOW`, `KEY_MOOD_LINE`
- **Provides to Story 1.6**: Widget pinning API usage; `WeatherWidget` class for pinning from onboarding completion
- **Provides to Story 1.7**: `EXTRA_OPEN_HOURLY` intent routing; widget tap navigates to hourly detail
- **Provides to Story 3.3**: `WeatherWidget.update()` companion method called by `CalendarScanWorker` after writing premium widget state

### References

- [Source: architecture.md#Communication Patterns] — Glance reads DataStore only; WorkManager → DataStore → Glance chain
- [Source: architecture.md#Frontend Architecture] — `WeatherDesignTokens` shared Compose+Glance, dynamic color disabled
- [Source: architecture.md#Enforcement Guidelines] — Rule 10: Never call Repository/DAO from Glance composable
- [Source: epics.md#Story 1.5] — All acceptance criteria including 4×2 vs 4×1 sizing, TalkBack, no premium prompts

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

None — no runtime errors encountered during implementation.

### Completion Notes List

- Task 4 (WidgetDisplayState.kt) and supporting models completed first as foundations for other tasks.
- DataStoreProvider.kt created to share a single `preferencesDataStore` delegate between Hilt (AppModule) and non-Hilt (WeatherWidget) contexts — required because Glance widgets cannot use Hilt injection.
- DataStoreExtensions.kt updated with new WidgetDisplayState structure and `inferWeatherState()` helper.
- WidgetStateReader.kt is a separate extension in the `ui.widget` package (per AC-10 separation of concerns) — functionally equivalent to DataStoreExtensions but explicitly scoped to widget use.
- WeatherWidgetContent.kt uses only `androidx.glance.*` composables — no `androidx.compose.material3` or standard Compose UI in the widget rendering path (anti-pattern per Dev Notes #10).
- `cornerRadius()` uses `Dp` overload (`16.dp`) not `Int` (which would be treated as a resource ID).
- `dynamicColor = false` enforced in AdaptiveSkyTheme — Material You never activates.
- All 4 WeatherState × light/dark combinations defined (8 total token sets).
- ForecastRefreshWorker updated to call `WeatherWidget.update(applicationContext)` after DataStore write (Task 10).
- EXTRA_OPEN_HOURLY already present in MainActivity from earlier story setup.

### File List

- `app/src/main/java/com/weatherapp/ui/theme/WeatherDesignTokens.kt` — NEW
- `app/src/main/java/com/weatherapp/ui/theme/AdaptiveSkyTheme.kt` — NEW
- `app/src/main/java/com/weatherapp/ui/widget/WidgetStateReader.kt` — NEW
- `app/src/main/java/com/weatherapp/ui/widget/WeatherWidgetContent.kt` — NEW
- `app/src/main/java/com/weatherapp/ui/widget/WeatherWidget.kt` — NEW
- `app/src/main/java/com/weatherapp/ui/widget/WeatherWidgetReceiver.kt` — NEW
- `app/src/main/res/xml/weather_widget_info.xml` — NEW
- `app/src/main/res/values/strings.xml` — MODIFIED (added widget_description)
- `app/src/main/AndroidManifest.xml` — MODIFIED (WeatherWidgetReceiver registered)
- `app/src/main/java/com/weatherapp/worker/ForecastRefreshWorker.kt` — MODIFIED (WeatherWidget.update() call)
- `app/src/main/java/com/weatherapp/data/datastore/DataStoreProvider.kt` — NEW (prior session)
- `app/src/main/java/com/weatherapp/data/datastore/DataStoreExtensions.kt` — MODIFIED (prior session)
- `app/src/main/java/com/weatherapp/model/WeatherState.kt` — NEW (prior session)
- `app/src/main/java/com/weatherapp/model/WidgetDisplayState.kt` — MODIFIED (prior session)
- `app/src/main/java/com/weatherapp/di/AppModule.kt` — MODIFIED (prior session)
