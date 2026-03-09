# Story 1.3: Weather Data Layer

Status: ready-for-dev

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a developer,
I want Room, DataStore, LocationRepository, and WeatherRepository wired together with a 30-minute WorkManager refresh cycle,
so that forecast data flows from the Cloudflare proxy into local storage and is continuously kept fresh.

## Acceptance Criteria

**AC-1:** Given `PreferenceKeys.kt` when reviewed then it is the single source of truth for all DataStore key strings; no inline string literals exist at any call site.

**AC-2:** Given `AppDatabase` when initialized then it includes the `ForecastHour` entity (table: `forecast_hour`) with columns `hour_epoch`, `temperature_c`, `precipitation_probability`, `wind_speed_kmh`, `weather_code`.

**AC-3:** Given `ForecastDao` when called then it supports `insert(List<ForecastHour>)`, `queryByTimeWindow(startEpoch, endEpoch): Flow<List<ForecastHour>>`, and `deleteExpired(beforeEpoch)`.

**AC-4:** Given raw GPS coordinates from `LocationRepository` when passed to any network call then they are first snapped via `Double.snapToGrid(0.1)` in `CoordinateUtils.kt` — raw coordinates never reach `WeatherApi`.

**AC-5:** Given `WeatherRepository.fetchForecast()` when the network succeeds then it writes hourly rows to Room via `ForecastDao` and returns `Result.success()`.

**AC-6:** Given the network fails with `IOException` when `WeatherRepository.fetchForecast()` is called then it returns `Result.failure()` without throwing across the layer boundary.

**AC-7:** Given `ForecastRefreshWorker` registered as a `PeriodicWorkRequest` with a 30-minute interval when it runs then it calls `WeatherRepository.fetchForecast()`, writes the staleness flag to DataStore before the call and clears it on success, and returns `Result.success()`, `Result.retry()` (up to 3 attempts on `IOException`), or `Result.failure()` as appropriate.

**AC-8:** Given `ForecastRefreshWorker` fails after 3 retries when the staleness flag check runs then the DataStore staleness flag remains set, and the widget will display the staleness indicator.

## Tasks / Subtasks

- [ ] Task 1: Create `PreferenceKeys.kt` with all DataStore key constants (AC: 1)
  - [ ] 1.1 Create file at `app/src/main/java/com/weatherapp/data/datastore/PreferenceKeys.kt`
  - [ ] 1.2 Define all keys listed in the architecture: `KEY_WIDGET_VERDICT`, `KEY_BRING_LIST`, `KEY_BEST_WINDOW`, `KEY_ALL_CLEAR`, `KEY_MOOD_LINE`, `KEY_LAST_UPDATE_EPOCH`, `KEY_STALENESS_FLAG`, `KEY_HAS_COMPLETED_ONBOARDING`, `KEY_IS_PREMIUM`, `KEY_LAST_BILLING_CHECK`, `KEY_TEMP_UNIT`
  - [ ] 1.3 Verify no inline string literals exist at any call site — all usages reference `PreferenceKeys.*`

- [ ] Task 2: Create `DataStoreExtensions.kt` with helper functions (AC: 1)
  - [ ] 2.1 Create file at `app/src/main/java/com/weatherapp/data/datastore/DataStoreExtensions.kt`
  - [ ] 2.2 Implement `suspend fun DataStore<Preferences>.readWidgetState(): WidgetDisplayState`
  - [ ] 2.3 Implement `suspend fun DataStore<Preferences>.writeWidgetState(state: WidgetDisplayState)`

- [ ] Task 3: Create `ForecastHour` Room entity (AC: 2)
  - [ ] 3.1 Create `app/src/main/java/com/weatherapp/data/db/entity/ForecastHour.kt`
  - [ ] 3.2 Annotate with `@Entity(tableName = "forecast_hour")`
  - [ ] 3.3 Define columns: `hour_epoch` (Long, PrimaryKey), `temperature_c` (Double), `precipitation_probability` (Double), `wind_speed_kmh` (Double), `weather_code` (Int) — all column names in `snake_case`

