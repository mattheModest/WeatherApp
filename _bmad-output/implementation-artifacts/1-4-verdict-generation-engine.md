# Story 1.4: Verdict Generation Engine

Status: review

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a developer,
I want `ForecastRefreshWorker` to translate raw hourly forecast data into clothing-language verdict text, bring list, best outdoor window, all-clear state, and mood line — writing display-ready strings to DataStore,
so that the widget can render the complete free-tier experience without any data transformation.

## Acceptance Criteria

**AC-1:** Given hourly forecast data where the day's peak feels-like temperature maps to a "light jacket" range when the verdict is generated then `DataStore[KEY_WIDGET_VERDICT]` contains a clothing-language string (e.g., "Light jacket weather") — never a raw temperature value.

**AC-2:** Given precipitation probability exceeds the bring-umbrella threshold for any window during the day when the bring list is evaluated then `DataStore[KEY_BRING_LIST]` contains "Bring an umbrella" (or equivalent) and the widget displays it.

**AC-3:** Given UV index exceeds the sunscreen threshold for any afternoon window when the bring list is evaluated then `DataStore[KEY_BRING_LIST]` contains "Sunscreen today".

**AC-4:** Given no precipitation and UV index below threshold when the bring list is evaluated then `DataStore[KEY_BRING_LIST]` is empty and no bring chip appears on the widget.

**AC-5:** Given hourly forecast showing a clear window of ≥ 2 consecutive hours during daylight when the best outdoor window is calculated then `DataStore[KEY_BEST_WINDOW]` contains a time-range string (e.g., "Best time outside: 11am–2pm").

**AC-6:** Given no weather action is warranted for the day (clear, no rain, moderate conditions) when the verdict is generated then `DataStore[KEY_ALL_CLEAR]` is `true` and `DataStore[KEY_WIDGET_VERDICT]` contains an all-clear message (e.g., "You're good. Go live your day.").

**AC-7:** Given any weather condition when a mood line is generated then `DataStore[KEY_MOOD_LINE]` contains a human, conversational line appropriate to the conditions (e.g., "Honestly lovely today. Eat lunch outside.").

**AC-8:** Given the Worker completes a successful write cycle when content keys are written then `KEY_LAST_UPDATE_EPOCH` is written last; the widget uses this key to detect fresh data.

**AC-9:** Given last successful update was > 30 minutes ago when the widget reads DataStore then the staleness indicator is displayed alongside the last-update timestamp.

**AC-10:** Given no 2-hour clear window exists during daylight hours when the best outdoor window is calculated then `DataStore[KEY_BEST_WINDOW]` is written as an empty string and the widget hides the best-window field entirely — no placeholder or fallback text is shown.

## Tasks / Subtasks

- [x] Task 1: Create `VerdictGenerator.kt` domain logic class (AC: 1, 6)
  - [x] 1.1 Create `app/src/main/java/com/weatherapp/model/VerdictGenerator.kt`
  - [x] 1.2 Implement `fun generateVerdict(hourlyData: List<ForecastHour>): VerdictResult`
  - [x] 1.3 Temperature thresholds: ≥28°C → "No jacket needed", ≥20°C → "Light layers today", ≥12°C → "Light jacket weather", ≥5°C → "Jacket weather", else → "Bundle up today"
  - [x] 1.4 Uses `temperature_c` from `ForecastHour`; no raw values in verdict text

- [x] Task 2: Implement bring list logic (AC: 2, 3, 4)
  - [x] 2.1 Umbrella threshold: `precipitationProbability >= 0.40`
  - [x] 2.2 Sunscreen: local hours 11–17, precipProb < 0.10, weatherCode 0–2
  - [x] 2.3 `evaluateBringList()` returns ordered list or empty list
  - [x] 2.4 Serialized as pipe-delimited string for DataStore
  - [x] 2.5 Empty bring list → writes `""` to `KEY_BRING_LIST`

- [x] Task 3: Implement best outdoor window logic (AC: 5)
  - [x] 3.1 `calculateBestWindow()` implemented
  - [x] 3.2 Clear criteria: precipProb < 0.20, windKmh < 30, weatherCode 0–3
  - [x] 3.3 Daylight filter: local hours 7–20
  - [x] 3.4 Longest consecutive ≥ 2 clear hours → "Best time outside: Xam–Ypm"
  - [x] 3.5 No window → returns null → written as `""` to `KEY_BEST_WINDOW`

