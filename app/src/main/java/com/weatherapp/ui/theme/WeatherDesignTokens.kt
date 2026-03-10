package com.weatherapp.ui.theme

import androidx.compose.ui.graphics.Color
import com.weatherapp.model.WeatherState

data class WeatherColorTokens(
    val background: Color,
    val cardBackground: Color,
    val verdictText: Color,
    val secondaryText: Color,
    val accentColor: Color,
    val chipBackground: Color,
    val chipText: Color
)

object WeatherDesignTokens {

    // CLEAR
    val clearLight = WeatherColorTokens(
        background    = Color(0xFFF0F7FF),
        cardBackground = Color(0xFFFFFFFF),
        verdictText   = Color(0xFF1A2B4A),
        secondaryText = Color(0xFF4A6A8A),
        accentColor   = Color(0xFF4A7AE0),
        chipBackground = Color(0xFFBBDEFB),
        chipText      = Color(0xFF1565C0)
    )
    val clearDark = WeatherColorTokens(
        background    = Color(0xFF0C1A2E),
        cardBackground = Color(0xFF142440),
        verdictText   = Color(0xFFE3F2FD),
        secondaryText = Color(0xFF90CAF9),
        accentColor   = Color(0xFF6C8FFF),
        chipBackground = Color(0xFF1C3557),
        chipText      = Color(0xFFBBDEFB)
    )

    // OVERCAST — uses landing page palette exactly
    val overcastLight = WeatherColorTokens(
        background    = Color(0xFFF2F3F8),
        cardBackground = Color(0xFFFFFFFF),
        verdictText   = Color(0xFF2A2B3E),
        secondaryText = Color(0xFF5C5C72),
        accentColor   = Color(0xFF7B68EE),
        chipBackground = Color(0xFFD8D8E8),
        chipText      = Color(0xFF3A3A50)
    )
    val overcastDark = WeatherColorTokens(
        background    = Color(0xFF0F1117),   // landing page --bg
        cardBackground = Color(0xFF1A1D27),  // landing page --surface
        verdictText   = Color(0xFFE8EAF6),   // landing page --text
        secondaryText = Color(0xFF8891B2),   // landing page --muted
        accentColor   = Color(0xFFA78BFA),   // landing page --accent2
        chipBackground = Color(0xFF22263A),  // landing page --surface2
        chipText      = Color(0xFFCCCCE0)
    )

    // RAIN
    val rainLight = WeatherColorTokens(
        background    = Color(0xFFEAECF0),
        cardBackground = Color(0xFFF5F6F8),
        verdictText   = Color(0xFF25303C),
        secondaryText = Color(0xFF546E7A),
        accentColor   = Color(0xFF607D8B),
        chipBackground = Color(0xFFCFD8DC),
        chipText      = Color(0xFF37474F)
    )
    val rainDark = WeatherColorTokens(
        background    = Color(0xFF0F1520),
        cardBackground = Color(0xFF162030),
        verdictText   = Color(0xFFECEFF1),
        secondaryText = Color(0xFF90A4AE),
        accentColor   = Color(0xFF78909C),
        chipBackground = Color(0xFF1E2D3A),
        chipText      = Color(0xFFB0BEC5)
    )

    // STORM
    val stormLight = WeatherColorTokens(
        background    = Color(0xFFEEEEEE),
        cardBackground = Color(0xFFF8F8F8),
        verdictText   = Color(0xFF1C1C1C),
        secondaryText = Color(0xFF555555),
        accentColor   = Color(0xFF455A64),
        chipBackground = Color(0xFFBDBDBD),
        chipText      = Color(0xFF212121)
    )
    val stormDark = WeatherColorTokens(
        background    = Color(0xFF0D0F12),
        cardBackground = Color(0xFF171A1E),
        verdictText   = Color(0xFFEEEEEE),
        secondaryText = Color(0xFF9E9E9E),
        accentColor   = Color(0xFF78909C),
        chipBackground = Color(0xFF1C1F24),
        chipText      = Color(0xFFBDBDBD)
    )

    fun getTokens(state: WeatherState, isDark: Boolean): WeatherColorTokens = when (state) {
        WeatherState.CLEAR    -> if (isDark) clearDark    else clearLight
        WeatherState.OVERCAST -> if (isDark) overcastDark else overcastLight
        WeatherState.RAIN     -> if (isDark) rainDark     else rainLight
        WeatherState.STORM    -> if (isDark) stormDark    else stormLight
    }
}