- [ ] Task 4: Create `ForecastDao` (AC: 3)
  - [ ] 4.1 Create `app/src/main/java/com/weatherapp/data/db/dao/ForecastDao.kt`
  - [ ] 4.2 Implement `@Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(hours: List<ForecastHour>)`
  - [ ] 4.3 Implement `@Query("SELECT * FROM forecast_hour WHERE hour_epoch BETWEEN :startEpoch AND :endEpoch ORDER BY hour_epoch ASC") fun queryByTimeWindow(startEpoch: Long, endEpoch: Long): Flow<List<ForecastHour>>`
  - [ ] 4.4 Implement `@Query("DELETE FROM forecast_hour WHERE hour_epoch < :beforeEpoch") suspend fun deleteExpired(beforeEpoch: Long)`

- [ ] Task 5: Create or update `AppDatabase` to include `ForecastHour` (AC: 2)
  - [ ] 5.1 Create/update `app/src/main/java/com/weatherapp/data/db/AppDatabase.kt`
  - [ ] 5.2 Annotate with `@Database(entities = [ForecastHour::class], version = 1, exportSchema = false)`
  - [ ] 5.3 Declare abstract `fun forecastDao(): ForecastDao`
  - [ ] 5.4 Make it a singleton via `companion object` with `@Volatile` instance and `synchronized` block

- [ ] Task 6: Create `CoordinateUtils.kt` with `snapToGrid` extension (AC: 4)
  - [ ] 6.1 Create `app/src/main/java/com/weatherapp/data/location/CoordinateUtils.kt`
  - [ ] 6.2 Implement `fun Double.snapToGrid(cellDegrees: Double = 0.1): Double = (this / cellDegrees).roundToInt() * cellDegrees`
  - [ ] 6.3 Write unit tests in `CoordinateUtilsTest.kt` covering positive, negative, and boundary coordinates

- [ ] Task 7: Create `LocationRepository.kt` (AC: 4)
  - [ ] 7.1 Create `app/src/main/java/com/weatherapp/data/location/LocationRepository.kt`
  - [ ] 7.2 Inject `Context` via Hilt for permission checks
  - [ ] 7.3 Implement `suspend fun getSnappedLocation(): Pair<Double, Double>?` — checks `ACCESS_COARSE_LOCATION` via `ContextCompat.checkSelfPermission()`, retrieves last known location, snaps both lat and lon via `.snapToGrid()` before returning
  - [ ] 7.4 Return `null` (not throw) when permission is denied
  - [ ] 7.5 Never pass raw GPS coordinates outside of `LocationRepository`

- [ ] Task 8: Create network DTOs and `WeatherApi` Retrofit interface (AC: 5, 6)
  - [ ] 8.1 Create `app/src/main/java/com/weatherapp/data/weather/dto/ForecastResponse.kt` matching the Worker JSON schema: `lat_grid`, `lon_grid`, `fetched_at`, `hourly_forecasts: List<HourlyForecastDto>`
  - [ ] 8.2 Create `app/src/main/java/com/weatherapp/data/weather/dto/HourlyForecastDto.kt` with fields: `hour_epoch`, `temperature_c`, `precipitation_probability`, `wind_speed_kmh`, `weather_code`
  - [ ] 8.3 Create `app/src/main/java/com/weatherapp/data/weather/WeatherApi.kt` with `@GET("forecast") suspend fun getForecast(@Query("lat_grid") latGrid: Double, @Query("lon_grid") lonGrid: Double, @Query("date") date: String): ForecastResponse`

- [ ] Task 9: Create `WeatherRepository.kt` (AC: 5, 6)
  - [ ] 9.1 Create `app/src/main/java/com/weatherapp/data/weather/WeatherRepository.kt`
  - [ ] 9.2 Inject `WeatherApi`, `ForecastDao`, `LocationRepository` via Hilt constructor injection
  - [ ] 9.3 Implement `suspend fun fetchForecast(): Result<Unit>` using `runCatching { ... }`
  - [ ] 9.4 Inside `runCatching`: get snapped location from `LocationRepository`, build date string, call `api.getForecast(snappedLat, snappedLon, date)`, map DTOs to `ForecastHour` entities, call `forecastDao.insert(...)` and `forecastDao.deleteExpired(currentEpoch - 48h)`
  - [ ] 9.5 Return `Result.failure()` on `IOException` — never throw across the layer boundary
  - [ ] 9.6 Expose `fun getHourlyForecast(startEpoch: Long, endEpoch: Long): Flow<List<ForecastHour>>` delegating to `ForecastDao.queryByTimeWindow()`

