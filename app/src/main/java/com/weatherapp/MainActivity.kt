package com.weatherapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.weatherapp.ui.main.MainScreen
import com.weatherapp.ui.onboarding.OnboardingScreen
import com.weatherapp.ui.settings.SettingsScreen
import com.weatherapp.ui.theme.AdaptiveSkyTheme
import com.weatherapp.ui.widget.rotateWidgetCopy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WeatherAppContent(mainViewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.onResume()
        lifecycleScope.launch { rotateWidgetCopy(this@MainActivity) }
    }

}

@Composable
fun WeatherAppContent(mainViewModel: MainViewModel) {
    val startDestination by mainViewModel.startDestination.collectAsStateWithLifecycle()
    val displayState by mainViewModel.displayState.collectAsStateWithLifecycle()
    val updateInfo by mainViewModel.updateInfo.collectAsStateWithLifecycle()
    val visualTheme by mainViewModel.visualTheme.collectAsStateWithLifecycle()
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
        weatherState = displayState.weatherState,
        darkTheme = isSystemInDarkTheme(),
        visualTheme = visualTheme
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
                    displayState = displayState,
                    updateInfo = updateInfo,
                    visualTheme = visualTheme,
                    onOpenSettings = { navController.navigate("settings") },
                    onDismissUpdate = { mainViewModel.dismissUpdate() }
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