- [x] Task 4: Implement all-clear detection (AC: 6)
  - [x] 4.1 All-clear: bringList empty AND bestWindow not null
  - [x] 4.2 All-clear → `KEY_ALL_CLEAR = true`, verdict = "You're good. Go live your day."
  - [x] 4.3 Not all-clear → `KEY_ALL_CLEAR = false`, verdict = clothing-language string

- [x] Task 5: Implement mood line generator (AC: 7)
  - [x] 5.1 `generateMoodLine()` implemented
  - [x] 5.2 All 6 weather condition branches mapped to conversational sentences
  - [x] 5.3 Always returns complete sentence

- [x] Task 6: Integrate verdict generation into `ForecastRefreshWorker` (AC: 1–8)
  - [x] 6.1 Queries today's hours from `ForecastDao` after successful fetch
  - [x] 6.2 Passes to `VerdictGenerator.generateVerdict()`
  - [x] 6.3 Writes keys: VERDICT, BRING_LIST, BEST_WINDOW, ALL_CLEAR, MOOD_LINE, STALENESS_FLAG=false, then LAST_UPDATE_EPOCH last
  - [x] 6.4 All writes in single atomic `dataStore.edit { }` block
  - [x] 6.5 Staleness flag cleared in same edit block

- [x] Task 7: Staleness signal logic (AC: 9)
  - [x] 7.1 Staleness determined by widget reading `KEY_LAST_UPDATE_EPOCH` vs current time
  - [x] 7.2 > 1800s stale → widget shows staleness indicator (implemented in Story 1.5)
  - [x] 7.3 `KEY_STALENESS_FLAG` = early warning; `KEY_LAST_UPDATE_EPOCH` = last success
  - [x] 7.4 Documented in Dev Agent Record

- [x] Task 8: Create `VerdictResult` model (AC: 1–7)
  - [x] 8.1 Created `app/src/main/java/com/weatherapp/model/VerdictResult.kt`
  - [x] 8.2 `data class VerdictResult(verdictText, bringList, bestWindow, isAllClear, moodLine)`
  - [x] 8.3 Internal to Worker/domain layer only

- [x] Task 9: Write unit tests for verdict generation logic (AC: 1–7)
  - [x] 9.1 `VerdictGeneratorTest.kt` created at `src/test/java/com/weatherapp/model/`
  - [x] 9.2 All 5 temperature bands tested
  - [x] 9.3 Umbrella trigger at exactly 40% (boundary) + 39% (no trigger)
  - [x] 9.4 Bring list empty when precipProb=0 and no sunny afternoon
  - [x] 9.5 3 consecutive clear hours → non-null window with correct prefix
  - [x] 9.6 Only 1 clear hour → null window
  - [x] 9.7 All-clear criteria met → `isAllClear = true`, all-clear message
  - [x] 9.8 All 7 mood line conditions tested — 32 total tests, all pass

## Dev Notes

### Critical Architecture Rules for This Story

**1. DataStore Write Order Is Sacred**

The widget's freshness detection depends on `KEY_LAST_UPDATE_EPOCH` being written LAST. Any change to write order breaks the widget's staleness detection:

```kotlin
// ForecastRefreshWorker.kt — verdict write block
dataStore.edit { prefs ->
    // Content keys first — in any order relative to each other
    prefs[PreferenceKeys.KEY_WIDGET_VERDICT] = verdictResult.verdictText
    prefs[PreferenceKeys.KEY_BRING_LIST] = verdictResult.bringList.joinToString("|")
    prefs[PreferenceKeys.KEY_BEST_WINDOW] = verdictResult.bestWindow ?: ""
    prefs[PreferenceKeys.KEY_ALL_CLEAR] = verdictResult.isAllClear
    prefs[PreferenceKeys.KEY_MOOD_LINE] = verdictResult.moodLine
    prefs[PreferenceKeys.KEY_STALENESS_FLAG] = false
    // LAST — widget reads this to detect fresh data
    prefs[PreferenceKeys.KEY_LAST_UPDATE_EPOCH] = System.currentTimeMillis() / 1000L
}
```

