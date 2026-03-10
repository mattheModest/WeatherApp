package com.weatherapp.model

import com.weatherapp.data.db.entity.ForecastHour
import java.time.Instant
import java.time.ZoneId
import kotlin.math.abs

class VerdictGenerator {

    fun generateVerdict(hourlyData: List<ForecastHour>, comfortOffset: Double = 0.0): VerdictResult {
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
            generateClothingVerdict(hourlyData, comfortOffset)
        }
        val moodLine = generateMoodLine(hourlyData, isAllClear, comfortOffset)
        return VerdictResult(verdictText, bringList, bestWindow, isAllClear, moodLine)
    }

    private fun generateClothingVerdict(hourlyData: List<ForecastHour>, comfortOffset: Double): String {
        val peakTempC = hourlyData.maxOf { it.temperatureC }
        // Thresholds shift down for cold-acclimated users (negative offset) and
        // up for heat-acclimated users (positive offset).
        return when {
            peakTempC >= 28 + comfortOffset -> "No jacket needed"
            peakTempC >= 20 + comfortOffset -> "Light layers today"
            peakTempC >= 12 + comfortOffset -> "Light jacket weather"
            peakTempC >= 5  + comfortOffset -> "Jacket weather"
            else                            -> "Bundle up today"
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

    fun generateMoodLine(hourlyData: List<ForecastHour>, isAllClear: Boolean, comfortOffset: Double = 0.0): String {
        val hasStorm = hourlyData.any { it.weatherCode in 95..99 }
        val hasHeavyRain = hourlyData.any { it.precipitationProbability >= 0.70 }
        val hasRain = hourlyData.any { it.precipitationProbability >= 0.40 }
        val hasWind = hourlyData.any { it.windSpeedKmh >= 50 }
        val peakTemp = hourlyData.maxOf { it.temperatureC }
        // "Lovely enough to eat outside" threshold shifts with local comfort baseline
        val lovelyThreshold = 20.0 + comfortOffset
        return when {
            hasStorm     -> "Stay in if you can. This one means it."
            hasHeavyRain -> "Proper rain today. Definitely bring that umbrella."
            hasRain      -> "A good day to stay cosy. Or embrace the drizzle."
            hasWind      -> "Breezy today. Tie your hat down."
            isAllClear && peakTemp >= lovelyThreshold -> "Honestly lovely today. Eat lunch outside."
            isAllClear   -> "A really good day. Don't forget to step out."
            else         -> "Grey but manageable. You've got this."
        }
    }

    companion object {
        const val UMBRELLA_THRESHOLD = 0.40
        const val ALL_CLEAR_MESSAGE = "You're good. Go live your day."

        /**
         * Returns a temperature offset (°C) based on the user's latitude and current month.
         *
         * Positive = heat-acclimated (raise thresholds — 18°C feels cold to a Californian).
         * Negative = cold-acclimated (lower thresholds — 18°C feels fine to a Swede in July).
         *
         * Examples:
         *   Stockholm (59°N) July  → -4.0  so "Light layers" kicks in at 16°C not 20°C
         *   Los Angeles (34°N) July → +2.0  so "Light jacket" kicks in at 14°C not 12°C
         *   Bangkok (13°N) any month → +6.0  so 18°C would feel genuinely cold
         */
        fun computeComfortOffset(latGrid: Double, monthOfYear: Int): Double {
            val absLat = abs(latGrid)
            val isNorthernHemisphere = latGrid >= 0
            val isLocalSummer = if (isNorthernHemisphere) {
                monthOfYear in 5..8
            } else {
                monthOfYear == 12 || monthOfYear in 1..3
            }
            return when {
                absLat < 20 -> 6.0  // Tropical: Bangkok, Lagos, Singapore
                absLat < 30 -> if (isLocalSummer) 4.0 else 2.0  // Subtropical: Miami, Cairo
                absLat < 40 -> if (isLocalSummer) 2.0 else 0.0  // Warm temperate: LA, Rome, Tokyo
                absLat < 50 -> if (isLocalSummer) 0.0 else -2.0 // Temperate: London, Berlin, Paris
                absLat < 65 -> if (isLocalSummer) -4.0 else -5.0 // Nordic: Stockholm, Helsinki, Edinburgh
                else        -> if (isLocalSummer) -5.0 else -7.0  // Subarctic: northern Norway, Alaska
            }
        }

        /**
         * Standalone hourly clothing verdict for a single forecast hour.
         * Used by HourlyDetailViewModel to annotate each hour row.
         */
        fun hourlyClothingVerdict(tempC: Double, precipProb: Double, comfortOffset: Double = 0.0): String {
            val clothingBand = when {
                tempC >= 28 + comfortOffset -> "No jacket needed"
                tempC >= 20 + comfortOffset -> "Light layers"
                tempC >= 12 + comfortOffset -> "Light jacket"
                tempC >= 5  + comfortOffset -> "Jacket"
                else                        -> "Bundle up"
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
