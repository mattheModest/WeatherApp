package com.weatherapp.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.weatherapp.model.VisualTheme
import com.weatherapp.model.WeatherState

/**
 * Full per-theme styling — colors, typography, shape, layout flags.
 * Passed into WeatherCard and ForecastCard to drive all visual variation.
 */
data class VisualThemeStyle(
    val tokens: WeatherColorTokens,
    // Typography
    val titleFontFamily: FontFamily = FontFamily.Default,
    val metaFontFamily: FontFamily = FontFamily.Default,
    val verdictWeight: FontWeight = FontWeight.SemiBold,
    // Shape
    val cardCornerRadius: Dp = 24.dp,
    val chipCornerRadius: Dp = 20.dp,
    // Chip border (Chalk: visible chalk border; Beige: light bevel approximation)
    val chipBorderColor: Color = Color.Transparent,
    val chipBorderWidth: Dp = 0.dp,
    // Card border
    val cardBorderColor: Color = Color.Transparent,
    val cardBorderWidth: Dp = 0.dp,
    // Layout variants
    val showTitlebar: Boolean = false,
    val titlebarBg: Color = Color.Transparent,
    val titlebarTextColor: Color = Color.White,
    val showPullQuoteBorder: Boolean = false,
    val pullQuoteBorderColor: Color = Color.Transparent,
    val showLiveBadge: Boolean = false,
    // Neo-Brutalism: hard CSS-style offset shadow
    val hasBrutalistShadow: Boolean = false,
    val brutalistShadowColor: Color = Color.Black,
    val brutalistShadowOffset: Dp = 4.dp,
    // 8-Bit: ASCII weather symbols + pixel indicator
    val useAsciiWeather: Boolean = false,
    val showPixelIndicator: Boolean = false,
)

// ── Fixed palettes ────────────────────────────────────────────────────────────

internal val beigeTokens = WeatherColorTokens(
    background        = Color(0xFFB8A880),
    cardBackground    = Color(0xFFC8B888),
    topZoneBackground = Color(0xFF5A4A28),   // dark amber titlebar
    verdictText       = Color(0xFF1E1408),
    secondaryText     = Color(0xFF3A2C10),
    accentColor       = Color(0xFF8A7848),
    chipBackground    = Color(0xFFBBA870),
    chipText          = Color(0xFF1E1408)
)

internal val inkTokens = WeatherColorTokens(
    background        = Color(0xFF0E0D0B),
    cardBackground    = Color(0x0AFBBF24),   // rgba(251,191,36,0.04)
    topZoneBackground = Color(0x0FFBBF24),   // rgba(251,191,36,0.06)
    verdictText       = Color(0xF7FCDC8C),   // rgba(252,220,140,0.97)
    secondaryText     = Color(0xB2DCC896),   // rgba(220,200,150,0.70)
    accentColor       = Color(0xFFFBBF24),
    chipBackground    = Color(0x19FBBF24),   // rgba(251,191,36,0.10)
    chipText          = Color(0xCCFBBF24)    // rgba(251,191,36,0.80)
)

internal val utilityTokens = WeatherColorTokens(
    background        = Color(0xFF070809),
    cardBackground    = Color(0xFF0D0F10),
    topZoneBackground = Color(0x084ADE80),   // rgba(74,222,128,0.03)
    verdictText       = Color(0xFFE8EAF6),
    secondaryText     = Color(0x804ADE80),   // rgba(74,222,128,0.50)
    accentColor       = Color(0xFF4ADE80),
    chipBackground    = Color(0x144ADE80),   // rgba(74,222,128,0.08)
    chipText          = Color(0xCC4ADE80)    // rgba(74,222,128,0.80)
)

internal val chalkTokens = WeatherColorTokens(
    background        = Color(0xFF1E2D38),
    cardBackground    = Color(0xFF293D4A),
    topZoneBackground = Color(0x09FFFFFF),   // rgba(255,255,255,0.035)
    verdictText       = Color(0xFFF5F2EE),
    secondaryText     = Color(0xB8E1DCCD),   // rgba(225,220,205,0.72)
    accentColor       = Color(0xBFEBE6D7),   // rgba(235,230,215,0.75)
    chipBackground    = Color.Transparent,
    chipText          = Color(0xE6E6E1D2)    // rgba(230,225,210,0.90)
)

// Neo-Brutalism: yellow/black high-contrast with black top zone
internal val neoBrutalismTokens = WeatherColorTokens(
    background        = Color(0xFFF5E642),  // screaming yellow
    cardBackground    = Color(0xFFF5E642),
    topZoneBackground = Color(0xFF0A0A0A),  // black header (inverted)
    verdictText       = Color(0xFFF5E642),  // yellow on black
    secondaryText     = Color(0xFF1A1A1A),
    accentColor       = Color(0xFF0A0A0A),
    chipBackground    = Color(0xFF0A0A0A),
    chipText          = Color(0xFFF5E642)
)

