package com.weatherapp.model

enum class ClimateZone {
    /** |lat| < 20°  — Bangkok, Lagos, Bogotá */
    TROPICAL,
    /** |lat| 20–32° — Miami, Cairo, Mumbai */
    SUBTROPICAL,
    /** |lat| 32–45° — LA, Rome, Tokyo */
    TEMPERATE,
    /** |lat| 45–57° — London, Berlin, Vancouver */
    OCEANIC,
    /** |lat| ≥ 57°  — Stockholm, Helsinki, Oslo, southern Patagonia */
    NORDIC
}
