# Story 1.2: Cloudflare Weather Proxy

Status: review

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a developer,
I want a Cloudflare Worker that proxies Open-Meteo with location-cluster KV caching,
so that the app can fetch weather data within budget while never transmitting raw GPS coordinates.

## Acceptance Criteria

**AC-1:** Given `cloudflare-worker/src/worker.ts` deployed via Wrangler, When a request arrives as `GET /forecast?lat_grid=37.8&lon_grid=-122.4&date=2026-03-08`, Then the Worker checks KV for key `forecast:37.8:-122.4:2026-03-08` and returns the cached response if it exists and is < 30 minutes old.

**AC-2:** Given no cached entry exists in KV, When the Worker receives a forecast request, Then it calls Open-Meteo for the grid coordinates, stores the result in KV with a 30-minute TTL, and returns the response to the device.

**AC-3:** Given the Worker response, When parsed, Then it matches the JSON schema: `{ lat_grid, lon_grid, fetched_at (ISO 8601 UTC), hourly_forecasts: [{ hour_epoch, temperature_c, precipitation_probability, wind_speed_kmh, weather_code }] }` — where `precipitation_probability` is a decimal (0.0–1.0).

**AC-4:** Given Open-Meteo returns an error, When the Worker handles it, Then it returns an HTTP 502 with body `{"message": "upstream error"}` — no envelope wrapper on success responses.

**AC-5:** Given `wrangler.toml`, When reviewed, Then it declares the KV namespace binding and the Worker route; the Worker deploys successfully via `wrangler deploy`.

## Tasks / Subtasks

- [x] Task 1: Initialize cloudflare-worker project (AC: 5)
  - [x] 1.1 Create `cloudflare-worker/package.json` with `@cloudflare/workers-types` and `wrangler` as devDependencies, `typescript` as devDependency
  - [x] 1.2 Create `cloudflare-worker/tsconfig.json` targeting ES2022 with `lib: ["ES2022"]` and `types: ["@cloudflare/workers-types"]`
  - [x] 1.3 Create `cloudflare-worker/wrangler.toml` with worker name, KV namespace binding (`WEATHER_CACHE`), compatibility date, and route
  - [x] 1.4 Verify `npm install` resolves all devDependencies in `cloudflare-worker/`

- [x] Task 2: Implement KV cache check (AC: 1)
  - [x] 2.1 Parse `lat_grid`, `lon_grid`, `date` query params from incoming request URL
  - [x] 2.2 Return HTTP 400 if any required param is missing or malformed (non-numeric lat/lon, invalid date format)
  - [x] 2.3 Construct KV key: `forecast:{lat_grid}:{lon_grid}:{date}`
  - [x] 2.4 Call `env.WEATHER_CACHE.get(key)` — if result exists, return it directly as JSON response with `Content-Type: application/json`

- [x] Task 3: Implement Open-Meteo fetch and KV store (AC: 2, 3)
  - [x] 3.1 On cache miss, construct Open-Meteo URL: `https://api.open-meteo.com/v1/forecast?latitude={lat_grid}&longitude={lon_grid}&hourly=temperature_2m,precipitation_probability,windspeed_10m,weathercode&timezone=UTC&forecast_days=2`
  - [x] 3.2 Fetch from Open-Meteo; if response is not ok, return 502 per AC-4
  - [x] 3.3 Transform Open-Meteo response to internal schema (see Dev Notes for transformation logic)
  - [x] 3.4 Store transformed JSON string in KV with `expirationTtl: 1800` (30 minutes)
  - [x] 3.5 Return transformed response to device as JSON

- [x] Task 4: Implement error handling (AC: 4)
  - [x] 4.1 Any non-ok Open-Meteo response → return `Response` with status 502 and body `{"message": "upstream error"}`
  - [x] 4.2 Network failure fetching Open-Meteo → catch exception, return 502
  - [x] 4.3 Unhandled top-level exception → return 500 with `{"message": "internal error"}`
  - [x] 4.4 Confirm success responses have NO envelope wrapper — raw schema object only

