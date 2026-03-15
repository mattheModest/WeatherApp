package com.weatherapp.ui.settings

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weatherapp.model.PersonalityCore
import com.weatherapp.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToPremium: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is UiState.Error -> {
                    Text(
                        text = state.message,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp)
                    )
                }

                is UiState.Success -> {
                    SettingsContent(
                        state = state.data,
                        onTempUnitToggled = { viewModel.onTempUnitToggled() },
                        onNotificationsToggled = { viewModel.onNotificationsToggled() },
                        onPersonalitySelected = { viewModel.onPersonalitySelected(it) },
                        onNavigateToPremium = onNavigateToPremium,
                        onUpgradeTapped = { viewModel.onUpgradeTapped(context as Activity) },
                        onShareMoodCard = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, state.data.shareText)
                                putExtra(Intent.EXTRA_TITLE, "Today's weather mood")
                            }
                            context.startActivity(
                                Intent.createChooser(shareIntent, "Share Today's Mood")
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsContent(
    state: SettingsState,
    onTempUnitToggled: () -> Unit,
    onNotificationsToggled: () -> Unit,
    onPersonalitySelected: (PersonalityCore) -> Unit,
    onNavigateToPremium: () -> Unit,
    onUpgradeTapped: () -> Unit,
    onShareMoodCard: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Temperature unit toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (state.tempUnit == TempUnit.CELSIUS) "Temperature: °C" else "Temperature: °F",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = state.tempUnit == TempUnit.FAHRENHEIT,
                onCheckedChange = { onTempUnitToggled() }
            )
        }

        // Notifications toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Notifications",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = state.notificationsEnabled,
                onCheckedChange = { onNotificationsToggled() }
            )
        }

        // Personality selector
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Personality",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        PersonalityCore.entries.forEach { p ->
            PersonalityCard(
                personality = p,
                isSelected = state.personality == p,
                onClick = { onPersonalitySelected(p) }
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Premium card (calm, informational — not a locked door)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (state.isPremium) {
                    Text(
                        text = "WeatherApp Premium",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Thank you for supporting WeatherApp.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "WeatherApp Premium",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Event-specific forecasts, change-triggered alerts, and more.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onNavigateToPremium,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Learn more — \$7.99/year", fontSize = 14.sp)
                    }
                    TextButton(
                        onClick = onUpgradeTapped,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("Upgrade to Premium", fontSize = 14.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Share Today's Mood
        TextButton(
            onClick = onShareMoodCard,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(
                text = "Share Today's Mood",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun PersonalityCard(
    personality: PersonalityCore,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    val contentColor = if (isSelected)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    val border = if (isSelected)
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    else
        null

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = personality.displayName,
                style = MaterialTheme.typography.titleSmall,
                color = contentColor
            )
            Text(
                text = personality.tagline,
                fontSize = 13.sp,
                color = contentColor.copy(alpha = 0.75f)
            )
        }
    }
}
