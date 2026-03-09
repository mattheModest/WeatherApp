# Story 1.4: Verdict Generation Engine

Status: ready-for-dev

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

- [ ] Task 1: Create `VerdictGenerator.kt` domain logic class (AC: 1, 6)
  - [ ] 1.1 Create `app/src/main/java/com/weatherapp/model/VerdictGenerator.kt`
  - [ ] 1.2 Implement `fun generateVerdict(hourlyData: List<ForecastHour>): VerdictResult` where `VerdictResult` is a data class holding `verdictText`, `bringList`, `bestWindow`, `isAllClear`, `moodLine`
  - [ ] 1.3 Define temperature range thresholds (in Celsius): hot ≥ 28°C → "No jacket needed", warm 20–27°C → "Light layers", mild 12–19°C → "Light jacket weather", cool 5–11°C → "Jacket weather", cold < 5°C → "Bundle up"
  - [ ] 1.4 Temperature mapping uses `temperature_c` from `ForecastHour`; never display raw temperature values in verdict text

- [ ] Task 2: Implement bring list logic (AC: 2, 3, 4)
  - [ ] 2.1 In `VerdictGenerator.kt`, define umbrella threshold: `precipitationProbability >= 0.40` (40%) for any hour during the day
  - [ ] 2.2 Define sunscreen threshold: UV index context from `weather_code` — use WMO weather codes 0–2 (clear sky) combined with afternoon hours (11:00–17:00 in local time); `precipitationProbability < 0.10` as proxy for sunny conditions warranting sunscreen
  - [ ] 2.3 Implement `fun evaluateBringList(hourlyData: List<ForecastHour>): List<String>` — returns ordered list of bring items or empty list
  - [ ] 2.4 Bring list items are serialized as a pipe-delimited string for DataStore storage (e.g., `"Bring an umbrella|Sunscreen today"`)
  - [ ] 2.5 Empty bring list → write empty string `""` to `KEY_BRING_LIST`

- [ ] Task 3: Implement best outdoor window logic (AC: 5)
  - [ ] 3.1 In `VerdictGenerator.kt`, implement `fun calculateBestWindow(hourlyData: List<ForecastHour>): String?`
  - [ ] 3.2 "Clear" hour criteria: `precipitationProbability < 0.20` AND `windSpeedKmh < 30` AND `weatherCode` in clear/partly cloudy range (0–3)
  - [ ] 3.3 Daylight window: filter to hours between 7:00 AM and 8:00 PM local time
  - [ ] 3.4 Find longest consecutive sequence of clear hours ≥ 2; format as "Best time outside: {startHour}–{endHour}" (e.g., "Best time outside: 11am–2pm")
  - [ ] 3.5 If no 2-hour clear window exists, write `""` to `KEY_BEST_WINDOW` (widget hides the field)

- [ ] Task 4: Implement all-clear detection (AC: 6)
  - [ ] 4.1 All-clear criteria: no umbrella trigger, best window exists, and dominant weather code is 0–3 (clear/partly cloudy)
  - [ ] 4.2 When all-clear: `KEY_ALL_CLEAR = true`, `KEY_WIDGET_VERDICT` = "You're good. Go live your day." (or equivalent confident all-clear text)
  - [ ] 4.3 When not all-clear: `KEY_ALL_CLEAR = false`, `KEY_WIDGET_VERDICT` = clothing-language verdict from Task 1

- [ ] Task 5: Implement mood line generator (AC: 7)
  - [ ] 5.1 In `VerdictGenerator.kt`, implement `fun generateMoodLine(hourlyData: List<ForecastHour>, isAllClear: Boolean): String`
  - [ ] 5.2 Map mood lines to weather conditions:
    - All-clear + warm: "Honestly lovely today. Eat lunch outside."
    - All-clear + mild: "A really good day. Don't forget to step out."
    - Rain: "A good day to stay cosy. Or embrace the drizzle."
    - Windy: "Breezy today. Tie your hat down."
    - Storm: "Stay in if you can. This one means it."
    - Overcast/mild: "Grey but manageable. You've got this."
  - [ ] 5.3 Mood line must always be a complete, conversational sentence — never a fragment or raw data

- [ ] Task 6: Integrate verdict generation into `ForecastRefreshWorker` (AC: 1–8)
  - [ ] 6.1 In `ForecastRefreshWorker.doWork()`, after successful `weatherRepository.fetchForecast()`, query today's forecast hours from `ForecastDao`
  - [ ] 6.2 Pass hourly data to `VerdictGenerator.generateVerdict()`
  - [ ] 6.3 Write all DataStore keys in this order: `KEY_WIDGET_VERDICT`, `KEY_BRING_LIST`, `KEY_BEST_WINDOW`, `KEY_ALL_CLEAR`, `KEY_MOOD_LINE`, then finally `KEY_LAST_UPDATE_EPOCH` (LAST — widget uses this as freshness signal)
  - [ ] 6.4 All writes must be in a single `dataStore.edit { prefs -> ... }` block to be atomic
  - [ ] 6.5 Staleness flag (`KEY_STALENESS_FLAG`) is cleared in the same edit block as `KEY_LAST_UPDATE_EPOCH`

- [ ] Task 7: Staleness signal logic (AC: 9)
  - [ ] 7.1 Staleness is determined by the widget reading `KEY_LAST_UPDATE_EPOCH` and comparing to current time
  - [ ] 7.2 If `currentTimeSeconds - KEY_LAST_UPDATE_EPOCH > 1800` (30 minutes), display staleness indicator
  - [ ] 7.3 `KEY_STALENESS_FLAG` (boolean) is an independent early-warning signal set by `ForecastRefreshWorker` BEFORE a refresh attempt; the widget can check either signal
  - [ ] 7.4 Document clearly: staleness flag = "refresh in progress or failed"; last_update_epoch = "time of last successful write"

- [ ] Task 8: Create `VerdictResult` model (AC: 1–7)
  - [ ] 8.1 Create `app/src/main/java/com/weatherapp/model/VerdictResult.kt`
  - [ ] 8.2 `data class VerdictResult(val verdictText: String, val bringList: List<String>, val bestWindow: String?, val isAllClear: Boolean, val moodLine: String)`
  - [ ] 8.3 This model is internal to the Worker/domain layer — never passed to Glance composables (those read DataStore strings directly)

- [ ] Task 9: Write unit tests for verdict generation logic (AC: 1–7)
  - [ ] 9.1 `VerdictGeneratorTest.kt` at `src/test/java/com/weatherapp/model/VerdictGeneratorTest.kt`
  - [ ] 9.2 Test each temperature band produces correct clothing language
  - [ ] 9.3 Test umbrella trigger at exactly 40% precipitation probability (boundary condition)
  - [ ] 9.4 Test bring list is empty when precipitation = 0% and no sunny afternoon conditions
  - [ ] 9.5 Test best window: 3 consecutive clear hours → correct time range string
  - [ ] 9.6 Test best window: only 1 clear hour → returns null (no window displayed)
  - [ ] 9.7 Test all-clear: all criteria met → `isAllClear = true`, verdict is the all-clear message
  - [ ] 9.8 Test mood line appropriate to each major weather condition category

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

### Completion Notes List

### File List