- [x] Task 5: Write unit tests (AC: 1–4)
  - [x] 5.1 Test: cache hit → returns cached JSON without calling Open-Meteo
  - [x] 5.2 Test: cache miss → calls Open-Meteo, stores in KV, returns transformed response
  - [x] 5.3 Test: Open-Meteo 500 → Worker returns 502 `{"message": "upstream error"}`
  - [x] 5.4 Test: missing `lat_grid` param → Worker returns 400
  - [x] 5.5 Test: transformed schema shape matches AC-3 exactly (field names, types, precipitation as decimal)
  - [x] 5.6 Use Vitest + `@cloudflare/vitest-pool-workers` for testing (see Dev Notes)

- [x] Task 6: Validate deploy (AC: 5)
  - [x] 6.1 Run `npx wrangler deploy --dry-run` to validate `wrangler.toml` without deploying
  - [x] 6.2 Confirm KV namespace binding name `WEATHER_CACHE` is declared in `wrangler.toml`
  - [x] 6.3 Document actual deploy command in Dev Agent Record: `npx wrangler deploy` (requires `CLOUDFLARE_API_TOKEN` env var)
  - [x] 6.4 Generate real KV namespace IDs — `wrangler.toml` already contains real IDs (`b4b36dd0...` / `67e2f72a...`). Verified during code review (2026-03-09).

## Dev Notes

### Project Location

All Cloudflare Worker files live under `cloudflare-worker/` at the repo root — established as a scaffold in Story 1.1. This is a **separate project** from the Android app; it has its own `package.json` and is deployed independently via Wrangler.

### `cloudflare-worker/package.json`

```json
{
  "name": "weatherapp-worker",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "deploy": "wrangler deploy",
    "dev": "wrangler dev",
    "test": "vitest"
  },
  "devDependencies": {
    "@cloudflare/vitest-pool-workers": "^0.8.0",
    "@cloudflare/workers-types": "^4.20260101.0",
    "typescript": "^5.7.3",
    "vitest": "^3.0.0",
    "wrangler": "^3.101.0"
  }
}
```

### `cloudflare-worker/tsconfig.json`

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "module": "ES2022",
    "moduleResolution": "bundler",
    "lib": ["ES2022"],
    "types": ["@cloudflare/workers-types"],
    "strict": true,
    "noEmit": true
  },
  "include": ["src/**/*.ts", "test/**/*.ts"]
}
```

### `cloudflare-worker/wrangler.toml`

```toml
name = "weatherapp-worker"
main = "src/worker.ts"
compatibility_date = "2026-01-01"

[[kv_namespaces]]
binding = "WEATHER_CACHE"
id = "REPLACE_WITH_REAL_KV_NAMESPACE_ID"
preview_id = "REPLACE_WITH_PREVIEW_KV_NAMESPACE_ID"

[vars]
OPEN_METEO_BASE_URL = "https://api.open-meteo.com/v1"
```

**Note:** KV namespace IDs are created via `npx wrangler kv namespace create WEATHER_CACHE` and `npx wrangler kv namespace create WEATHER_CACHE --preview`. The dev agent must run these commands and substitute the real IDs before deploying.

### Worker Environment Type

```typescript
export interface Env {
  WEATHER_CACHE: KVNamespace;
  OPEN_METEO_BASE_URL: string;
}
```

### Worker Request/Response Contract

**Incoming request (from Android device):**
```
GET /forecast?lat_grid=37.8&lon_grid=-122.4&date=2026-03-08
```
- `lat_grid` and `lon_grid`: already snapped to 0.1° grid by device (never raw GPS)
- `date`: YYYY-MM-DD format in UTC

**KV key format:**
```
forecast:{lat_grid}:{lon_grid}:{date}
```
Example: `forecast:37.8:-122.4:2026-03-08`

**Success response (HTTP 200) — raw object, no envelope:**
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

**Error response (HTTP 502):**
```json
{"message": "upstream error"}
```

### Open-Meteo API & Transformation Logic

**Open-Meteo URL:**
```
https://api.open-meteo.com/v1/forecast
  ?latitude={lat_grid}
  &longitude={lon_grid}
  &hourly=temperature_2m,precipitation_probability,windspeed_10m,weathercode
  &timezone=UTC
  &forecast_days=2
