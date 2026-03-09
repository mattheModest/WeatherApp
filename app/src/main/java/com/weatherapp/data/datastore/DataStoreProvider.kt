package com.weatherapp.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Single DataStore instance shared between Hilt DI (AppModule) and non-Hilt contexts (WeatherWidget)
val Context.weatherDataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_prefs")
