package com.weatherapp.worker

import android.Manifest
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.weatherapp.data.billing.BillingRepository
import com.weatherapp.data.datastore.PreferenceKeys
import com.weatherapp.data.db.dao.ForecastDao
import com.weatherapp.data.weather.WeatherRepository
import com.weatherapp.model.VerdictGenerator
import com.weatherapp.ui.widget.WeatherWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.io.IOException

@HiltWorker
class ForecastRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherRepository: WeatherRepository,
    private val forecastDao: ForecastDao,
    private val dataStore: DataStore<Preferences>,
    private val workManager: WorkManager,
    private val billingRepository: BillingRepository
) : CoroutineWorker(appContext, workerParams) {

    private val verdictGenerator = VerdictGenerator()

    override suspend fun doWork(): Result {
        // Read isPremium at the very start of doWork()
        val isPremium = dataStore.data.first()[PreferenceKeys.KEY_IS_PREMIUM] ?: false

        // 24h billing check gate
        val lastBillingCheck = dataStore.data.first()[PreferenceKeys.KEY_LAST_BILLING_CHECK] ?: 0L
        val nowEpoch = System.currentTimeMillis() / 1000L
        val billingCheckAgeSeconds = nowEpoch - lastBillingCheck
        if (billingCheckAgeSeconds > 86400L) {
            Timber.d("ForecastRefreshWorker: billing check age=${billingCheckAgeSeconds}s — running checkAndUpdatePremiumStatus")
            billingRepository.checkAndUpdatePremiumStatus()
                .onFailure { e -> Timber.w(e, "ForecastRefreshWorker: billing check failed") }
        }

        // Check READ_CALENDAR permission — if revoked, operate in weather-only mode silently
        val calendarPermission = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.READ_CALENDAR
        )
        if (calendarPermission != PermissionChecker.PERMISSION_GRANTED) {
            Timber.w("ForecastRefreshWorker: READ_CALENDAR permission not granted — weather-only mode")
        }

        // Write staleness flag BEFORE attempting refresh
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.KEY_STALENESS_FLAG] = true
        }

        return try {
            val fetchResult = weatherRepository.fetchForecast()
            if (fetchResult.isSuccess) {
                // Query today's hours for verdict generation
                val nowSeconds   = System.currentTimeMillis() / 1000L
                val startOfDay   = nowSeconds - (nowSeconds % 86400L)
                val endOfDay     = startOfDay + 86400L
                val todayHours   = forecastDao.queryByTimeWindow(startOfDay, endOfDay).first()

                val verdictResult = verdictGenerator.generateVerdict(todayHours)

                // Atomic write — all content keys first, KEY_LAST_UPDATE_EPOCH LAST
                dataStore.edit { prefs ->
                    prefs[PreferenceKeys.KEY_WIDGET_VERDICT]  = verdictResult.verdictText
                    prefs[PreferenceKeys.KEY_BRING_LIST]      = verdictResult.bringList.joinToString("|")
                    prefs[PreferenceKeys.KEY_BEST_WINDOW]     = verdictResult.bestWindow ?: ""
                    prefs[PreferenceKeys.KEY_ALL_CLEAR]       = verdictResult.isAllClear
                    prefs[PreferenceKeys.KEY_MOOD_LINE]       = verdictResult.moodLine
                    prefs[PreferenceKeys.KEY_STALENESS_FLAG]  = false
                    // LAST — widget reads this to detect fresh data
                    prefs[PreferenceKeys.KEY_LAST_UPDATE_EPOCH] = System.currentTimeMillis() / 1000L
                }

                Timber.d("ForecastRefreshWorker: verdict written — allClear=${verdictResult.isAllClear}")
                WeatherWidget.update(applicationContext)

                // Queue notification permission request after first successful widget render
                // Only trigger once: guard on KEY_NOTIFICATIONS_PERMISSION_REQUESTED
                val alreadyRequested = dataStore.data.first()[PreferenceKeys.KEY_NOTIFICATIONS_PERMISSION_REQUESTED]
                if (alreadyRequested != true) {
                    dataStore.edit { prefs ->
                        prefs[PreferenceKeys.KEY_SHOULD_REQUEST_NOTIFICATIONS] = true
                    }
                    Timber.d("ForecastRefreshWorker: notification permission prompt queued")
                }

                // Chain AlertEvaluationWorker to run immediately after
                val alertWork = OneTimeWorkRequestBuilder<AlertEvaluationWorker>().build()
                workManager.enqueue(alertWork)
                Timber.d("ForecastRefreshWorker: AlertEvaluationWorker enqueued")

                // If premium, also enqueue CalendarScanWorker
                if (isPremium) {
                    val calendarWork = OneTimeWorkRequestBuilder<CalendarScanWorker>().build()
                    workManager.enqueue(calendarWork)
                    Timber.d("ForecastRefreshWorker: CalendarScanWorker enqueued (premium)")
                }

                Result.success()
            } else {
                Timber.w("fetchForecast failed: ${fetchResult.exceptionOrNull()?.message}")
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
}
