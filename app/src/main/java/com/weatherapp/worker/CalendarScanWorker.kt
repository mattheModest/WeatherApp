package com.weatherapp.worker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.weatherapp.data.calendar.CalendarEvent
import com.weatherapp.data.calendar.CalendarRepository
import com.weatherapp.data.datastore.PreferenceKeys
import com.weatherapp.data.db.dao.CalendarEventForecastDao
import com.weatherapp.data.db.dao.ForecastDao
import com.weatherapp.data.db.entity.CalendarEventForecast
import com.weatherapp.data.db.entity.ForecastHour
import com.weatherapp.model.ForecastSnapshot
import com.weatherapp.ui.widget.WeatherWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.Locale

@HiltWorker
class CalendarScanWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val calendarRepository: CalendarRepository,
    private val forecastDao: ForecastDao,
    private val calendarEventForecastDao: CalendarEventForecastDao,
    private val dataStore: DataStore<Preferences>,
    private val workManager: WorkManager
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        val OUTDOOR_KEYWORDS: Set<String> = setOf(
            "bbq", "barbecue", "picnic", "hike", "hiking", "run", "running", "jog", "jogging",
            "bike", "biking", "cycle", "cycling", "walk", "walking", "park", "garden", "gardening",
            "tennis", "golf", "soccer", "football", "baseball", "softball", "cricket", "rugby",
            "swim", "swimming", "beach", "pool", "kayak", "kayaking", "canoe", "canoeing",
            "camp", "camping", "outdoor", "outside", "alfresco", "festival", "concert",
            "marathon", "race", "match", "game", "tournament", "sports", "yoga", "workout",
            "graduation", "wedding", "parade", "fair", "carnival", "hunt", "fishing", "ski",
            "skiing", "snowboard", "surf", "surfing", "climbing", "trail"
        )
    }

    private val gson = Gson()

    override suspend fun doWork(): Result {
        Timber.d("CalendarScanWorker started")

        // Defense in depth — check premium status first
        val isPremium = dataStore.data.first()[PreferenceKeys.KEY_IS_PREMIUM] ?: false
        if (!isPremium) {
            Timber.d("CalendarScanWorker: not premium — exiting immediately")
            return Result.success()
        }

        return try {
            val nowEpoch = System.currentTimeMillis() / 1000L

            // 1. Delete expired CalendarEventForecast records (older than now)
            calendarEventForecastDao.deleteExpired(nowEpoch)

            // 2. Get upcoming events (7 days), filter to outdoor-potential with >= 30 min duration
            val allEvents = calendarRepository.getUpcomingEvents(daysAhead = 7)
            val outdoorEvents = allEvents.filter { isOutdoorPotential(it) }
            Timber.d("CalendarScanWorker: ${allEvents.size} events total, ${outdoorEvents.size} outdoor-potential")

            // 3. Focus on events starting within next 24h for widget display
            val next24hEpoch = nowEpoch + 86400L
            val upcomingOutdoor = outdoorEvents.filter { it.startEpoch in nowEpoch..next24hEpoch }

            // 4. Detect conflicts among outdoor events in next 24h
            val conflicts = detectConflicts(upcomingOutdoor)

            // 5. Build verdict string
            if (upcomingOutdoor.isNotEmpty()) {
                val nowSeconds   = System.currentTimeMillis() / 1000L
                val startOfDay   = nowSeconds - (nowSeconds % 86400L)
                val endOfDay     = startOfDay + 86400L
                val todayHours   = forecastDao.queryByTimeWindow(startOfDay, endOfDay).first()

                val verdictString = if (conflicts.isNotEmpty()) {
                    buildConflictVerdictString(conflicts, todayHours)
                } else {
                    buildEventVerdictString(upcomingOutdoor.first(), todayHours)
                }

                // 6. Write KEY_WIDGET_VERDICT to DataStore (only this key, preserve others)
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.KEY_WIDGET_VERDICT] = verdictString
                }
                Timber.d("CalendarScanWorker: widget verdict written — \"$verdictString\"")

                // TODO: v2 — pre-fetch location-specific forecasts for event locations
                // WeatherRepository.fetchForecast() does not yet support custom coordinates.
                // For now, we use the existing home location forecast only.

                // 7. Upsert CalendarEventForecast for each outdoor event
                val snapshot = buildSnapshot(todayHours, nowEpoch)
                val snapshotJson = gson.toJson(snapshot)
                outdoorEvents.forEach { event ->
                    val displayString = buildEventVerdictString(event, todayHours)
                    calendarEventForecastDao.upsert(
                        CalendarEventForecast(
                            eventId             = event.eventId,
                            lastWeatherSnapshot = snapshotJson,
                            widgetDisplayString = displayString,
                            lastUpdatedEpoch    = nowEpoch,
                            eventStartEpoch     = event.startEpoch
                        )
                    )
                }
            }

            // 8. Call WeatherWidget.update()
            WeatherWidget.update(applicationContext)

            // 9. Enqueue AlertEvaluationWorker
            val alertWork = OneTimeWorkRequestBuilder<AlertEvaluationWorker>().build()
            workManager.enqueue(alertWork)
            Timber.d("CalendarScanWorker: AlertEvaluationWorker enqueued")

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "CalendarScanWorker failed")
            Result.failure()
        }
    }

    internal fun isOutdoorPotential(event: CalendarEvent): Boolean {
        val durationMinutes = (event.endEpoch - event.startEpoch) / 60L
        if (durationMinutes < 30L) return false
        val titleLower = event.title.lowercase(Locale.getDefault())
        return OUTDOOR_KEYWORDS.any { keyword -> titleLower.contains(keyword) }
    }

    internal fun detectConflicts(events: List<CalendarEvent>): List<CalendarEvent> {
        if (events.size < 2) return emptyList()
        val conflicting = mutableSetOf<CalendarEvent>()
        for (i in events.indices) {
            for (j in i + 1 until events.size) {
                val a = events[i]
                val b = events[j]
                // Overlap: a starts before b ends AND b starts before a ends
                if (a.startEpoch < b.endEpoch && b.startEpoch < a.endEpoch) {
                    conflicting.add(a)
                    conflicting.add(b)
                }
            }
        }
        return conflicting.toList()
    }

    private fun buildEventVerdictString(event: CalendarEvent, hours: List<ForecastHour>): String {
        val timeStr = formatHour(event.startEpoch)
        val weatherDesc = classifyWeather(hours, event.startEpoch)
        return if (weatherDesc == "is clear") {
            "Your ${event.title} ($timeStr) is clear."
        } else {
            "Your ${event.title} ($timeStr) $weatherDesc."
        }
    }

    internal fun buildConflictVerdictString(events: List<CalendarEvent>, hours: List<ForecastHour>): String {
        val count = events.size
        val earliest = events.minByOrNull { it.startEpoch } ?: return ""
        val timeStr = formatHour(earliest.startEpoch)
        val weatherDesc = classifyWeather(hours, earliest.startEpoch)
        val weatherPart = if (weatherDesc == "is clear") "Clear" else weatherDesc
        return "$count outdoor events at $timeStr · $weatherPart · Check both."
    }

    private fun classifyWeather(hours: List<ForecastHour>, eventEpoch: Long): String {
        // Find the hour closest to the event start
        val eventHour = hours.minByOrNull { kotlin.math.abs(it.hourEpoch - eventEpoch) }
            ?: hours.firstOrNull()
        val precipProb = eventHour?.precipitationProbability ?: 0.0
        val windKmh    = eventHour?.windSpeedKmh ?: 0.0
        return when {
            precipProb >= 0.40 -> "Rain expected"
            windKmh >= 40.0    -> "Windy"
            else               -> "is clear"
        }
    }

    internal fun formatHour(epochSeconds: Long): String {
        val javaDate = java.util.Date(epochSeconds * 1000L)
        val cal = java.util.Calendar.getInstance()
        cal.time = javaDate
        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            hour == 0  -> "12am"
            hour < 12  -> "${hour}am"
            hour == 12 -> "12pm"
            else       -> "${hour - 12}pm"
        }
    }

    private fun buildSnapshot(hours: List<ForecastHour>, nowEpoch: Long): ForecastSnapshot {
        val maxPrecip   = hours.maxOfOrNull { it.precipitationProbability } ?: 0.0
        val maxWind     = hours.maxOfOrNull { it.windSpeedKmh } ?: 0.0
        val windowStart = hours.minOfOrNull { it.hourEpoch } ?: nowEpoch
        val windowEnd   = hours.maxOfOrNull { it.hourEpoch } ?: nowEpoch
        return ForecastSnapshot(
            precipProb  = maxPrecip,
            windKmh     = maxWind,
            windowStart = windowStart,
            windowEnd   = windowEnd
        )
    }
}
