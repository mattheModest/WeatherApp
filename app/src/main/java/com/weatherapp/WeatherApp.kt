package com.weatherapp

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.weatherapp.ui.widget.rotateWidgetCopy
import com.weatherapp.util.NotificationChannels
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class WeatherApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    // Dynamic receiver for ACTION_USER_PRESENT (fires on lock screen dismiss).
    // Must be dynamic — static manifest receivers cannot receive this broadcast on Android 8+.
    private val screenUnlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_USER_PRESENT) {
                Timber.d("WeatherApp: screen unlocked, rotating widget copy")
                CoroutineScope(Dispatchers.IO).launch {
                    rotateWidgetCopy(context)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        NotificationChannels.ensureWeatherAlertsChannel(this)
        registerReceiver(screenUnlockReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
    }
}
