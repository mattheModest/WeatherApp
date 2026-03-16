# WeatherApp

**Weather handled. Go live your day.**

WeatherApp inverts the standard weather model: instead of showing you data to interpret, it tells you what to do. One glance at the widget — you know exactly what to wear, what to bring, when to go outside. No mental mapping required.

[**→ Download APK**](https://github.com/matthemodest/WeatherApp/releases/latest) · [**Landing page**](https://matthemodest.github.io/WeatherApp)

---

## What it does

The widget is the product. The app is configuration.

- Clothing-language verdict ("light jacket weather") — never raw temperature
- Contextual bring list (umbrella, sunscreen) only when warranted
- Best outdoor window for the day
- All-clear state ("You're good. Go live your day.")
- Confirmation-first alerts — proactively confirms good news, escalates only on genuine change
- Hourly detail one tap behind the widget
- 3 personality cores (Frank, Kelvin, Graves) — rotate in settings
- 7 visual themes
- Calendar-aware widget shifts for upcoming outdoor events
- Change-triggered alerts when forecast changes after an earlier all-clear

## Install (sideload)

1. Download `WeatherApp-vX.X.X.apk` from [Releases](https://github.com/matthemodest/WeatherApp/releases/latest)
2. On your Android device: **Settings → Apps → Special app access → Install unknown apps** → allow your browser or file manager
3. Open the APK and tap **Install**
4. Launch WeatherApp — onboarding takes under 60 seconds

> **Requires Android 14+** (API 34). Signed with a debug key; Android will show an "unknown source" warning — expected.

## Tech stack

- **Android** — Kotlin, Jetpack Compose, Material3
- **Widget** — Jetpack Glance
- **Background** — WorkManager (30-min refresh cycle)
- **Data** — Room (forecast cache + alert records), DataStore (preferences)
- **Network** — Retrofit → Cloudflare Worker → Open-Meteo API
- **DI** — Hilt

## Architecture

```
com.weatherapp/
├── data/
│   ├── calendar/       # CalendarContract reader
│   ├── datastore/      # Preference keys + flows
│   ├── db/             # Room: ForecastHour, AlertRecord DAOs
│   ├── location/       # FusedLocationProvider wrapper
│   └── weather/        # Retrofit API + WeatherRepository
├── di/                 # Hilt modules
├── model/              # VerdictGenerator, personality pools, AlertStateMachine
├── ui/
│   ├── hourly/         # HourlyDetailBottomSheet + ViewModel
│   ├── main/           # MainScreen
│   ├── onboarding/     # Permission flow + ViewModel
│   ├── settings/       # Settings + ViewModel
│   ├── theme/          # Material3 theme + design tokens
│   └── widget/         # Glance widget composable
├── util/               # UiState, extensions
└── worker/             # ForecastRefreshWorker, CalendarScanWorker, AlertEvaluationWorker
```

## CI

GitHub Actions runs on every push to `master` and every PR:

- `./gradlew lint`
- `./gradlew test`
- `./gradlew assembleDebug`

## Privacy

- **Location** — used only to fetch the local forecast. Never stored server-side. Coordinates are snapped to a 0.1° grid before any network request.
- **Calendar** — read locally via `CalendarContract`. Event titles are processed on-device; never sent to any server.
- **No analytics, no ads, no accounts.**

---

*Built by [Lafayette](https://github.com/matthemodest) · Weather data: [Open-Meteo](https://open-meteo.com) · Proxy: Cloudflare Workers*
