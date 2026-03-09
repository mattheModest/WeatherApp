# Story 1.7: Hourly Detail View

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a user,
I want to tap the widget and see an hourly weather breakdown for today,
so that I can look up the detail behind the verdict when I want it, without it being the default view.

## Acceptance Criteria

**AC-1:** Given the user taps the home screen widget when the tap action fires then the app opens and `HourlyDetailBottomSheet` is displayed, showing today's hourly forecast.

**AC-2:** Given `HourlyDetailBottomSheet` is open when the user views each hour row via `HourlyDetailRow` then each row displays: hour label, clothing-language verdict (primary, prominent), temperature in °C (secondary, smaller) — raw temperature is never the primary element.

**AC-3:** Given 48 hours of forecast data in Room when the hourly view renders then it shows hours from the current hour through end of day; past hours are not shown.

**AC-4:** Given `HourlyDetailViewModel` when it exposes state then it is typed as `StateFlow<UiState<List<HourlyDetailRow>>>` — never a raw nullable or bare list.

**AC-5:** Given the hourly bottom sheet is open when the user swipes down to dismiss then the bottom sheet closes and the user is returned to the underlying screen.

**AC-6:** Given the device has TalkBack active when the user navigates the hourly list then each row is announced with hour + verdict + temperature in logical reading order.

## Tasks / Subtasks

- [ ] Task 1: Create `HourlyDetailRow.kt` data class / composable (AC: 2, 6)
  - [ ] 1.1 Create `app/src/main/java/com/weatherapp/ui/hourly/HourlyDetailRow.kt`
  - [ ] 1.2 Define the data class: `data class HourlyDetailRow(val hourLabel: String, val verdictText: String, val temperatureCelsius: Double, val temperatureDisplay: String)`
  - [ ] 1.3 The `temperatureDisplay` is pre-formatted by the ViewModel respecting `KEY_TEMP_UNIT` preference (e.g., "14°C" or "57°F")
  - [ ] 1.4 Create the composable `@Composable fun HourlyDetailRow(row: HourlyDetailRow)` displaying:
    - Hour label (small, secondary weight, e.g., "2pm")
    - Verdict text (primary, `FontWeight.Medium`, prominent size — dominant element)
    - Temperature (secondary, smaller size, below or beside verdict)
  - [ ] 1.5 Set `Modifier.semantics { contentDescription = "${row.hourLabel}. ${row.verdictText}. ${row.temperatureDisplay}" }` for TalkBack (AC-6)
  - [ ] 1.6 Minimum 48×48dp touch target for the row container

- [ ] Task 2: Create `HourlyDetailViewModel.kt` (AC: 4)
  - [ ] 2.1 Create `app/src/main/java/com/weatherapp/ui/hourly/HourlyDetailViewModel.kt`
  - [ ] 2.2 Annotate with `@HiltViewModel`, inject `WeatherRepository` and `DataStore<Preferences>`
  - [ ] 2.3 Expose `val uiState: StateFlow<UiState<List<HourlyDetailRow>>>` — NEVER raw nullable or `List<HourlyDetailRow>?`
  - [ ] 2.4 Initialize with `UiState.Loading`
  - [ ] 2.5 In `init { }`, collect today's forecast from `WeatherRepository.getHourlyForecast(startEpoch, endEpoch)` using `viewModelScope.launch`
  - [ ] 2.6 Filter to hours from `currentHour` (inclusive) through end of today — past hours excluded (AC-3)
  - [ ] 2.7 Read `KEY_TEMP_UNIT` from DataStore to format temperature display string
  - [ ] 2.8 Map `ForecastHour` to `HourlyDetailRow` using `VerdictGenerator` temperature → clothing language logic
  - [ ] 2.9 On success: emit `UiState.Success(rows)`; on error: emit `UiState.Error(message)`
  - [ ] 2.10 Use `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)`

- [ ] Task 3: Create `HourlyDetailBottomSheet.kt` composable (AC: 1, 2, 3, 5)
  - [ ] 3.1 Create `app/src/main/java/com/weatherapp/ui/hourly/HourlyDetailBottomSheet.kt`
  - [ ] 3.2 Implement as a `ModalBottomSheet` (from `androidx.compose.material3`) with `onDismissRequest` callback
  - [ ] 3.3 Collect `viewModel.uiState` with `collectAsStateWithLifecycle()`
  - [ ] 3.4 Render `UiState.Loading`: show a `CircularProgressIndicator` in `weatherAccent` color
  - [ ] 3.5 Render `UiState.Success(rows)`: `LazyColumn` of `HourlyDetailRow` composables
  - [ ] 3.6 Render `UiState.Error(message)`: show error text with a retry option
  - [ ] 3.7 Swipe-to-dismiss is handled automatically by `ModalBottomSheet` — verify `SheetState` behavior
  - [ ] 3.8 Bottom sheet header: show "Today's Forecast" title in `MaterialTheme.typography.titleMedium`

