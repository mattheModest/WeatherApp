package com.weatherapp.model

enum class PersonalityCore(
    val displayName: String,
    val tagline: String,
    val sampleVerdict: String
) {
    FRANK(
        displayName   = "Frank",
        tagline       = "Direct and a little cheeky",
        sampleVerdict = "\"Light jacket weather.\""
    ),
    KELVIN(
        displayName   = "Kelvin",
        tagline       = "Explains the science",
        sampleVerdict = "\"Surface temp 13°C, light winds. Jacket territory. Thermodynamically speaking.\""
    ),
    GRAVES(
        displayName   = "Graves",
        tagline       = "Finds the dark side",
        sampleVerdict = "\"Light jacket weather. Not much to report, which is a sentence this forecast rarely gets to say.\""
    )
}

fun personalityCoreFromString(value: String?): PersonalityCore =
    PersonalityCore.entries.firstOrNull { it.name == value } ?: PersonalityCore.FRANK
