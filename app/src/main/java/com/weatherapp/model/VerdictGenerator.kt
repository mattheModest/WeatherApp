package com.weatherapp.model

import com.weatherapp.data.db.entity.ForecastHour
import java.time.Instant
import java.time.ZoneId

class VerdictGenerator {

    fun generateVerdict(hourlyData: List<ForecastHour>): VerdictResult {
        if (hourlyData.isEmpty()) {
            return VerdictResult(
                verdictText = "Weather data loading...",
                bringList = emptyList(),
                bestWindow = null,
                isAllClear = false,
                moodLine = "Check back in a moment."
            )
        }
        val bringList = evaluateBringList(hourlyData)
        val bestWindow = calculateBestWindow(hourlyData)
        val isAllClear = bringList.isEmpty() && bestWindow != null
        val verdictText = if (isAllClear) {
            ALL_CLEAR_MESSAGE
        } else {
            generateClothingVerdict(hourlyData)
        }
        val moodLine = generateMoodLine(hourlyData, isAllClear)
        return VerdictResult(verdictText, bringList, bestWindow, isAllClear, moodLine)
    }

    private fun generateClothingVerdict(hourlyData: List<ForecastHour>): String {
        val peakTempC = hourlyData.maxOf { it.temperatureC }
        return when {
            peakTempC >= 28 -> "No jacket needed"
            peakTempC >= 20 -> "Light layers today"
            peakTempC >= 12 -> "Light jacket weather"
            peakTempC >= 5  -> "Jacket weather"
            else            -> "Bundle up today"
        }
    }

    fun evaluateBringList(hourlyData: List<ForecastHour>): List<String> {
        val items = mutableListOf<String>()
        val hasRain = hourlyData.any { it.precipitationProbability >= UMBRELLA_THRESHOLD }
        if (hasRain) items.add("Bring an umbrella")
        val hasSunnyAfternoon = hourlyData.any { hour ->
            val localHour = Instant.ofEpochSecond(hour.hourEpoch)
                .atZone(ZoneId.systemDefault()).hour
            localHour in 11..17 &&
                hour.precipitationProbability < 0.10 &&
                hour.weatherCode in 0..2
        }
        if (hasSunnyAfternoon) items.add("Sunscreen today")
        return items
    }

    fun calculateBestWindow(hourlyData: List<ForecastHour>): String? {
        val daylightHours = hourlyData.filter { hour ->
            val localHour = Instant.ofEpochSecond(hour.hourEpoch)
                .atZone(ZoneId.systemDefault()).hour
            localHour in 7..20
        }.sortedBy { it.hourEpoch }

        var bestStart: Long? = null
        var bestEnd: Long? = null
        var currentStart: Long? = null
        var consecutiveCount = 0

        for (hour in daylightHours) {
            val isClear = hour.precipitationProbability < 0.20 &&
                hour.windSpeedKmh < 30 &&
                hour.weatherCode in 0..3
            if (isClear) {
                if (currentStart == null) currentStart = hour.hourEpoch
                consecutiveCount++
                if (consecutiveCount >= 2 && bestStart == null) {
                    bestStart = currentStart
                    bestEnd = hour.hourEpoch + 3600L
                } else if (consecutiveCount >= 2) {
                    bestEnd = hour.hourEpoch + 3600L
                }
            } else {
                currentStart = null
                consecutiveCount = 0
            }
        }

        return if (bestStart != null && bestEnd != null) {
            val startLabel = formatHour(bestStart)
            val endLabel = formatHour(bestEnd)
            "Best time outside: $startLabel–$endLabel"
        } else null
    }

    private fun formatHour(epochSeconds: Long): String {
        val hour = Instant.ofEpochSecond(epochSeconds)
            .atZone(ZoneId.systemDefault()).hour
        return when {
            hour == 0  -> "12am"
            hour < 12  -> "${hour}am"
            hour == 12 -> "12pm"
            else       -> "${hour - 12}pm"
        }
    }

    fun generateMoodLine(hourlyData: List<ForecastHour>, isAllClear: Boolean): String {
        val hasStorm = hourlyData.any { it.weatherCode in 95..99 }
        val hasHeavyRain = hourlyData.any { it.precipitationProbability >= 0.70 }
        val hasRain = hourlyData.any { it.precipitationProbability >= 0.40 }
        val hasWind = hourlyData.any { it.windSpeedKmh >= 50 }
        val peakTemp = hourlyData.maxOf { it.temperatureC }
        return when {
            hasStorm     -> "Stay in if you can. This one means it."
            hasHeavyRain -> "Proper rain today. Definitely bring that umbrella."
            hasRain      -> "A good day to stay cosy. Or embrace the drizzle."
            hasWind      -> "Breezy today. Tie your hat down."
            isAllClear && peakTemp >= 20 -> "Honestly lovely today. Eat lunch outside."
            isAllClear   -> "A really good day. Don't forget to step out."
            else         -> "Grey but manageable. You've got this."
        }
    }

    companion object {
        const val UMBRELLA_THRESHOLD = 0.40
        const val ALL_CLEAR_MESSAGE = "You're good. Go live your day."

        /**
         * Standalone hourly clothing verdict for a single forecast hour.
         * Used by HourlyDetailViewModel to annotate each hour row.
         */
        fun hourlyClothingVerdict(tempC: Double, precipProb: Double): String {
            val clothingBand = when {
                tempC >= 28 -> "No jacket needed"
                tempC >= 20 -> "Light layers"
                tempC >= 12 -> "Light jacket"
                tempC >= 5  -> "Jacket"
                else        -> "Bundle up"
            }
            val rainQualifier = when {
                precipProb >= 0.70 -> " — heavy rain likely"
                precipProb >= 0.40 -> " — rain likely"
                precipProb >= 0.20 -> " — chance of showers"
                else               -> ""
            }
            return clothingBand + rainQualifier
        }
    }
}
