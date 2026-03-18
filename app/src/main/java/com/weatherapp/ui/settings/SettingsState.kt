package com.weatherapp.ui.settings

import com.weatherapp.model.PersonalityCore
import com.weatherapp.model.VisualTheme

enum class TempUnit {
    CELSIUS,
    FAHRENHEIT
}

data class SettingsState(
    val tempUnit: TempUnit,
    val notificationsEnabled: Boolean,
    val isPremium: Boolean,
    val moodLine: String,
    val shareText: String,
    val personality: PersonalityCore = PersonalityCore.FRANK,
    val visualTheme: VisualTheme = VisualTheme.DEFAULT,
    val manualLocation: String = ""
)
