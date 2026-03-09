package com.weatherapp.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weatherapp.R
import com.weatherapp.ui.hourly.HourlyDetailBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    showHourlySheet: Boolean,
    onOpenHourly: () -> Unit,
    onCloseHourly: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Open settings"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Your weather is on your home screen.",
                fontSize = 16.sp
            )
            OutlinedButton(
                onClick = onOpenHourly,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .heightIn(min = 48.dp)
            ) {
                Text(text = "See Today's Forecast", fontSize = 16.sp)
            }
        }

        if (showHourlySheet) {
            HourlyDetailBottomSheet(onDismiss = onCloseHourly)
        }
    }
}
