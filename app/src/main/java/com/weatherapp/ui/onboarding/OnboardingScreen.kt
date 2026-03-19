package com.weatherapp.ui.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weatherapp.util.UiState

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onOnboardingComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val locationPermissionLauncher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        if (granted) viewModel.onLocationGranted() else viewModel.onLocationDenied()
    }

    val calendarPermissionLauncher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        if (granted) viewModel.onCalendarGranted() else viewModel.onCalendarDenied()
    }

    when (val state = uiState) {
        is UiState.Loading -> Unit
        is UiState.Error   -> Unit
        is UiState.Success -> {
            when (state.data.step) {
                OnboardingStep.LOCATION_PERMISSION -> {
                    AlertDialog(
                        onDismissRequest = { /* not dismissible */ },
                        title = {
                            Text(
                                text = "Where are you?",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        },
                        text = {
                            Text(
                                text = "WeatherApp needs to know your location to give you accurate weather. Your location stays on your device.",
                                fontSize = 14.sp
                            )
                        },
                        confirmButton = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                                    },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                ) {
                                    Text("Use my location")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.onCityChosen() },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                ) {
                                    Text("Enter my city instead")
                                }
                            }
                        },
                        dismissButton = null
                    )
                }

                OnboardingStep.MANUAL_LOCATION -> {
                    var cityText by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { /* not dismissible */ },
                        title = {
                            Text(
                                text = "Your city",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "Enter your city name and we'll fetch local weather for you.",
                                    fontSize = 14.sp
                                )
                                OutlinedTextField(
                                    value = cityText,
                                    onValueChange = { cityText = it },
                                    label = { Text("City name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.onManualLocationEntered(cityText) },
                                enabled = cityText.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            ) {
                                Text("Continue")
                            }
                        },
                        dismissButton = null
                    )
                }

                OnboardingStep.CALENDAR_RATIONALE -> {
                    AlertDialog(
                        onDismissRequest = { /* not dismissible */ },
                        title = {
                            Text(
                                text = "Add calendar?",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        },
                        text = {
                            Text(
                                text = "WeatherApp can check your calendar for outdoor plans — picnics, hikes, sports — and tell you the weather for those events specifically.",
                                fontSize = 14.sp
                            )
                        },
                        confirmButton = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.onCalendarRationaleAcknowledged() },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                ) {
                                    Text("Yes, check my calendar")
                                }
                                TextButton(
                                    onClick = { viewModel.onCalendarDenied() },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                ) {
                                    Text("Skip for now")
                                }
                            }
                        },
                        dismissButton = null
                    )
                }

                OnboardingStep.CALENDAR_PERMISSION -> {
                    LaunchedEffect(Unit) {
                        calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                    }
                }

                OnboardingStep.COMPLETE -> {
                    LaunchedEffect(Unit) {
                        onOnboardingComplete()
                    }
                }
            }
        }
    }
}
