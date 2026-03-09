package com.weatherapp.ui.onboarding

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weatherapp.ui.widget.WeatherWidgetReceiver
import com.weatherapp.util.UiState
import timber.log.Timber

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onOnboardingComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        if (granted) {
            viewModel.onLocationGranted()
        } else {
            viewModel.onLocationDenied()
        }
    }

    val calendarPermissionLauncher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        if (granted) {
            viewModel.onCalendarGranted()
        } else {
            viewModel.onCalendarDenied()
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator()
                }

                is UiState.Error -> {
                    Text(
                        text = state.message,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                is UiState.Success -> {
                    OnboardingStepContent(
                        state = state.data,
                        onLocationPermissionRequest = {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                        },
                        onCalendarRationaleAcknowledged = { viewModel.onCalendarRationaleAcknowledged() },
                        onCalendarPermissionLaunch = {
                            calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                        },
                        onManualLocationEntered = { location -> viewModel.onManualLocationEntered(location) },
                        onOnboardingComplete = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val appWidgetManager = AppWidgetManager.getInstance(context)
                                val provider = ComponentName(context, WeatherWidgetReceiver::class.java)
                                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                                    appWidgetManager.requestPinAppWidget(provider, null, null)
                                    Timber.d("OnboardingScreen: widget pin requested")
                                }
                            }
                            onOnboardingComplete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingStepContent(
    state: OnboardingState,
    onLocationPermissionRequest: () -> Unit,
    onCalendarRationaleAcknowledged: () -> Unit,
    onCalendarPermissionLaunch: () -> Unit,
    onManualLocationEntered: (String) -> Unit,
    onOnboardingComplete: () -> Unit
) {
    when (state.step) {
        OnboardingStep.LOCATION_PERMISSION -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Allow location access to get weather for your area.",
                    fontSize = 16.sp
                )
                Button(
                    onClick = onLocationPermissionRequest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text(text = "Grant Location Access", fontSize = 16.sp)
                }
            }
        }

        OnboardingStep.CALENDAR_RATIONALE -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "We use your calendar to tell you when weather matters to your plans.",
                    fontSize = 16.sp
                )
                Button(
                    onClick = onCalendarRationaleAcknowledged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text(text = "Continue", fontSize = 16.sp)
                }
            }
        }

        OnboardingStep.CALENDAR_PERMISSION -> {
            LaunchedEffect(Unit) {
                onCalendarPermissionLaunch()
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        OnboardingStep.MANUAL_LOCATION -> {
            var cityText by remember { mutableStateOf("") }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Enter your city to get local weather.",
                    fontSize = 16.sp
                )
                OutlinedTextField(
                    value = cityText,
                    onValueChange = { cityText = it },
                    label = { Text("City name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = { onManualLocationEntered(cityText) },
                    enabled = cityText.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text(text = "Continue", fontSize = 16.sp)
                }
            }
        }

        OnboardingStep.COMPLETE -> {
            LaunchedEffect(Unit) {
                onOnboardingComplete()
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
