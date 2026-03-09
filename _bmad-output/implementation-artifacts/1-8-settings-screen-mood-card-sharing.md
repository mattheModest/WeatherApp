# Story 1.8: Settings Screen & Mood Card Sharing

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a user,
I want a settings screen where I can configure preferences, access the premium upgrade, and share today's mood card,
so that I can customise the experience and spread the app to friends.

## Acceptance Criteria

**AC-1:** Given `SettingsScreen` is opened via deliberate navigation when reviewed then it contains: temperature unit toggle (°C/°F), notification preference toggle, premium upgrade entry point, and a "Share Today's Mood" action.

**AC-2:** Given the premium upgrade entry point in Settings when viewed by a free-tier user then it is displayed as a calm, informational option — not a nag, not a locked-door visual; no upgrade prompt appears anywhere in the widget.

**AC-3:** Given the user taps "Share Today's Mood" when the share action fires then Android's share sheet opens with the mood card content (mood line text + app attribution) ready to send via any installed sharing app.

**AC-4:** Given `SettingsViewModel` when it exposes state then it is typed as `StateFlow<UiState<SettingsState>>` — never raw nullable.

**AC-5:** Given the user toggles the temperature unit when the setting is saved then `DataStore[KEY_TEMP_UNIT]` is updated and the hourly detail view reflects the new unit on next render.

**AC-6:** Given `SettingsScreen` when reviewed then it is accessible only through deliberate navigation — there is no path from the widget to the settings screen that surfaces premium prompts unexpectedly.

## Tasks / Subtasks

- [ ] Task 1: Create `SettingsState.kt` data class (AC: 1, 4)
  - [ ] 1.1 Create `app/src/main/java/com/weatherapp/ui/settings/SettingsState.kt`
  - [ ] 1.2 Define: `data class SettingsState(val tempUnit: TempUnit, val notificationsEnabled: Boolean, val isPremium: Boolean, val moodLine: String, val shareText: String)`
  - [ ] 1.3 Define: `enum class TempUnit { CELSIUS, FAHRENHEIT }`
  - [ ] 1.4 The `shareText` field is the formatted mood card text, pre-built by ViewModel for easy sharing