```

**Open-Meteo response shape (abbreviated):**
```json
{
  "hourly": {
    "time": ["2026-03-08T00:00", "2026-03-08T01:00", ...],
    "temperature_2m": [14.2, 13.8, ...],
    "precipitation_probability": [15, 10, ...],
    "windspeed_10m": [18.0, 16.5, ...],
    "weathercode": [1, 1, ...]
  }
}
```

**Critical transformation rules:**
1. `time` strings → `hour_epoch` (Unix epoch seconds): `new Date(timeStr + "Z").getTime() / 1000`
2. `precipitation_probability` (0–100 integer) → divide by 100 to get decimal (0.0–1.0)
3. `temperature_2m` → `temperature_c` (no conversion needed, already Celsius)
4. `windspeed_10m` → `wind_speed_kmh` (no conversion needed, already km/h)
5. `weathercode` → `weather_code` (rename only)
6. `fetched_at` → `new Date().toISOString()` at time of Worker execution

**Transformation TypeScript:**
```typescript
function transformResponse(
  latGrid: number,
  lonGrid: number,
  openMeteoData: OpenMeteoResponse
): WorkerResponse {
  const { time, temperature_2m, precipitation_probability, windspeed_10m, weathercode } =
    openMeteoData.hourly;

  const hourlyForecasts = time.map((t, i) => ({
    hour_epoch: Math.floor(new Date(t + "Z").getTime() / 1000),
    temperature_c: temperature_2m[i],
    precipitation_probability: precipitation_probability[i] / 100,
    wind_speed_kmh: windspeed_10m[i],
    weather_code: weathercode[i],
  }));

  return {
    lat_grid: latGrid,
    lon_grid: lonGrid,
    fetched_at: new Date().toISOString(),
    hourly_forecasts: hourlyForecasts,
  };
}
```

### Complete `worker.ts` Structure

```typescript
export interface Env {
  WEATHER_CACHE: KVNamespace;
  OPEN_METEO_BASE_URL: string;
}

interface HourlyForecast {
  hour_epoch: number;
  temperature_c: number;
  precipitation_probability: number; // decimal 0.0–1.0
  wind_speed_kmh: number;
  weather_code: number;
}

interface WorkerResponse {
  lat_grid: number;
  lon_grid: number;
  fetched_at: string; // ISO 8601 UTC
  hourly_forecasts: HourlyForecast[];
}

interface OpenMeteoResponse {
  hourly: {
    time: string[];
    temperature_2m: number[];
    precipitation_probability: number[];
    windspeed_10m: number[];
    weathercode: number[];
  };
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    // ... implementation per tasks
  },
};
```

### Testing Setup (`vitest.config.ts`)

```typescript
import { defineWorkersConfig } from "@cloudflare/vitest-pool-workers/config";

export default defineWorkersConfig({
  test: {
    poolOptions: {
      workers: {
        wrangler: { configPath: "./wrangler.toml" },
      },
    },
  },
});
```

Tests live at `cloudflare-worker/test/worker.test.ts`. Use `env.WEATHER_CACHE` mock via the workers pool.

### Cross-Story Context from Story 1.1

- Story 1.1 establishes the `cloudflare-worker/` directory scaffold at repo root
- The Android `WeatherApi.kt` (Story 1.3) will point its Retrofit base URL to this Worker's deployed endpoint
- The Worker URL is the **only** network endpoint the Android app ever calls — no direct Open-Meteo calls from device

### Privacy Enforcement

- The Worker **never** receives raw GPS coordinates — device snaps to 0.1° grid before request
- KV keys contain only `lat_grid`/`lon_grid` (already anonymised): `forecast:37.8:-122.4:2026-03-08`
- No user identifiers, device IDs, or PII ever pass through the Worker

### Project Structure Notes

```
cloudflare-worker/          ← repo root (separate from Android app)
  package.json
  wrangler.toml             ← KV binding + route declaration
  tsconfig.json
  vitest.config.ts
  src/
    worker.ts               ← single file: fetch handler + transformation
  test/
    worker.test.ts          ← Vitest + workers pool tests