- [ ] Task 10: Create `ForecastRefreshWorker.kt` (AC: 7, 8)
  - [ ] 10.1 Create `app/src/main/java/com/weatherapp/worker/ForecastRefreshWorker.kt`
  - [ ] 10.2 Annotate with `@HiltWorker` and extend `CoroutineWorker`
  - [ ] 10.3 Inject `WeatherRepository` and `DataStore<Preferences>` via `@AssistedInject`
  - [ ] 10.4 In `doWork()`: write staleness flag (`KEY_STALENESS_FLAG = true`) to DataStore BEFORE calling repository
  - [ ] 10.5 Call `weatherRepository.fetchForecast()` and on success: clear staleness flag, write `KEY_LAST_UPDATE_EPOCH` with current epoch
  - [ ] 10.6 On `IOException`: if `runAttemptCount < 3` return `Result.retry()`, else return `Result.failure()` (staleness flag stays set)
  - [ ] 10.7 On any other exception: `Timber.e(e, "Unrecoverable worker failure")` and return `Result.failure()`
  - [ ] 10.8 Register as `PeriodicWorkRequest` with 30-minute repeat interval in the Hilt `WorkerModule` or `AppModule`

- [ ] Task 11: Create Hilt modules for database and network (AC: 2, 3, 5)
  - [ ] 11.1 Create/update `app/src/main/java/com/weatherapp/di/DatabaseModule.kt` — provide `AppDatabase` singleton and `ForecastDao`
  - [ ] 11.2 Create/update `app/src/main/java/com/weatherapp/di/NetworkModule.kt` — provide `Retrofit` instance pointing to Cloudflare Worker base URL, `WeatherApi` via Retrofit, `OkHttpClient` with `HttpLoggingInterceptor`
  - [ ] 11.3 Create/update `app/src/main/java/com/weatherapp/di/AppModule.kt` — provide `DataStore<Preferences>` singleton
  - [ ] 11.4 Create/update `app/src/main/java/com/weatherapp/di/WorkerModule.kt` — bind `HiltWorkerFactory`

- [ ] Task 12: Write unit and instrumented tests (AC: 1–8)
  - [ ] 12.1 `CoordinateUtilsTest.kt`: test `snapToGrid()` with positive, negative, boundary (0.0), large values
  - [ ] 12.2 `WeatherRepositoryTest.kt`: mock `WeatherApi` success → verify `ForecastDao.insert` called; mock IOException → verify `Result.failure()` returned without throw
  - [ ] 12.3 `ForecastRefreshWorkerTest.kt` (WorkManager test): verify staleness flag is written before fetch; verify staleness flag is cleared on success; verify retry on IOException with `runAttemptCount < 3`; verify failure after 3 attempts
  - [ ] 12.4 `ForecastDaoTest.kt` (instrumented): verify `insert`, `queryByTimeWindow` returns correct rows, `deleteExpired` removes old rows

## Dev Notes

### Critical Architecture Rules for This Story

This story establishes the data pipeline foundation. Every subsequent story depends on the patterns established here. Do NOT deviate from the following:

**1. Coordinate Snapping — Mandatory at Network Boundary**

`CoordinateUtils.kt` must implement:
```kotlin
// app/src/main/java/com/weatherapp/data/location/CoordinateUtils.kt
package com.weatherapp.data.location

import kotlin.math.roundToInt

fun Double.snapToGrid(cellDegrees: Double = 0.1): Double =
    (this / cellDegrees).roundToInt() * cellDegrees
```

`LocationRepository` must snap BEFORE returning coordinates:
```kotlin
// Raw GPS from FusedLocationProviderClient
val rawLat = location.latitude
val rawLon = location.longitude
// Snap on-device; raw values never leave this function
return Pair(rawLat.snapToGrid(), rawLon.snapToGrid())
```

`WeatherRepository` receives only pre-snapped coordinates — it must NOT snap again. The contract is: `LocationRepository` owns snapping, `WeatherRepository` receives snapped values.

**2. Result<T> Error Boundary — Mandatory in Repository**