// 8-Bit: Game Boy 4-shade green palette
internal val eightBitTokens = WeatherColorTokens(
    background        = Color(0xFF0F380F),  // darkest
    cardBackground    = Color(0xFF306230),  // dark
    topZoneBackground = Color(0xFF0F380F),  // darkest header
    verdictText       = Color(0xFF9BBC0F),  // lightest
    secondaryText     = Color(0xFF8BAC0F),
    accentColor       = Color(0xFF9BBC0F),
    chipBackground    = Color(0xFF8BAC0F),
    chipText          = Color(0xFF0F380F)   // darkest on light chip
)

// ── Theme → Style factory ─────────────────────────────────────────────────────

fun VisualTheme.toStyle(weatherState: WeatherState, isDark: Boolean): VisualThemeStyle = when (this) {
    VisualTheme.DEFAULT -> VisualThemeStyle(
        tokens = WeatherDesignTokens.getTokens(weatherState, isDark)
    )
    VisualTheme.SUN_STAINED_BEIGE -> VisualThemeStyle(
        tokens          = beigeTokens,
        titleFontFamily = AppFonts.monospace,
        metaFontFamily  = AppFonts.monospace,
        verdictWeight   = FontWeight.Bold,
        cardCornerRadius = 4.dp,
        chipCornerRadius = 2.dp,
        chipBorderColor  = Color(0xFFE8DCC0),
        chipBorderWidth  = 1.dp,
        showTitlebar     = true,
        titlebarBg       = Color(0xFF5A4A28),
        titlebarTextColor = Color(0xFFE0C888),
    )
    VisualTheme.INK_EDITORIAL -> VisualThemeStyle(
        tokens               = inkTokens,
        verdictWeight        = FontWeight.Black,
        cardCornerRadius     = 14.dp,
        chipCornerRadius     = 3.dp,
        showPullQuoteBorder  = true,
        pullQuoteBorderColor = Color(0x80FBBF24),   // rgba(251,191,36,0.50)
        cardBorderColor      = Color(0x1FFBBF24),
        cardBorderWidth      = 1.dp,
    )
    VisualTheme.UTILITY_CHIC -> VisualThemeStyle(
        tokens          = utilityTokens,
        metaFontFamily  = AppFonts.monospace,
        cardCornerRadius = 14.dp,
        chipCornerRadius = 3.dp,
        showLiveBadge   = true,
        cardBorderColor  = Color(0x2E4ADE80),
        cardBorderWidth  = 1.dp,
    )
    VisualTheme.CHALK_SLATE -> VisualThemeStyle(
        tokens           = chalkTokens,
        titleFontFamily  = AppFonts.handwriting,
        metaFontFamily   = AppFonts.handwriting,
        verdictWeight    = FontWeight.Bold,
        cardCornerRadius = 4.dp,
        chipCornerRadius = 0.dp,
        chipBorderColor  = Color(0x8CE6E1D2),
        chipBorderWidth  = 2.dp,
        cardBorderColor  = Color(0x52F0EBDC),
        cardBorderWidth  = 2.dp,
    )
    VisualTheme.NEO_BRUTALISM -> VisualThemeStyle(
        tokens                = neoBrutalismTokens,
        verdictWeight         = FontWeight.Black,
        cardCornerRadius      = 0.dp,
        chipCornerRadius      = 0.dp,
        chipBorderColor       = Color(0xFFF5E642),  // yellow border on black chips
        chipBorderWidth       = 2.dp,
        cardBorderColor       = Color(0xFF0A0A0A),
        cardBorderWidth       = 3.dp,
        hasBrutalistShadow    = true,
        brutalistShadowColor  = Color(0xFF0A0A0A),
        brutalistShadowOffset = 4.dp,
    )
    VisualTheme.EIGHT_BIT -> VisualThemeStyle(
        tokens             = eightBitTokens,
        titleFontFamily    = AppFonts.monospace,
        metaFontFamily     = AppFonts.monospace,
        verdictWeight      = FontWeight.Bold,
        cardCornerRadius   = 0.dp,
        chipCornerRadius   = 0.dp,
        chipBorderColor    = Color(0xFF9BBC0F),
        chipBorderWidth    = 2.dp,
        cardBorderColor    = Color(0xFF9BBC0F),
        cardBorderWidth    = 2.dp,
        useAsciiWeather    = true,
        showPixelIndicator = true,
    )
}

/** Widget-specific: colors only (Glance has no custom font support). */
fun VisualTheme.toWidgetTokens(weatherState: WeatherState, isDark: Boolean): WeatherColorTokens =
    when (this) {
        VisualTheme.DEFAULT          -> WeatherDesignTokens.getTokens(weatherState, isDark)
        VisualTheme.SUN_STAINED_BEIGE -> beigeTokens
        VisualTheme.INK_EDITORIAL    -> inkTokens
        VisualTheme.UTILITY_CHIC     -> utilityTokens
        VisualTheme.CHALK_SLATE      -> chalkTokens
        VisualTheme.NEO_BRUTALISM    -> neoBrutalismTokens
        VisualTheme.EIGHT_BIT        -> eightBitTokens
    }
