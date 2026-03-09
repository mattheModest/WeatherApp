package com.weatherapp.ui.theme

import androidx.compose.ui.graphics.Color
import com.weatherapp.model.WeatherState

data class WeatherColorTokens(
    val background: Color,
    val verdictText: Color,
    val secondaryText: Color,
    val accentColor: Color,
    val chipBackground: Color,
    val chipText: Color
)

object WeatherDesignTokens {

    // CLEAR
    val clearLight = WeatherColorTokens(
        background    = Color(0xFFE8F4FD),
        verdictText   = Color(0xFF1A3A5C),
        secondaryText = Color(0xFF4A6A8A),
        accentColor   = Color(0xFF2196F3),
        chipBackground = Color(0xFFBBDEFB),
        chipText      = Color(0xFF1565C0)
    )
    val clearDark = WeatherColorTokens(
        background    = Color(0xFF0D2137),
        verdictText   = Color(0xFFE3F2FD),
        secondaryText = Color(0xFF90CAF9),
        accentColor   = Color(0xFF64B5F6),
        chipBackground = Color(0xFF1565C0),
        chipText      = Color(0xFFE3F2FD)
    )

    // OVERCAST
    val overcastLight = WeatherColorTokens(
        background    = Color(0xFFF0F0F4),
        verdictText   = Color(0xFF2C2C3E),
        secondaryText = Color(0xFF5C5C72),
        accentColor   = Color(0xFF7B68EE),
        chipBackground = Color(0xFFD8D8E8),
        chipText      = Color(0xFF3A3A50)
    )
    val overcastDark = WeatherColorTokens(
        background    = Color(0xFF1A1A2E),
        verdictText   = Color(0xFFE8E8F4),
        secondaryText = Color(0xFFAAAAAC),
        accentColor   = Color(0xFF9D8FFF),
        chipBackground = Color(0xFF2E2E45),
        chipText      = Color(0xFFCCCCE0)
    )

    // RAIN
    val rainLight = WeatherColorTokens(
        background    = Color(0xFFECEFF1),
        verdictText   = Color(0xFF263238),
        secondaryText = Color(0xFF546E7A),
        accentColor   = Color(0xFF607D8B),
        chipBackground = Color(0xFFCFD8DC),
        chipText      = Color(0xFF37474F)
    )
    val rainDark = WeatherColorTokens(
        background    = Color(0xFF162027),
        verdictText   = Color(0xFFECEFF1),
        secondaryText = Color(0xFF90A4AE),
        accentColor   = Color(0xFF78909C),
        chipBackground = Color(0xFF263238),
        chipText      = Color(0xFFB0BEC5)
    )

    // STORM
    val stormLight = WeatherColorTokens(
        background    = Color(0xFFEEEEEE),
        verdictText   = Color(0xFF1C1C1C),
        secondaryText = Color(0xFF555555),
        accentColor   = Color(0xFF455A64),
        chipBackground = Color(0xFFBDBDBD),
        chipText      = Color(0xFF212121)
    )
    val stormDark = WeatherColorTokens(
        background    = Color(0xFF0F0F0F),
        verdictText   = Color(0xFFEEEEEE),
        secondaryText = Color(0xFF9E9E9E),
        accentColor   = Color(0xFF78909C),
        chipBackground = Color(0xFF1C1C1C),
        chipText      = Color(0xFFBDBDBD)
    )

    fun getTokens(state: WeatherState, isDark: Boolean): WeatherColorTokens = when (state) {
        WeatherState.CLEAR    -> if (isDark) clearDark    else clearLight
        WeatherState.OVERCAST -> if (isDark) overcastDark else overcastLight
        WeatherState.RAIN     -> if (isDark) rainDark     else rainLight
        WeatherState.STORM    -> if (isDark) stormDark    else stormLight
    }
}