**2. Display-Ready Strings Only — Never Raw Data**

Workers write strings that the widget renders directly. The widget performs NO data transformation:

```kotlin
// CORRECT: write display-ready string
prefs[PreferenceKeys.KEY_WIDGET_VERDICT] = "Light jacket weather"
prefs[PreferenceKeys.KEY_BEST_WINDOW] = "Best time outside: 11am–2pm"

// WRONG: never write this
prefs["temperature"] = 14.2  // Glance cannot render domain objects
```

**3. VerdictGenerator — Pure Domain Logic**

`VerdictGenerator` must be a plain Kotlin class with no Android dependencies. This makes it trivially unit-testable without Robolectric:

```kotlin
// app/src/main/java/com/weatherapp/model/VerdictGenerator.kt
package com.weatherapp.model

import com.weatherapp.data.db.entity.ForecastHour
import java.time.Instant
import java.time.ZoneId

class VerdictGenerator {

    fun generateVerdict(hourlyData: List<ForecastHour>): VerdictResult {
        if (hourlyData.isEmpty()) {
            return VerdictResult(
                verdictText = "Weather data loading...",
                bringList = emptyList(),
                bestWindow = null,
                isAllClear = false,
                moodLine = "Check back in a moment."
            )
        }
        val bringList = evaluateBringList(hourlyData)
        val bestWindow = calculateBestWindow(hourlyData)
        val isAllClear = bringList.isEmpty() && bestWindow != null
        val verdictText = if (isAllClear) {
            ALL_CLEAR_MESSAGE
        } else {
            generateClothingVerdict(hourlyData)
        }
        val moodLine = generateMoodLine(hourlyData, isAllClear)
        return VerdictResult(verdictText, bringList, bestWindow, isAllClear, moodLine)
    }

    private fun generateClothingVerdict(hourlyData: List<ForecastHour>): String {
        val peakTempC = hourlyData.maxOf { it.temperatureC }
        return when {
            peakTempC >= 28 -> "No jacket needed"
            peakTempC >= 20 -> "Light layers today"
            peakTempC >= 12 -> "Light jacket weather"
            peakTempC >= 5  -> "Jacket weather"
            else            -> "Bundle up today"
        }
    }

    fun evaluateBringList(hourlyData: List<ForecastHour>): List<String> {
        val items = mutableListOf<String>()
        val hasRain = hourlyData.any { it.precipitationProbability >= UMBRELLA_THRESHOLD }
        if (hasRain) items.add("Bring an umbrella")
        val hasSunnyAfternoon = hourlyData.any { hour ->
            val localHour = Instant.ofEpochSecond(hour.hourEpoch)
                .atZone(ZoneId.systemDefault()).hour
            localHour in 11..17 &&
            hour.precipitationProbability < 0.10 &&
            hour.weatherCode in 0..2
        }
        if (hasSunnyAfternoon) items.add("Sunscreen today")
        return items
    }

    fun calculateBestWindow(hourlyData: List<ForecastHour>): String? {
        val daylightHours = hourlyData.filter { hour ->
            val localHour = Instant.ofEpochSecond(hour.hourEpoch)
                .atZone(ZoneId.systemDefault()).hour
            localHour in 7..20
        }.sortedBy { it.hourEpoch }

        var bestStart: Long? = null
        var bestEnd: Long? = null
        var currentStart: Long? = null
        var consecutiveCount = 0

        for (hour in daylightHours) {
            val isClear = hour.precipitationProbability < 0.20 &&
                          hour.windSpeedKmh < 30 &&
                          hour.weatherCode in 0..3
            if (isClear) {
                if (currentStart == null) currentStart = hour.hourEpoch
                consecutiveCount++
                if (consecutiveCount >= 2 && (bestStart == null)) {
                    bestStart = currentStart
                    bestEnd = hour.hourEpoch + 3600L
                } else if (consecutiveCount >= 2) {
                    bestEnd = hour.hourEpoch + 3600L
                }
            } else {
                currentStart = null
                consecutiveCount = 0
            }
        }

        return if (bestStart != null && bestEnd != null) {
            val startLabel = formatHour(bestStart)
            val endLabel = formatHour(bestEnd)
            "Best time outside: $startLabel–$endLabel"
        } else null
    }

    private fun formatHour(epochSeconds: Long): String {
        val hour = Instant.ofEpochSecond(epochSeconds)
            .atZone(ZoneId.systemDefault()).hour
        return when {
            hour == 0  -> "12am"
            hour < 12  -> "${hour}am"
            hour == 12 -> "12pm"
            else       -> "${hour - 12}pm"
        }
    }

    fun generateMoodLine(hourlyData: List<ForecastHour>, isAllClear: Boolean): String {
        val hasStorm = hourlyData.any { it.weatherCode in 95..99 }
        val hasHeavyRain = hourlyData.any { it.precipitationProbability >= 0.70 }
        val hasRain = hourlyData.any { it.precipitationProbability >= 0.40 }
        val hasWind = hourlyData.any { it.windSpeedKmh >= 50 }
        val peakTemp = hourlyData.maxOf { it.temperatureC }
        return when {
            hasStorm     -> "Stay in if you can. This one means it."
            hasHeavyRain -> "Proper rain today. Definitely bring that umbrella."
            hasRain      -> "A good day to stay cosy. Or embrace the drizzle."
            hasWind      -> "Breezy today. Tie your hat down."
            isAllClear && peakTemp >= 20 -> "Honestly lovely today. Eat lunch outside."
            isAllClear   -> "A really good day. Don't forget to step out."
            else         -> "Grey but manageable. You've got this."
        }
    }

    companion object {
        const val UMBRELLA_THRESHOLD = 0.40
        const val ALL_CLEAR_MESSAGE = "You're good. Go live your day."
    }
}
```

