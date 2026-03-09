# WeatherApp

**Weather handled. Go live your day.**

WeatherApp inverts the standard weather model: instead of showing you data to interpret, it reads your calendar, checks the forecast, and delivers a verdict. One glance at the widget — you know exactly what to do. No mental mapping required.

[**→ Download APK**](https://github.com/matthemodest/WeatherApp/releases/latest) · [**Landing page**](https://matthemodest.github.io/WeatherApp)

---

## What it does

The widget is the product. The app is configuration.

| Free tier | Premium ($7.99/year) |
|---|---|
| Clothing-language verdict ("light jacket weather") | Calendar-aware widget shifts ("Your BBQ starts in 2h. You're clear.") |
| Contextual bring list (umbrella, sunscreen) only when warranted | Change-triggered alerts — only fires if forecast changes after an earlier all-clear |
| Best outdoor window for the day | Event-importance-scaled monitoring windows |
| All-clear state ("You're good. Go live your day.") | Conflict detection for overlapping outdoor events |
| Confirmation-first alerts | — |
| Hourly detail on one tap | — |

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
- **Billing** — Google Play Billing Library 7.x ($7.99/year subscription)

## Architecture

```
com.weatherapp/
├── data/
│   ├── billing/        # Play Billing wrapper + repository
│   ├── calendar/       # CalendarContract reader
│   ├── datastore/      # Preference keys + flows
│   ├── db/             # Room: ForecastHour, AlertRecord DAOs
│   ├── location/       # FusedLocationProvider wrapper
│   └── weather/        # Retrofit API + WeatherRepository
├── di/                 # Hilt modules (App, Network, DB, Billing)
├── model/              # VerdictGenerator, AlertStateMachine
├── ui/
│   ├── hourly/         # HourlyDetailBottomSheet + ViewModel
│   ├── main/           # MainScreen (host)
│   ├── onboarding/     # Permission flow + ViewModel
│   ├── settings/       # Settings + ViewModel
│   ├── theme/          # Material3 theme
│   └── widget/         # Glance widget composable
├── util/               # UiState, extensions
└── worker/             # ForecastRefreshWorker, CalendarScanWorker, AlertEvaluationWorker
```

## CI

GitHub Actions runs on every PR to `master`:

- `./gradlew lint`
- `./gradlew test`
- `./gradlew assembleDebug`

Release builds are triggered by pushing a version tag (`v1.0.0`) or via manual workflow dispatch.

## Privacy

- **Location** — used only to fetch the local forecast. Never stored server-side.
- **Calendar** — read locally via `CalendarContract`. Event titles are processed on-device; never sent to any server.
- **No analytics, no ads, no accounts.**

---

*Built by [Lafayette](https://github.com/matthemodest) · Weather data: [Open-Meteo](https://open-meteo.com) (free, no API key required) · Proxy: Cloudflare Workers*
