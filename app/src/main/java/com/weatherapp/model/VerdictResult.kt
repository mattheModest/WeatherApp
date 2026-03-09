package com.weatherapp.model

data class VerdictResult(
    val verdictText: String,
    val bringList: List<String>,
    val bestWindow: String?,
    val isAllClear: Boolean,
    val moodLine: String
)
