package com.weatherapp

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.weatherapp.model.WeatherState
import com.weatherapp.ui.main.MainScreen
import com.weatherapp.ui.onboarding.OnboardingScreen
import com.weatherapp.ui.settings.SettingsScreen
import com.weatherapp.ui.theme.AdaptiveSkyTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import androidx.activity.compose.rememberLauncherForActivityResult

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    companion object {
        const val EXTRA_OPEN_HOURLY = "open_hourly"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (intent.getBooleanExtra(EXTRA_OPEN_HOURLY, false)) {
            mainViewModel.openHourlyDetail()
        }

        setContent {
            WeatherAppContent(mainViewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        mainViewModel.onNewIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.onResume()
    }
}

@Composable
fun WeatherAppContent(mainViewModel: MainViewModel) {
    val startDestination by mainViewModel.startDestination.collectAsStateWithLifecycle()
    val showHourly by mainViewModel.showHourlySheet.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        Timber.d("WeatherAppContent: notification permission result: granted=$granted")
    }

    LaunchedEffect(Unit) {
        mainViewModel.requestNotificationPermission.collect {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val startDest = startDestination ?: return

    AdaptiveSkyTheme(
        weatherState = WeatherState.CLEAR,
        darkTheme = isSystemInDarkTheme()
    ) {
        NavHost(
            navController = navController,
            startDestination = startDest
        ) {
            composable("onboarding") {
                OnboardingScreen(
                    onOnboardingComplete = {
                        navController.navigate("main") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }
            composable("main") {
                MainScreen(
                    showHourlySheet = showHourly,
                    onOpenHourly = { mainViewModel.openHourlyDetail() },
                    onCloseHourly = { mainViewModel.closeHourlyDetail() },
                    onOpenSettings = { navController.navigate("settings") }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
