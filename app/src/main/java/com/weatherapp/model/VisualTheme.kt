package com.weatherapp.model

enum class VisualTheme(val displayName: String, val tagline: String) {
    DEFAULT("Default", "Adapts to the weather. Clean and polished."),
    SUN_STAINED_BEIGE("Sun-Stained Beige", "Warm retro. Like a PC by a sunny window."),
    INK_EDITORIAL("Ink & Editorial", "Heavy type. Printed weather almanac."),
    UTILITY_CHIC("Utility Chic", "Precision terminal. Data, not decoration."),
    CHALK_SLATE("Chalk & Slate", "Handwritten on a dark chalkboard."),
    NEO_BRUTALISM("Neo-Brutalism", "Bold. Yellow. Zero compromise."),
    EIGHT_BIT("8-Bit Handheld", "Game Boy weather. Pixel-perfect.")
}

fun visualThemeFromString(value: String?): VisualTheme =
    VisualTheme.entries.firstOrNull { it.name == value } ?: VisualTheme.DEFAULT