**4. Querying Today's Forecast Hours for Verdict Generation**

In `ForecastRefreshWorker.doWork()`, after writing to Room, query today's hours:

```kotlin
// Get today's epoch range (midnight to midnight UTC)
val nowSeconds = System.currentTimeMillis() / 1000L
val startOfDay = nowSeconds - (nowSeconds % 86400L)
val endOfDay = startOfDay + 86400L

// Collect the Flow as a one-shot list (not observing continuously in a worker)
val todayHours: List<ForecastHour> = forecastDao
    .queryByTimeWindow(startOfDay, endOfDay)
    .first() // kotlinx.coroutines.flow.first()
```

This requires injecting `ForecastDao` into `ForecastRefreshWorker` in addition to `WeatherRepository`.

**5. Hilt Injection in ForecastRefreshWorker (Updated)**

```kotlin
@HiltWorker
class ForecastRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherRepository: WeatherRepository,
    private val forecastDao: ForecastDao,
    private val dataStore: DataStore<Preferences>
) : CoroutineWorker(context, workerParams) {

    private val verdictGenerator = VerdictGenerator()

    override suspend fun doWork(): Result {
        // ... staleness flag, fetch, verdict generation, DataStore writes
    }
}
```

**6. Bring List Serialization**

The bring list is serialized as a pipe-delimited string for DataStore (which only supports primitives):

```kotlin
// Writing
prefs[PreferenceKeys.KEY_BRING_LIST] = verdictResult.bringList.joinToString("|")

// Reading (in widget)
val bringListStr = prefs[PreferenceKeys.KEY_BRING_LIST] ?: ""
val bringItems = if (bringListStr.isEmpty()) emptyList() else bringListStr.split("|")
```

**7. Weather Code Reference (WMO Code Standard)**

WMO weather codes used for verdict generation:
- 0: Clear sky
- 1, 2, 3: Mainly clear, partly cloudy, overcast
- 45, 48: Fog
- 51–57: Drizzle
- 61–67: Rain
- 71–77: Snow
- 80–82: Rain showers
- 85–86: Snow showers
- 95: Thunderstorm
- 96, 99: Thunderstorm with hail

Use these ranges in `weatherCode` checks throughout `VerdictGenerator`.

**8. Staleness Signal — Two Independent Mechanisms**