```

- Single-file Worker by design — no subdirectories under `src/`
- No build step needed — Wrangler bundles TypeScript directly
- `cloudflare-worker/node_modules/` is gitignored (per Story 1.1 `.gitignore`)

### References

- [Source: _bmad-output/planning-artifacts/architecture.md#API & Communication Patterns] — Cloudflare Worker + KV design, KV key format, 30-min TTL
- [Source: _bmad-output/planning-artifacts/architecture.md#Format Patterns] — Complete response JSON schema, precipitation as decimal
- [Source: _bmad-output/planning-artifacts/architecture.md#Complete Project Directory Structure] — `cloudflare-worker/` file list
- [Source: _bmad-output/planning-artifacts/epics.md#Story 1.2] — Acceptance criteria (BDD formatted)
- [Source: _bmad-output/planning-artifacts/architecture.md#Authentication & Security] — Privacy: device-side coordinate snapping, Worker never receives raw GPS

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

- Epoch calculation: `2026-03-08T00:00:00Z` = `1772928000` (not `1741392000` which is 2025). Test 5.5 initially had the wrong expected epoch; corrected to `1772928000` after first test run showed worker was producing the right value.
- `wrangler.toml` `compatibility_date = "2026-01-01"` causes a miniflare warning during test ("latest supported: 2025-09-06") — this is non-fatal and does not affect test results or production deployment.
- `EBUSY` errors on test teardown are Windows file-lock warnings from miniflare temp dir cleanup — non-fatal, all tests pass.

### Completion Notes List

⚠️ **Code Review Fixes Applied (2026-03-09):**
- [H] Missing test for network-level exception (Task 4.2: `fetch()` throws, not HTTP error) — **fixed**: added test `5.3b` to `worker.test.ts`
- [H] KV namespace IDs — **resolved**: `wrangler.toml` already contained real IDs; Task 6.4 closed.
- [M] No geographic range validation for `lat_grid`/`lon_grid` — **fixed**: added range checks (`lat: -90..90`, `lon: -180..180`) to `worker.ts` after NaN validation
- [M] Added range validation tests for lat/lon out-of-bounds to `worker.test.ts`

- Implemented full Cloudflare Worker at `cloudflare-worker/src/worker.ts`: KV cache check (30-min TTL via `expirationTtl: 1800`), Open-Meteo fetch with transformation, parameter validation (400 for missing/malformed), upstream error handling (502), and top-level error guard (500).
- Transformation: `precipitation_probability` divided by 100 (percentage → decimal 0.0–1.0); `time` strings → `hour_epoch` Unix epoch seconds; field renames as specified.
- 9 unit tests written using Vitest + `@cloudflare/vitest-pool-workers` v0.8.71; all pass. Tests use `SELF.fetch()` for requests, `env.WEATHER_CACHE` for KV assertions, and `fetchMock` (undici MockAgent) for Open-Meteo interception.
- Wrangler dry-run validated: worker bundles to 3.44 KiB, `WEATHER_CACHE` KV binding confirmed.
- Deploy command: `npx wrangler deploy` (requires `CLOUDFLARE_API_TOKEN`). KV namespace IDs are placeholders — must run `npx wrangler kv namespace create WEATHER_CACHE` and `npx wrangler kv namespace create WEATHER_CACHE --preview` and substitute real IDs in `wrangler.toml` before first deploy.

### File List

- `cloudflare-worker/package.json` (modified — added vitest, @cloudflare/vitest-pool-workers, renamed to weatherapp-worker, added test script)
- `cloudflare-worker/tsconfig.json` (modified — upgraded to ES2022, added test dir to include)
- `cloudflare-worker/wrangler.toml` (modified — added KV namespace binding WEATHER_CACHE, OPEN_METEO_BASE_URL var, compatibility_date 2026-01-01)
- `cloudflare-worker/vitest.config.ts` (created — defineWorkersConfig with wrangler.toml reference)
- `cloudflare-worker/src/worker.ts` (modified — full implementation replacing stub)
- `cloudflare-worker/test/worker.test.ts` (created — 9 unit tests covering all ACs)

## Change Log

- 2026-03-09: Story 1.2 implemented — Cloudflare Worker with KV caching, Open-Meteo proxy, request validation, error handling, and full unit test suite (9 tests, all passing). Wrangler dry-run validated.
