package com.weatherapp.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.weatherapp.R
import com.weatherapp.data.calendar.CalendarEvent
import com.weatherapp.data.calendar.CalendarRepository
import com.weatherapp.data.datastore.PreferenceKeys
import com.weatherapp.data.db.dao.AlertStateDao
import com.weatherapp.data.db.dao.CalendarEventForecastDao
import com.weatherapp.data.db.dao.ForecastDao
import com.weatherapp.data.db.entity.AlertStateRecord
import com.weatherapp.data.db.entity.ForecastHour
import com.weatherapp.data.location.LocationRepository
import com.weatherapp.model.AlertState
import com.weatherapp.model.AlertThresholds
import com.weatherapp.model.ForecastSnapshot
import com.weatherapp.util.NotificationChannels
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@HiltWorker
class AlertEvaluationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val alertStateDao: AlertStateDao,
    private val forecastDao: ForecastDao,
    private val dataStore: DataStore<Preferences>,
    private val locationRepository: LocationRepository,
    private val calendarRepository: CalendarRepository,
    private val calendarEventForecastDao: CalendarEventForecastDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val NOTIFICATION_ID_CONFIRMATION = 1001
        const val NOTIFICATION_ID_CHANGE       = 1002

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

        val HIGH_STAKES_KEYWORDS: Map<String, Int> = mapOf(
            "marathon"   to 6,
            "wedding"    to 12,
            "match"      to 4,
            "race"       to 6,
            "graduation" to 8,
            "concert"    to 4,
            "festival"   to 4
        )
    }

    private val gson = Gson()

    override suspend fun doWork(): Result {
        Timber.d("AlertEvaluationWorker started")
        return try {
            val notificationsEnabled =
                dataStore.data.first()[PreferenceKeys.KEY_NOTIFICATIONS_ENABLED] ?: true
            if (!notificationsEnabled) {
                Timber.d("Notifications disabled by user — skipping alert evaluation")
                return Result.success()
            }

            // Read isPremium at start of doWork()
            val isPremium = dataStore.data.first()[PreferenceKeys.KEY_IS_PREMIUM] ?: false

            NotificationChannels.ensureWeatherAlertsChannel(applicationContext)

            val nowEpoch          = System.currentTimeMillis() / 1000L
            val startOfTodayEpoch = nowEpoch - (nowEpoch % 86400L)
            val endOfTodayEpoch   = startOfTodayEpoch + 86400L
            val startOfYesterdayEpoch = startOfTodayEpoch - 86400L

            alertStateDao.resolveExpired(startOfYesterdayEpoch, nowEpoch)

            val location = locationRepository.getSnappedLocation()
            if (location == null) {
                Timber.w("AlertEvaluationWorker: location unavailable — skipping")
                return Result.success()
            }
            val (latGrid, lonGrid) = location
            val todayDate = LocalDate.now(ZoneOffset.UTC).toString()
            val todayKey  = "location:$latGrid:$lonGrid:$todayDate"

            val existing   = alertStateDao.getByEventId(todayKey)
            val todayHours = forecastDao.queryByTimeWindow(startOfTodayEpoch, endOfTodayEpoch).first()

            // Free-tier evaluation
            evaluateAndTransition(todayKey, existing, todayHours, nowEpoch)

            // Premium per-event evaluation
            if (isPremium) {
                val allEvents = calendarRepository.getUpcomingEvents(daysAhead = 7)
                val outdoorEvents = allEvents.filter { isOutdoorPotential(it) }
                Timber.d("AlertEvaluationWorker (premium): evaluating ${outdoorEvents.size} outdoor events")
                for (event in outdoorEvents) {
                    evaluatePremiumEventAlert(event, nowEpoch, todayHours)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "AlertEvaluationWorker failed")
            Result.failure()
        }
    }

    private suspend fun evaluateAndTransition(
        todayKey: String,
        existing: AlertStateRecord?,
        hours: List<ForecastHour>,
        nowEpoch: Long
    ) {
        val currentState = existing?.state ?: AlertState.UNCHECKED
        when (currentState) {
            AlertState.UNCHECKED -> {
                if (isAllClear(hours)) {
                    val snapshot = buildForecastSnapshot(hours, nowEpoch)
                    alertStateDao.insertRecord(
                        AlertStateRecord(
                            eventId = todayKey,
                            state   = AlertState.CONFIRMED_CLEAR,
                            confirmedForecastSnapshot = gson.toJson(snapshot),
                            lastTransitionAt = nowEpoch
                        )
                    )
                    Timber.d("AlertEvaluationWorker: UNCHECKED → CONFIRMED_CLEAR for $todayKey")
                    sendNotification(buildConfirmationNotification(), NOTIFICATION_ID_CONFIRMATION)
                }
                // else: not all-clear → stay UNCHECKED, no notification (AC-5)
            }

            AlertState.CONFIRMED_CLEAR -> {
                // At-most-once: confirmation already sent today (AC-3)
                val snapshot = runCatching {
                    gson.fromJson(existing!!.confirmedForecastSnapshot, ForecastSnapshot::class.java)
                }.getOrNull()
                if (snapshot != null && isMaterialChange(snapshot, hours)) {
                    alertStateDao.insertRecord(
                        AlertStateRecord(
                            eventId = todayKey,
                            state   = AlertState.ALERT_SENT,
                            confirmedForecastSnapshot = existing!!.confirmedForecastSnapshot,
                            lastTransitionAt = nowEpoch
                        )
                    )
                    Timber.d("AlertEvaluationWorker: CONFIRMED_CLEAR → ALERT_SENT for $todayKey")
                    sendNotification(buildChangeNotification(snapshot, hours), NOTIFICATION_ID_CHANGE)
                }
            }

            AlertState.ALERT_SENT, AlertState.RESOLVED -> {
                Timber.d("AlertEvaluationWorker: state=$currentState — no action for $todayKey")
            }
        }
    }

    private suspend fun evaluatePremiumEventAlert(
        event: CalendarEvent,
        nowEpoch: Long,
        hours: List<ForecastHour>
    ) {
        val existing = alertStateDao.getByEventId(event.eventId)
        val currentState = existing?.state ?: AlertState.UNCHECKED
        val freshSnapshot = buildForecastSnapshotForEvent(hours, event.startEpoch, nowEpoch)
        val freshSnapshotJson = gson.toJson(freshSnapshot)

        when (currentState) {
            AlertState.UNCHECKED -> {
                if (isAllClear(hours)) {
                    // Premium skips confirmation notification — just record state
                    alertStateDao.insertRecord(
                        AlertStateRecord(
                            eventId = event.eventId,
                            state   = AlertState.CONFIRMED_CLEAR,
                            confirmedForecastSnapshot = freshSnapshotJson,
                            lastTransitionAt = nowEpoch
                        )
                    )
                    Timber.d("AlertEvaluationWorker (premium): UNCHECKED → CONFIRMED_CLEAR for ${event.eventId} — no notification")
                }
            }

            AlertState.CONFIRMED_CLEAR -> {
                val snapshot = runCatching {
                    gson.fromJson(existing!!.confirmedForecastSnapshot, ForecastSnapshot::class.java)
                }.getOrNull()
                if (snapshot != null && isMaterialChange(snapshot, hours) && shouldSendAlert(event, nowEpoch)) {
                    alertStateDao.insertRecord(
                        AlertStateRecord(
                            eventId = event.eventId,
                            state   = AlertState.ALERT_SENT,
                            confirmedForecastSnapshot = existing!!.confirmedForecastSnapshot,
                            lastTransitionAt = nowEpoch
                        )
                    )
                    Timber.d("AlertEvaluationWorker (premium): CONFIRMED_CLEAR → ALERT_SENT for ${event.eventId}")
                    val notificationId = event.eventId.hashCode().let { if (it < 0) (-it) + 2000 else it + 2000 }
                    sendNotification(
                        buildPremiumChangeNotification(event, snapshot, hours),
                        notificationId
                    )
                }
            }

            AlertState.ALERT_SENT -> {
                // If conditions improved, reset to CONFIRMED_CLEAR to allow future re-alert
                if (isAllClear(hours)) {
                    alertStateDao.insertRecord(
                        AlertStateRecord(
                            eventId = event.eventId,
                            state   = AlertState.CONFIRMED_CLEAR,
                            confirmedForecastSnapshot = freshSnapshotJson,
                            lastTransitionAt = nowEpoch
                        )
                    )
                    Timber.d("AlertEvaluationWorker (premium): ALERT_SENT → CONFIRMED_CLEAR for ${event.eventId} (conditions improved)")
                }
            }

            AlertState.RESOLVED -> {
                Timber.d("AlertEvaluationWorker (premium): RESOLVED — skipping ${event.eventId}")
            }
        }
    }

    internal fun isOutdoorPotential(event: CalendarEvent): Boolean {
        val durationMinutes = (event.endEpoch - event.startEpoch) / 60L
        if (durationMinutes < 30L) return false
        val titleLower = event.title.lowercase(Locale.getDefault())
        return OUTDOOR_KEYWORDS.any { keyword -> titleLower.contains(keyword) }
    }

    internal fun getAlertLeadTimeHours(event: CalendarEvent): Int {
        val titleLower = event.title.lowercase(Locale.getDefault())
        return HIGH_STAKES_KEYWORDS.entries
            .firstOrNull { (keyword, _) -> titleLower.contains(keyword) }
            ?.value ?: 2
    }

    internal fun shouldSendAlert(event: CalendarEvent, nowEpoch: Long): Boolean {
        val hoursUntilEvent = (event.startEpoch - nowEpoch) / 3600.0
        val leadHours = getAlertLeadTimeHours(event)
        return hoursUntilEvent <= leadHours && hoursUntilEvent >= 2.0
    }

    private fun buildPremiumChangeNotification(
        event: CalendarEvent,
        snapshot: ForecastSnapshot,
        currentHours: List<ForecastHour>
    ): android.app.Notification {
        val timeStr = formatHour(event.startEpoch)
        val changeDescription = buildChangeDescription(snapshot, currentHours)
        val contentText = "Your ${event.title} ($timeStr) — $changeDescription. Conditions changed since last check."
        return NotificationCompat.Builder(applicationContext, NotificationChannels.CHANNEL_ID_WEATHER_ALERTS)
            .setSmallIcon(R.drawable.ic_weather_notification)
            .setContentTitle("Forecast change: ${event.title}")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
    }

    private fun buildChangeDescription(snapshot: ForecastSnapshot, currentHours: List<ForecastHour>): String {
        val currentMaxPrecip = currentHours.maxOfOrNull { it.precipitationProbability } ?: 0.0
        val currentMaxWind   = currentHours.maxOfOrNull { it.windSpeedKmh } ?: 0.0
        val precipIncreased  = currentMaxPrecip - snapshot.precipProb >= AlertThresholds.MATERIAL_PRECIP_CHANGE
        val windCrossed      = currentMaxWind >= AlertThresholds.WIND_SPEED_ALERT_KMH
        return when {
            precipIncreased -> "Rain moving in"
            windCrossed     -> "Wind picking up significantly"
            else            -> "Forecast changed"
        }
    }

    private fun formatHour(epochSeconds: Long): String {
        val date = Date(epochSeconds * 1000L)
        val cal = Calendar.getInstance()
        cal.time = date
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        return when {
            hour == 0  -> "12am"
            hour < 12  -> "${hour}am"
            hour == 12 -> "12pm"
            else       -> "${hour - 12}pm"
        }
    }

    private fun isAllClear(hours: List<ForecastHour>): Boolean {
        if (hours.isEmpty()) return false
        val daylightHours = hours.filter { hour ->
            Instant.ofEpochSecond(hour.hourEpoch).atZone(ZoneId.systemDefault()).hour in 7..20
        }
        if (daylightHours.isEmpty()) return false
        return daylightHours.all { hour ->
            hour.precipitationProbability < AlertThresholds.ALL_CLEAR_PRECIP_MAX &&
                hour.windSpeedKmh < AlertThresholds.ALL_CLEAR_WIND_MAX_KMH
        }
    }

    private fun isMaterialChange(
        snapshot: ForecastSnapshot,
        currentHours: List<ForecastHour>
    ): Boolean {
        if (currentHours.isEmpty()) return false
        val currentMaxPrecip = currentHours.maxOf { it.precipitationProbability }
        val currentMaxWind   = currentHours.maxOf { it.windSpeedKmh }
        val precipShift  = abs(currentMaxPrecip - snapshot.precipProb)
        val windCrossed  = currentMaxWind >= AlertThresholds.WIND_SPEED_ALERT_KMH &&
            snapshot.windKmh < AlertThresholds.WIND_SPEED_ALERT_KMH
        return precipShift >= AlertThresholds.MATERIAL_PRECIP_CHANGE || windCrossed
    }

    private fun buildForecastSnapshot(hours: List<ForecastHour>, nowEpoch: Long): ForecastSnapshot {
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

    private fun buildForecastSnapshotForEvent(
        hours: List<ForecastHour>,
        eventEpoch: Long,
        nowEpoch: Long
    ): ForecastSnapshot {
        // Use the hour closest to the event start for the snapshot
        val eventHour = hours.minByOrNull { abs(it.hourEpoch - eventEpoch) }
        val precipProb = eventHour?.precipitationProbability ?: 0.0
        val windKmh    = eventHour?.windSpeedKmh ?: 0.0
        val windowStart = hours.minOfOrNull { it.hourEpoch } ?: nowEpoch
        val windowEnd   = hours.maxOfOrNull { it.hourEpoch } ?: nowEpoch
        return ForecastSnapshot(
            precipProb  = precipProb,
            windKmh     = windKmh,
            windowStart = windowStart,
            windowEnd   = windowEnd
        )
    }

    private fun buildConfirmationNotification() =
        NotificationCompat.Builder(applicationContext, NotificationChannels.CHANNEL_ID_WEATHER_ALERTS)
            .setSmallIcon(R.drawable.ic_weather_notification)
            .setContentTitle("You're clear today")
            .setContentText("Go live your day.")
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

    private fun buildChangeNotification(
        snapshot: ForecastSnapshot,
        currentHours: List<ForecastHour>
    ): android.app.Notification {
        val contentText = buildChangeNotificationText(snapshot, currentHours)
        return NotificationCompat.Builder(applicationContext, NotificationChannels.CHANNEL_ID_WEATHER_ALERTS)
            .setSmallIcon(R.drawable.ic_weather_notification)
            .setContentTitle("Conditions changed")
            .setContentText(contentText)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
    }

    private fun buildChangeNotificationText(
        snapshot: ForecastSnapshot,
        currentHours: List<ForecastHour>
    ): String {
        val currentMaxPrecip = currentHours.maxOf { it.precipitationProbability }
        val currentMaxWind   = currentHours.maxOf { it.windSpeedKmh }
        val precipIncreased  = currentMaxPrecip - snapshot.precipProb >= AlertThresholds.MATERIAL_PRECIP_CHANGE
        val windCrossed      = currentMaxWind >= AlertThresholds.WIND_SPEED_ALERT_KMH
        return when {
            precipIncreased -> "Rain moving in — conditions changed since this morning."
            windCrossed     -> "Wind picking up significantly — conditions changed."
            else            -> "Forecast changed since your morning check."
        }
    }

    private fun sendNotification(notification: android.app.Notification, id: Int) {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Timber.d("POST_NOTIFICATIONS not granted — notification suppressed (id=$id)")
            return
        }
        NotificationManagerCompat.from(applicationContext).notify(id, notification)
    }
}