```kotlin
// WeatherRepository.kt
suspend fun fetchForecast(): Result<Unit> = runCatching {
    val (latGrid, lonGrid) = locationRepository.getSnappedLocation()
        ?: return Result.failure(IllegalStateException("Location unavailable"))
    val date = LocalDate.now(ZoneOffset.UTC).toString() // "YYYY-MM-DD"
    val response = api.getForecast(latGrid, lonGrid, date)
    val hours = response.hourlyForecasts.map { dto ->
        ForecastHour(
            hourEpoch = dto.hourEpoch,
            temperatureC = dto.temperatureC,
            precipitationProbability = dto.precipitationProbability,
            windSpeedKmh = dto.windSpeedKmh,
            weatherCode = dto.weatherCode
        )
    }
    forecastDao.insert(hours)
    // Clean up stale rows older than 48 hours
    val cutoff = System.currentTimeMillis() / 1000L - 48 * 3600L
    forecastDao.deleteExpired(cutoff)
}
```

**3. Staleness Flag Pattern in ForecastRefreshWorker**

```kotlin
// ForecastRefreshWorker.kt — doWork() structure
override suspend fun doWork(): Result {
    // Write staleness flag BEFORE attempting refresh
    dataStore.edit { prefs ->
        prefs[PreferenceKeys.KEY_STALENESS_FLAG] = true
    }
    return try {
        val result = weatherRepository.fetchForecast()
        if (result.isSuccess) {
            dataStore.edit { prefs ->
                prefs[PreferenceKeys.KEY_STALENESS_FLAG] = false
                prefs[PreferenceKeys.KEY_LAST_UPDATE_EPOCH] = System.currentTimeMillis() / 1000L
            }
            Result.success()
        } else {
            Timber.w("fetchForecast failed: ${result.exceptionOrNull()?.message}")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    } catch (e: IOException) {
        Timber.w(e, "Network error in ForecastRefreshWorker, attempt $runAttemptCount")
        if (runAttemptCount < 3) Result.retry() else Result.failure()
    } catch (e: Exception) {
        Timber.e(e, "Unrecoverable worker failure")
        Result.failure()
    }
}
```

**4. PreferenceKeys.kt — Exact Key Definitions**

```kotlin
// app/src/main/java/com/weatherapp/data/datastore/PreferenceKeys.kt
package com.weatherapp.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    val KEY_WIDGET_VERDICT = stringPreferencesKey("widget_verdict")
    val KEY_BRING_LIST = stringPreferencesKey("bring_list")
    val KEY_BEST_WINDOW = stringPreferencesKey("best_window")
    val KEY_ALL_CLEAR = booleanPreferencesKey("all_clear")
    val KEY_MOOD_LINE = stringPreferencesKey("mood_line")
    val KEY_LAST_UPDATE_EPOCH = longPreferencesKey("last_update_epoch")
    val KEY_STALENESS_FLAG = booleanPreferencesKey("staleness_flag")
    val KEY_HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
    val KEY_IS_PREMIUM = booleanPreferencesKey("is_premium")
    val KEY_LAST_BILLING_CHECK = longPreferencesKey("last_billing_check")
    val KEY_TEMP_UNIT = stringPreferencesKey("temp_unit")
}
```

**5. ForecastHour Entity — Exact Column Names**

```kotlin
// app/src/main/java/com/weatherapp/data/db/entity/ForecastHour.kt
package com.weatherapp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forecast_hour")
data class ForecastHour(
    @PrimaryKey
    @ColumnInfo(name = "hour_epoch")
    val hourEpoch: Long,

    @ColumnInfo(name = "temperature_c")
    val temperatureC: Double,

    @ColumnInfo(name = "precipitation_probability")
    val precipitationProbability: Double,

    @ColumnInfo(name = "wind_speed_kmh")
    val windSpeedKmh: Double,

    @ColumnInfo(name = "weather_code")
    val weatherCode: Int
)
```

**6. ForecastDao — Exact Interface**

