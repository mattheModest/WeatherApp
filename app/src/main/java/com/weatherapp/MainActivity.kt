package com.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint

// Navigation and content wired in Story 1.6 (Onboarding) and Story 1.7 (Hourly Detail).
// EXTRA_OPEN_HOURLY intent extra routing added in Story 1.5 (Home Screen Widget).
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_OPEN_HOURLY = "open_hourly"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // NavHost and AdaptiveSkyTheme wired in Story 1.6
        }
    }
}
