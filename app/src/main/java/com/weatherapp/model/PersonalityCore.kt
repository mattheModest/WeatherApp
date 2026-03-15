package com.weatherapp.model

enum class PersonalityCore(
    val displayName: String,
    val tagline: String
) {
    FRANK("Frank", "Direct and a little cheeky"),
    KELVIN("Kelvin", "Explains the science"),
    GRAVES("Graves", "Finds the dark side")
}

fun personalityCoreFromString(value: String?): PersonalityCore =
    PersonalityCore.entries.firstOrNull { it.name == value } ?: PersonalityCore.FRANK