```kotlin
// app/src/main/java/com/weatherapp/data/db/dao/ForecastDao.kt
package com.weatherapp.data.db.dao

import androidx.room.*
import com.weatherapp.data.db.entity.ForecastHour
import kotlinx.coroutines.flow.Flow

@Dao
interface ForecastDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(hours: List<ForecastHour>)

    @Query("SELECT * FROM forecast_hour WHERE hour_epoch BETWEEN :startEpoch AND :endEpoch ORDER BY hour_epoch ASC")
    fun queryByTimeWindow(startEpoch: Long, endEpoch: Long): Flow<List<ForecastHour>>

    @Query("DELETE FROM forecast_hour WHERE hour_epoch < :beforeEpoch")
    suspend fun deleteExpired(beforeEpoch: Long)
}
```

**7. AppDatabase — Initial Version**

```kotlin
// app/src/main/java/com/weatherapp/data/db/AppDatabase.kt
package com.weatherapp.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.weatherapp.data.db.dao.ForecastDao
import com.weatherapp.data.db.entity.ForecastHour

@Database(
    entities = [ForecastHour::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun forecastDao(): ForecastDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "weather_app_db"
                ).build().also { INSTANCE = it }
            }
    }
}
```

Note: `AlertStateRecord` and `CalendarEventForecast` will be added to this database in Stories 2.1 and 3.2 respectively, with version migrations.

**8. WeatherApi — Retrofit Interface**

```kotlin
// app/src/main/java/com/weatherapp/data/weather/WeatherApi.kt
package com.weatherapp.data.weather

import com.weatherapp.data.weather.dto.ForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("forecast")
    suspend fun getForecast(
        @Query("lat_grid") latGrid: Double,
        @Query("lon_grid") lonGrid: Double,
        @Query("date") date: String
    ): ForecastResponse
}
```

**9. DTO Classes — Exact Field Mapping**

```kotlin
// app/src/main/java/com/weatherapp/data/weather/dto/ForecastResponse.kt
package com.weatherapp.data.weather.dto

import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    @SerializedName("lat_grid") val latGrid: Double,
    @SerializedName("lon_grid") val lonGrid: Double,
    @SerializedName("fetched_at") val fetchedAt: String,
    @SerializedName("hourly_forecasts") val hourlyForecasts: List<HourlyForecastDto>
)

// app/src/main/java/com/weatherapp/data/weather/dto/HourlyForecastDto.kt
data class HourlyForecastDto(
    @SerializedName("hour_epoch") val hourEpoch: Long,
    @SerializedName("temperature_c") val temperatureC: Double,
    @SerializedName("precipitation_probability") val precipitationProbability: Double,
    @SerializedName("wind_speed_kmh") val windSpeedKmh: Double,
    @SerializedName("weather_code") val weatherCode: Int
)
```

**10. Hilt Module Patterns**

```kotlin
// app/src/main/java/com/weatherapp/di/DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    fun provideForecastDao(db: AppDatabase): ForecastDao = db.forecastDao()
}

// app/src/main/java/com/weatherapp/di/AppModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("weather_prefs") }
}

// app/src/main/java/com/weatherapp/di/WorkerModule.kt
@Module
@InstallIn(SingletonComponent::class)
interface WorkerModule {
    @Binds
    fun bindWorkerFactory(factory: HiltWorkerFactory): WorkerFactory
}
```

**11. ForecastRefreshWorker Registration**

The worker should be registered in `MainActivity` or `Application.onCreate()` via:
```kotlin
WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "forecast_refresh",
    ExistingPeriodicWorkPolicy.KEEP,
    PeriodicWorkRequestBuilder<ForecastRefreshWorker>(30, TimeUnit.MINUTES)
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .build()
)
```

But this is only called AFTER onboarding completes (Story 1.6 will trigger this). For this story, just ensure the Worker is properly defined and testable.

### Anti-Patterns to Avoid

- NEVER: `Log.d("WeatherRepo", "fetching")` → ALWAYS: `Timber.d("fetching")`
- NEVER: `api.getForecast(rawLat, rawLon)` → ALWAYS use snapped coords from `LocationRepository`
- NEVER: `stringPreferencesKey("widget_verdict")` inline at call site → ALWAYS: `PreferenceKeys.KEY_WIDGET_VERDICT`
- NEVER: throw exceptions from `WeatherRepository` → ALWAYS return `Result<T>`
- NEVER: store `ForecastHour` objects directly in DataStore → DataStore stores display-ready strings only
- NEVER: use `REPLACE` strategy on PrimaryKey in Room without understanding it will overwrite existing data with same epoch