- [ ] Task 4: Wire navigation from widget tap to hourly detail (AC: 1)
  - [ ] 4.1 In `MainActivity.kt`, check for intent extra `EXTRA_OPEN_HOURLY` (defined in Story 1.5)
  - [ ] 4.2 If extra is present (`intent.getBooleanExtra(EXTRA_OPEN_HOURLY_KEY, false)`), set a state flag to show the bottom sheet
  - [ ] 4.3 Pass this flag to the NavHost/Scaffold so `HourlyDetailBottomSheet` is shown immediately on launch
  - [ ] 4.4 Handle `onNewIntent()` in `MainActivity` for cases where app is already running when widget is tapped

- [ ] Task 5: Implement temperature unit conversion logic (AC: 2)
  - [ ] 5.1 Read `KEY_TEMP_UNIT` from DataStore in `HourlyDetailViewModel`
  - [ ] 5.2 Define conversion: if `KEY_TEMP_UNIT == "fahrenheit"`, display as `(celsius * 9/5 + 32).roundToInt()°F`, otherwise display as `celsius.roundToInt()°C`
  - [ ] 5.3 Default unit is Celsius if `KEY_TEMP_UNIT` is not set
  - [ ] 5.4 The verdict (clothing language) is always unit-independent — it's based on the raw Celsius value and converted to language, not displayed as a number

- [ ] Task 6: Add hour-to-clothing-language mapping for rows (AC: 2)
  - [ ] 6.1 In `HourlyDetailViewModel`, reuse `VerdictGenerator.generateClothingVerdict()` logic for each `ForecastHour` to produce per-row clothing language
  - [ ] 6.2 Since `VerdictGenerator.generateVerdict()` takes a full list, extract the per-hour logic into a separate pure function: `fun hourlyClothingVerdict(tempC: Double, precipProb: Double, weatherCode: Int): String`
  - [ ] 6.3 Per-hour verdict is simpler than daily verdict: just temperature band + rain qualifier (e.g., "Light jacket • Rain likely" if precipProb >= 0.4)

- [ ] Task 7: Accessibility implementation (AC: 6)
  - [ ] 7.1 Each `HourlyDetailRow` composable must have `Modifier.semantics` with `contentDescription` combining hour + verdict + temperature
  - [ ] 7.2 Bottom sheet itself must have `contentDescription = "Today's hourly forecast"` on the container
  - [ ] 7.3 Use `Modifier.semantics(mergeDescendants = true)` on the row container to merge all child descriptions into one announcement
  - [ ] 7.4 Test with TalkBack enabled: navigate through the list, confirm each item announces correctly

- [ ] Task 8: Write tests (AC: 3, 4)
  - [ ] 8.1 Create `app/src/test/java/com/weatherapp/ui/hourly/HourlyDetailViewModelTest.kt`
  - [ ] 8.2 Test initial state is `UiState.Loading`
  - [ ] 8.3 Test that hours before the current hour are filtered out (AC-3)
  - [ ] 8.4 Test temperature display with Celsius setting → "14°C"
  - [ ] 8.5 Test temperature display with Fahrenheit setting → correct conversion
  - [ ] 8.6 Test uiState type is `StateFlow<UiState<List<HourlyDetailRow>>>` — never a nullable

## Dev Notes

### Critical Architecture Rules for This Story

**1. StateFlow<UiState<T>> — Mandatory**

```kotlin
// HourlyDetailViewModel.kt
@HiltViewModel
class HourlyDetailViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    val uiState: StateFlow<UiState<List<HourlyDetailRow>>> = flow {
        emit(UiState.Loading)
        try {
            val nowSeconds = System.currentTimeMillis() / 1000L
            // Filter to current hour through end of today
            val endOfDay = nowSeconds - (nowSeconds % 86400L) + 86400L
            weatherRepository.getHourlyForecast(nowSeconds, endOfDay)
                .collect { hours ->
                    val tempUnit = dataStore.data.first()[PreferenceKeys.KEY_TEMP_UNIT] ?: "celsius"
                    val rows = hours.map { hour -> hour.toHourlyDetailRow(tempUnit) }
                    emit(UiState.Success(rows))
                }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load hourly forecast")
            emit(UiState.Error("Could not load forecast. Please try again."))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState.Loading
    )
}
```

**2. Hourly Detail Row — Verdict is Primary**

The design requires clothing-language verdict to be the PRIMARY visual element in each row. Temperature is secondary:

```kotlin
// HourlyDetailRow.kt composable
@Composable
fun HourlyDetailRow(row: HourlyDetailRow, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "${row.hourLabel}. ${row.verdictText}. ${row.temperatureDisplay}"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hour label — left column, small secondary text
        Text(
            text = row.hourLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(48.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            // Verdict — PRIMARY element, prominent
            Text(
                text = row.verdictText,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            // Temperature — SECONDARY element, smaller
            Text(
                text = row.temperatureDisplay,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

**3. Filtering Past Hours**

```kotlin
// In HourlyDetailViewModel — filter to current and future hours only
private fun filterToFutureHours(hours: List<ForecastHour>): List<ForecastHour> {
    val currentHourEpoch = System.currentTimeMillis() / 1000L
    // Round down to the start of the current hour
    val currentHourStart = currentHourEpoch - (currentHourEpoch % 3600L)
    return hours.filter { it.hourEpoch >= currentHourStart }
}
```

**4. Temperature Unit Conversion**

```kotlin
// Extension function in HourlyDetailViewModel or a utility
private fun Double.toDisplayTemperature(unit: String): String {
    return when (unit) {
        "fahrenheit" -> "${(this * 9.0 / 5.0 + 32).roundToInt()}°F"
        else         -> "${this.roundToInt()}°C"
    }
}
```

**5. Per-Hour Clothing Language**

Each hour needs its own clothing verdict. Extract from `VerdictGenerator`:

```kotlin
fun hourlyClothingVerdict(tempC: Double, precipProb: Double): String {
    val base = when {
        tempC >= 28 -> "No jacket needed"
        tempC >= 20 -> "Light layers"
        tempC >= 12 -> "Light jacket"
        tempC >= 5  -> "Jacket"
        else        -> "Bundle up"
    }
    return if (precipProb >= 0.40) "$base · Rain likely" else base
}
```

**6. Widget Tap → Bottom Sheet Navigation Flow**

```
Widget tap
  → Glance actionStartActivity<MainActivity>(extras: EXTRA_OPEN_HOURLY=true)
  → MainActivity.onCreate() / onNewIntent()
    → reads intent extra
    → sets showHourlyBottomSheet = true in state
  → NavHost renders main screen
  → HourlyDetailBottomSheet shows immediately if showHourlyBottomSheet = true
```

The bottom sheet is part of the main screen Scaffold, not a separate navigation destination. This keeps the tap-to-open flow fast.

**7. ModalBottomSheet Usage**

```kotlin
// In main screen composable (or MainActivity-level Scaffold)
var showHourlySheet by remember { mutableStateOf(openHourly) }

if (showHourlySheet) {
    HourlyDetailBottomSheet(
        onDismiss = { showHourlySheet = false }
    )
}

// HourlyDetailBottomSheet.kt
@Composable
fun HourlyDetailBottomSheet(onDismiss: () -> Unit) {
    val viewModel: HourlyDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        when (val state = uiState) {
            is UiState.Loading -> CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            is UiState.Success -> LazyColumn {
                item {
                    Text(
                        text = "Today's Forecast",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                items(state.data, key = { it.hourLabel }) { row ->
                    HourlyDetailRow(row)
                    HorizontalDivider()
                }
            }
            is UiState.Error -> Text(
                text = state.message,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
```

**8. Anti-Patterns to Avoid**

- NEVER make temperature the primary/largest text element — verdict text is primary
- NEVER expose `var state: List<HourlyDetailRow>?` from ViewModel — always `StateFlow<UiState<T>>`
- NEVER show hours that have already passed — filter to current hour and beyond
- NEVER use `Log.*` — use `Timber.d/i/w/e()`
- NEVER call `ForecastDao` directly from ViewModel — go through `WeatherRepository`

### Project Structure Notes

Files to create in this story:
```
app/src/main/java/com/weatherapp/
  ui/
    hourly/
      HourlyDetailBottomSheet.kt   ← NEW: ModalBottomSheet with LazyColumn of rows
      HourlyDetailViewModel.kt     ← NEW: StateFlow<UiState<List<HourlyDetailRow>>>
      HourlyDetailRow.kt           ← NEW: data class + composable for single hour row

app/src/test/java/com/weatherapp/
  ui/
    hourly/
      HourlyDetailViewModelTest.kt ← NEW
```

Files to modify in this story:
```
app/src/main/java/com/weatherapp/
  MainActivity.kt                  ← MODIFY: handle EXTRA_OPEN_HOURLY intent extra + showHourlySheet state
  model/
    VerdictGenerator.kt             ← MODIFY: extract hourlyClothingVerdict() as a standalone function
```

### Cross-Story Dependencies

- **Depends on Story 1.3**: `WeatherRepository.getHourlyForecast()`, `ForecastHour` entity
- **Depends on Story 1.4**: `VerdictGenerator` temperature → clothing language logic (reuse for per-hour verdict)
- **Depends on Story 1.5**: `EXTRA_OPEN_HOURLY` intent extra constant in `MainActivity`; `WeatherWidgetContent.kt` tap action already wired to launch MainActivity
- **Depends on Story 1.8**: `KEY_TEMP_UNIT` DataStore key for temperature unit preference (created in Story 1.3 PreferenceKeys.kt, written in Story 1.8)
- **Provides to nothing directly** — this is a terminal UI story; other stories do not build on top of this

### References

- [Source: architecture.md#Frontend Architecture] — MVVM + Repository + StateFlow; `StateFlow<UiState<T>>`
- [Source: architecture.md#Requirements to Structure Mapping] — FR-006 files: `ui/hourly/` all three files
- [Source: epics.md#Story 1.7] — All acceptance criteria
- [Source: architecture.md#Format Patterns] — UiState sealed class definition

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

### Completion Notes List

### File List