- [ ] Task 2: Create `SettingsViewModel.kt` (AC: 4, 5)
  - [ ] 2.1 Create `app/src/main/java/com/weatherapp/ui/settings/SettingsViewModel.kt`
  - [ ] 2.2 Annotate with `@HiltViewModel`, inject `DataStore<Preferences>`
  - [ ] 2.3 Expose `val uiState: StateFlow<UiState<SettingsState>>` initialized to `UiState.Loading`, populated from DataStore in `init`
  - [ ] 2.4 In `init`, collect DataStore preferences and map to `SettingsState`:
    - Read `KEY_TEMP_UNIT` → `TempUnit.CELSIUS` or `TempUnit.FAHRENHEIT`
    - Read `KEY_IS_PREMIUM` → `isPremium`
    - Read `KEY_MOOD_LINE` → `moodLine` (current day's mood line for sharing)
    - Build `shareText = "\"$moodLine\" — WeatherApp"` for share sheet
  - [ ] 2.5 Implement `fun onTempUnitToggled()`: flip between CELSIUS/FAHRENHEIT, write `KEY_TEMP_UNIT` to DataStore
  - [ ] 2.6 Implement `fun onNotificationsToggled()`: read/write notification preference — store in `KEY_NOTIFICATIONS_ENABLED` DataStore key (add to `PreferenceKeys.kt`)
  - [ ] 2.7 Use `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)`

- [ ] Task 3: Create `SettingsScreen.kt` composable (AC: 1, 2, 3, 6)
  - [ ] 3.1 Create `app/src/main/java/com/weatherapp/ui/settings/SettingsScreen.kt`
  - [ ] 3.2 Collect `viewModel.uiState` with `collectAsStateWithLifecycle()`
  - [ ] 3.3 Render the temperature unit toggle as a `Switch` or segmented control (°C / °F) with label "Temperature units"
  - [ ] 3.4 Render notifications toggle as a `Switch` with label "Weather alerts"
  - [ ] 3.5 Render premium upgrade entry point as a `ListItem` or `Card` with: title "WeatherApp Premium", subtitle "Event-specific forecasts, change-triggered alerts, and more." with a "Learn more" or "Try Premium" CTA — displayed as calm informational option, NOT a locked door (AC-2)
  - [ ] 3.6 Render "Share Today's Mood" as a `Button` or `TextButton`
  - [ ] 3.7 On "Share Today's Mood" tap: launch Android share sheet via `Intent(Intent.ACTION_SEND)` with the `shareText` from `SettingsState`
  - [ ] 3.8 All UI elements use `sp` for text sizes; all interactive elements have minimum 48×48dp touch targets
  - [ ] 3.9 Navigation to `SettingsScreen` only from deliberate user action (e.g., settings icon in app bar) — never auto-navigated from widget

- [ ] Task 4: Implement the share sheet action (AC: 3)
  - [ ] 4.1 In `SettingsScreen.kt`, create a share launcher:
    ```kotlin
    val context = LocalContext.current
    fun launchShareSheet(text: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_TITLE, "Today's weather mood")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
    }
    ```
  - [ ] 4.2 The share text format: `"\"$moodLine\"\n\nWeatherApp — daily weather in plain language"`
  - [ ] 4.3 Verify the share sheet appears when tested on device/emulator with sharing apps installed

- [ ] Task 5: Add `KEY_NOTIFICATIONS_ENABLED` to `PreferenceKeys.kt` (AC: 1)
  - [ ] 5.1 Add `val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")` to `PreferenceKeys.kt`
  - [ ] 5.2 Default value when unset: `true` (notifications are on by default if permission is granted)
  - [ ] 5.3 `AlertEvaluationWorker` (Story 2.2) will read this key before queuing notifications

- [ ] Task 6: Wire `KEY_TEMP_UNIT` write to DataStore and verify hourly view reads it (AC: 5)
  - [ ] 6.1 Verify `HourlyDetailViewModel` (Story 1.7) reads `KEY_TEMP_UNIT` from DataStore to format temperature display
  - [ ] 6.2 When `SettingsViewModel.onTempUnitToggled()` writes to DataStore, the `HourlyDetailViewModel` will pick up the change on its next collection (via DataStore Flow)
  - [ ] 6.3 Write `KEY_TEMP_UNIT` as string `"celsius"` or `"fahrenheit"` (lowercase string value)

- [ ] Task 7: Wire navigation to `SettingsScreen` from `MainActivity` (AC: 6)
  - [ ] 7.1 In `MainActivity.kt` NavHost or Scaffold, add a navigation route for `SettingsScreen`
  - [ ] 7.2 Add a settings icon to the app bar (top bar) visible from the main screen
  - [ ] 7.3 Verify: the only path to `SettingsScreen` is user tapping the settings icon — no automatic navigation, no widget-triggered navigation

- [ ] Task 8: Premium upgrade entry point — ensure it is calm and informational (AC: 2)
  - [ ] 8.1 The premium section in `SettingsScreen` must NOT look like a locked feature, a paywall, or a nag
  - [ ] 8.2 Suggested design: a clean card with WeatherApp Premium name, 2-3 bullet points of what premium includes, and a single CTA button ("Try Premium" or "Learn More")
  - [ ] 8.3 When `isPremium = true` in `SettingsState`, hide the upgrade card (or replace with "Premium subscriber — thank you!")
  - [ ] 8.4 The premium upgrade flow (Story 3.1 billing) is launched from this button in a future story; for this story, the button can show a placeholder ("Coming soon") or call `Timber.d("Premium upgrade tapped")`

- [ ] Task 9: Write unit tests (AC: 4, 5)
  - [ ] 9.1 Create `app/src/test/java/com/weatherapp/ui/settings/SettingsViewModelTest.kt`
  - [ ] 9.2 Test initial state loads from DataStore correctly → `UiState.Success(SettingsState(...))`
  - [ ] 9.3 Test `onTempUnitToggled()` flips unit and writes to DataStore: CELSIUS → FAHRENHEIT and back
  - [ ] 9.4 Test that `KEY_TEMP_UNIT` writes `"celsius"` or `"fahrenheit"` string (not enum name)
  - [ ] 9.5 Test share text is formatted correctly: `"\"$moodLine\"\n\nWeatherApp — daily weather in plain language"`

## Dev Notes

### Critical Architecture Rules for This Story

**1. StateFlow<UiState<SettingsState>> — Mandatory**

```kotlin
// SettingsViewModel.kt
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    val uiState: StateFlow<UiState<SettingsState>> = dataStore.data
        .map { prefs ->
            val tempUnit = when (prefs[PreferenceKeys.KEY_TEMP_UNIT]) {
                "fahrenheit" -> TempUnit.FAHRENHEIT
                else         -> TempUnit.CELSIUS
            }
            val isPremium = prefs[PreferenceKeys.KEY_IS_PREMIUM] ?: false
            val moodLine = prefs[PreferenceKeys.KEY_MOOD_LINE] ?: "A good day."
            val notificationsEnabled = prefs[PreferenceKeys.KEY_NOTIFICATIONS_ENABLED] ?: true
            val shareText = "\"$moodLine\"\n\nWeatherApp — daily weather in plain language"
            UiState.Success(
                SettingsState(
                    tempUnit = tempUnit,
                    notificationsEnabled = notificationsEnabled,
                    isPremium = isPremium,
                    moodLine = moodLine,
                    shareText = shareText
                )
            ) as UiState<SettingsState>
        }
        .catch { e ->
            Timber.e(e, "Failed to load settings")
            emit(UiState.Error("Could not load settings."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )

    fun onTempUnitToggled() {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[PreferenceKeys.KEY_TEMP_UNIT] ?: "celsius"
                prefs[PreferenceKeys.KEY_TEMP_UNIT] = if (current == "celsius") "fahrenheit" else "celsius"
            }
        }
    }

    fun onNotificationsToggled() {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[PreferenceKeys.KEY_NOTIFICATIONS_ENABLED] ?: true
                prefs[PreferenceKeys.KEY_NOTIFICATIONS_ENABLED] = !current
            }
        }
    }
}
```

**2. Share Sheet Implementation**

```kotlin
// SettingsScreen.kt
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToPremium: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    when (val state = uiState) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Error   -> ErrorText(state.message)
        is UiState.Success -> SettingsContent(
            state = state.data,
            onTempUnitToggled = viewModel::onTempUnitToggled,
            onNotificationsToggled = viewModel::onNotificationsToggled,
            onShareMood = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, state.data.shareText)
                    putExtra(Intent.EXTRA_TITLE, "Today's weather mood")
                }
                context.startActivity(Intent.createChooser(intent, null))
            },
            onUpgradeTapped = onNavigateToPremium
        )
    }
}
```

**3. Premium Entry Point Design — Calm and Informational**

The premium section must feel like a feature announcement, not a paywall:

```kotlin
// Inside SettingsContent
if (!state.isPremium) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "WeatherApp Premium",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "• Event-specific forecasts on your widget\n• Change-triggered alerts for your plans\n• Travel pre-load for non-home events",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onUpgradeTapped) {
                Text("Learn More — \$7.99/year")
            }
        }
    }
} else {
    ListItem(
        headlineContent = { Text("WeatherApp Premium") },
        supportingContent = { Text("Thank you for subscribing!") },
        leadingContent = { Icon(Icons.Default.Star, contentDescription = null) }
    )
}
```

**4. Temperature Unit Write — String Value**

```kotlin
// Write as lowercase string — not enum name
prefs[PreferenceKeys.KEY_TEMP_UNIT] = "fahrenheit"  // or "celsius"
// NOT: prefs[PreferenceKeys.KEY_TEMP_UNIT] = TempUnit.FAHRENHEIT.name
```

This ensures the `HourlyDetailViewModel` (and any future consumer) uses a stable, predictable string value.

**5. KEY_NOTIFICATIONS_ENABLED — New Key**

Add to `PreferenceKeys.kt`:
```kotlin
val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
```

This key is read by `AlertEvaluationWorker` (Story 2.2) to determine whether to queue notifications:
```kotlin
// AlertEvaluationWorker will read this:
val notificationsEnabled = dataStore.data.first()[PreferenceKeys.KEY_NOTIFICATIONS_ENABLED] ?: true
```

**6. Navigation to SettingsScreen — Deliberate Only**

```kotlin
// MainActivity.kt NavHost
NavHost(navController = navController, startDestination = "main") {
    composable("main") {
        MainScreen(
            onOpenSettings = { navController.navigate("settings") },
            onOpenHourly = { showHourlySheet = true }
        )
    }
    composable("settings") {
        SettingsScreen(
            onNavigateToPremium = { navController.navigate("premium") } // Story 3.1
        )
    }
}
```

The settings icon must be in the top app bar of the main screen, not in the widget or any automatically-navigated screen.

**7. Anti-Patterns to Avoid**

- NEVER show a "locked" or "degraded" premium teaser in the widget — the upgrade path is ONLY in `SettingsScreen`
- NEVER expose `var state: SettingsState?` from ViewModel — always `StateFlow<UiState<SettingsState>>`
- NEVER hardcode `"celsius"` or `"fahrenheit"` inline at call sites — read from `PreferenceKeys.KEY_TEMP_UNIT`
- NEVER use `Log.*` — use `Timber.d/i/w/e()`
- NEVER navigate to SettingsScreen automatically or from the widget

### Project Structure Notes

Files to create in this story:
```
app/src/main/java/com/weatherapp/
  ui/
    settings/
      SettingsScreen.kt            ← NEW: composable with all settings UI
      SettingsViewModel.kt         ← NEW: StateFlow<UiState<SettingsState>>
      SettingsState.kt             ← NEW: data class + TempUnit enum

app/src/test/java/com/weatherapp/
  ui/
    settings/
      SettingsViewModelTest.kt     ← NEW
```

Files to modify in this story:
```
app/src/main/java/com/weatherapp/
  data/datastore/
    PreferenceKeys.kt              ← MODIFY: add KEY_NOTIFICATIONS_ENABLED
  MainActivity.kt                  ← MODIFY: add Settings navigation route + app bar icon
```

### Cross-Story Dependencies

- **Depends on Story 1.3**: `PreferenceKeys.kt`, `DataStore<Preferences>` provider, `KEY_TEMP_UNIT`, `KEY_IS_PREMIUM`, `KEY_MOOD_LINE`
- **Depends on Story 1.4**: `KEY_MOOD_LINE` populated with mood line text by `ForecastRefreshWorker`
- **Provides to Story 2.2**: `KEY_NOTIFICATIONS_ENABLED` DataStore key; `AlertEvaluationWorker` reads this before queuing notifications
- **Provides to Story 3.1**: Premium upgrade CTA in `SettingsScreen` where billing flow will be triggered; `isPremium` state displayed correctly

### References

- [Source: architecture.md#Frontend Architecture] — MVVM + Repository + StateFlow pattern
- [Source: architecture.md#Format Patterns] — UiState sealed class
- [Source: epics.md#Story 1.8] — All acceptance criteria including premium calm entry point, share sheet
- [Source: architecture.md#Enforcement Guidelines] — Rule 4: All ViewModel state as `StateFlow<UiState<T>>`

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

### Completion Notes List

### File List