### Project Structure Notes

Files to create in this story:
```
app/src/main/java/com/weatherapp/
  data/
    datastore/
      PreferenceKeys.kt              ← NEW: all DataStore key constants
      DataStoreExtensions.kt         ← NEW: helper read/write functions
    db/
      AppDatabase.kt                 ← NEW: Room database, version 1
      entity/
        ForecastHour.kt              ← NEW: forecast_hour table entity
      dao/
        ForecastDao.kt               ← NEW: insert/query/delete
    weather/
      WeatherRepository.kt           ← NEW: fetchForecast() + getHourlyForecast()
      WeatherApi.kt                  ← NEW: Retrofit interface
      dto/
        ForecastResponse.kt          ← NEW: JSON response mapping
        HourlyForecastDto.kt         ← NEW: hourly row mapping
    location/
      LocationRepository.kt          ← NEW: coarse location + snapping
      CoordinateUtils.kt             ← NEW: snapToGrid() extension
  di/
    AppModule.kt                     ← NEW: DataStore provider
    DatabaseModule.kt                ← NEW: Room + DAO providers
    NetworkModule.kt                 ← NEW: Retrofit + OkHttp + WeatherApi
    WorkerModule.kt                  ← NEW: HiltWorkerFactory binding
  worker/
    ForecastRefreshWorker.kt         ← NEW: 30-min periodic worker

app/src/test/java/com/weatherapp/
  data/
    location/CoordinateUtilsTest.kt  ← NEW
    weather/WeatherRepositoryTest.kt ← NEW
  worker/
    ForecastRefreshWorkerTest.kt     ← NEW

app/src/androidTest/java/com/weatherapp/
  db/
    ForecastDaoTest.kt               ← NEW
```

### Cross-Story Dependencies

- **Depends on Story 1.1**: Project foundation, Gradle dependencies (Room, DataStore, WorkManager, Retrofit, Hilt, Timber), `WeatherApp.kt` Application class with Timber planted, `util/UiState.kt` already present
- **Depends on Story 1.2**: Cloudflare Worker deployed and accessible; base URL must be configured in `NetworkModule.kt`
- **Provides to Story 1.4**: `ForecastDao.queryByTimeWindow()` for verdict generation; `DataStore<Preferences>` with `KEY_STALENESS_FLAG` and `KEY_LAST_UPDATE_EPOCH` patterns; `ForecastRefreshWorker` for chaining verdict generation logic into the same worker
- **Provides to Story 1.5**: `PreferenceKeys.*` constants for widget DataStore reads
- **Provides to Story 2.1**: `AppDatabase` for adding `AlertStateRecord` entity (version migration required)

### References

- [Source: architecture.md#Data Architecture] — DataStore vs Room split rationale
- [Source: architecture.md#Process Patterns] — Error handling with `runCatching`, WorkManager retry pattern
- [Source: architecture.md#Process Patterns] — Coordinate snapping mandatory pattern
- [Source: architecture.md#Naming Patterns] — `snake_case` Room tables/columns, `SCREAMING_SNAKE_CASE` DataStore keys
- [Source: architecture.md#Communication Patterns] — Workers write display-ready strings, key write order
- [Source: architecture.md#Enforcement Guidelines] — All 10 mandatory rules apply

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

### Completion Notes List

**[Code Review Pre-Implementation Warning — M-6]**

Task 5.4 specifies a `companion object` singleton for `AppDatabase`. This pattern conflicts with Hilt DI. When Task 11.1 creates `DatabaseModule.kt` providing `AppDatabase` as a `@Singleton`, two singleton paths will exist. The dev agent MUST:

1. Remove the `companion object { getInstance() }` pattern from `AppDatabase` — do NOT implement Task 5.4 as written
2. Make `AppDatabase` a plain abstract class (no companion object, no manual singleton)
3. Provide it exclusively via `@Provides @Singleton` in `DatabaseModule.kt`
4. Never call `AppDatabase.getInstance(context)` anywhere — always inject via Hilt

If both patterns are implemented, Hilt will manage one instance while `getInstance()` may create a second, causing database corruption or missed migrations.

### File List
