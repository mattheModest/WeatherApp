package com.weatherapp.ui.theme

import androidx.compose.ui.text.font.FontFamily

object AppFonts {
    /** Serif — used by Chalk & Slate theme. Glance (widget) only supports system fonts;
     *  Cursive maps to illegible script fonts on many devices. Serif is the closest
     *  legible match that works identically in both Compose and Glance. */
    val handwriting: FontFamily = FontFamily.Serif

    /** Monospace — used by Sun-Stained Beige and Utility Chic themes. */
    val monospace: FontFamily = FontFamily.Monospace
}