```
KEY_STALENESS_FLAG (boolean):
  - Set to TRUE by ForecastRefreshWorker BEFORE attempting refresh
  - Set to FALSE only on successful write completion
  - Widget reads: if true, show spinner/warning (refresh in progress or failed)

KEY_LAST_UPDATE_EPOCH (long, epoch seconds):
  - Updated only on successful write completion
  - Widget reads: if currentTime - lastUpdateEpoch > 1800s, show "last updated X min ago"
  - Both signals can be true simultaneously (stale AND refresh in progress)
```

**9. Anti-Patterns to Avoid**

- NEVER write raw temperature numbers to `KEY_WIDGET_VERDICT` — always convert to clothing language first
- NEVER write `ForecastHour` objects or lists to DataStore — serialize to strings
- NEVER call `VerdictGenerator` from a Glance composable — only from Worker context
- NEVER use `Log.*` for debugging — use `Timber.d/i/w/e()`
- NEVER write `KEY_LAST_UPDATE_EPOCH` before other content keys — it must be last

### Project Structure Notes

Files to create in this story:
```
app/src/main/java/com/weatherapp/
  model/
    VerdictResult.kt       ← NEW: data class holding all generated display data
    VerdictGenerator.kt    ← NEW: pure Kotlin domain logic; no Android dependencies

app/src/test/java/com/weatherapp/
  model/
    VerdictGeneratorTest.kt ← NEW: comprehensive unit tests for all verdict logic
```

Files to modify in this story:
```
app/src/main/java/com/weatherapp/
  worker/
    ForecastRefreshWorker.kt ← MODIFY: add verdict generation + DataStore write after fetch
```

### Cross-Story Dependencies

- **Depends on Story 1.3**: `ForecastDao.queryByTimeWindow()`, `ForecastHour` entity, `DataStore<Preferences>`, `PreferenceKeys.kt`, `ForecastRefreshWorker` skeleton
- **Provides to Story 1.5**: All DataStore keys populated with display-ready strings; `KEY_LAST_UPDATE_EPOCH` freshness signal; `KEY_STALENESS_FLAG` signal; complete `VerdictResult` written atomically
- **Provides to Story 1.7**: `KEY_BRING_LIST`, `KEY_BEST_WINDOW` for hourly view context
- **Provides to Story 1.8**: `KEY_MOOD_LINE` for the shareable mood card

### References

- [Source: architecture.md#Communication Patterns] — Workers write display-ready strings to DataStore; key write order
- [Source: architecture.md#Enforcement Guidelines] — Rule 5: Workers write display-ready strings, not raw objects
- [Source: architecture.md#Data Architecture] — DataStore fields and rationale
- [Source: epics.md#Story 1.4] — All acceptance criteria definitions

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

None.

### Completion Notes List

**[Staleness: Two Independent Mechanisms]**
`KEY_STALENESS_FLAG` = set true before fetch, cleared on success; early warning signal.
`KEY_LAST_UPDATE_EPOCH` = written last on success only; widget staleness threshold = currentTime - lastEpoch > 1800s.

**[All-clear condition simplified]**
All-clear = `bringList.isEmpty() && bestWindow != null`. No separate dominant weather code check — a clear best window implicitly requires favorable weather codes (0–3).

**[Test timezone independence]**
`clearDayHours()` uses `precipProb=0.12` and `weatherCode=3` to avoid sunscreen trigger (which requires weatherCode 0–2 and precipProb < 0.10 in local afternoon hours), while still qualifying as clear window hours (precipProb < 0.20, weatherCode 0–3).

### File List

**New files:**
- `app/src/main/java/com/weatherapp/model/VerdictResult.kt`
- `app/src/main/java/com/weatherapp/model/VerdictGenerator.kt`
- `app/src/test/java/com/weatherapp/model/VerdictGeneratorTest.kt`

**Modified files:**
- `app/src/main/java/com/weatherapp/worker/ForecastRefreshWorker.kt` — added `ForecastDao` injection, verdict generation, atomic DataStore write with correct key order

### Change Log

- Implemented VerdictGenerator pure domain class with clothing verdict, bring list, best window, all-clear, and mood line logic (Date: 2026-03-09)
- Updated ForecastRefreshWorker to generate and atomically write all display-ready strings to DataStore after each successful fetch (Date: 2026-03-09)
