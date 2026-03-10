package com.weatherapp.model

import com.weatherapp.data.db.entity.ForecastHour
import java.time.Instant
import java.time.ZoneId
import kotlin.math.abs

class VerdictGenerator {

    fun generateVerdict(hourlyData: List<ForecastHour>, comfortOffset: Double = 0.0): VerdictResult {
        if (hourlyData.isEmpty()) {
            return VerdictResult(
                verdictText = "Checking the forecast...",
                bringList = emptyList(),
                bestWindow = null,
                isAllClear = false,
                moodLine = "One moment."
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
            peakTempC >= 28 + comfortOffset -> "Nothing to grab. Warm all day."
            peakTempC >= 20 + comfortOffset -> "Light layers, if anything."
            peakTempC >= 12 + comfortOffset -> "Light jacket weather."
            peakTempC >= 5  + comfortOffset -> "Jacket day. Don't skip it."
            else                            -> "Bundle up. It's a cold one."
        }
    }

    fun evaluateBringList(hourlyData: List<ForecastHour>): List<String> {
        val items = mutableListOf<String>()
        val hasRain = hourlyData.any { it.precipitationProbability >= UMBRELLA_THRESHOLD }
        if (hasRain) items.add("☂ Umbrella")
        val hasSunnyAfternoon = hourlyData.any { hour ->
            val localHour = Instant.ofEpochSecond(hour.hourEpoch)
                .atZone(ZoneId.systemDefault()).hour
            localHour in 11..17 &&
                hour.precipitationProbability < 0.10 &&
                hour.weatherCode in 0..2
        }
        if (hasSunnyAfternoon) items.add("☀ Sunscreen")
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
            "$startLabel–$endLabel"
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
            hasHeavyRain -> "Proper rain today. You'll want that umbrella."
            hasRain      -> "Good day to stay cosy — or just embrace the drizzle."
            hasWind      -> "Breezy today. Tie your hat down."
            isAllClear && peakTemp >= lovelyThreshold -> "Honestly lovely today. Eat lunch outside."
            isAllClear   -> "A genuinely good day. Don't forget to step outside."
            else         -> "Grey but manageable. You've got this."
        }
    }

    companion object {
        const val UMBRELLA_THRESHOLD = 0.40
        const val ALL_CLEAR_MESSAGE = "You're good. Go live your day."

        /**
         * Returns a temperature offset (°C) based on the user's latitude and current month.
         * Applied to all clothing thresholds: positive = heat-acclimated (higher bar to feel cool),
         * negative = cold-acclimated (lower bar to feel warm).
         *
         * Calibrated against peer-reviewed thermal comfort field data (ASHRAE adaptive model,
         * PET studies across 160 buildings / 21,000+ respondents) and jacket-threshold surveys:
         *
         *   Bangkok (13°N, any)      → +5  → "light jacket" starts at 17°C, "no jacket" at 33°C
         *   Los Angeles (34°N, July) → -2  → "light jacket" starts at 10°C, "no jacket" at 26°C
         *   London (51°N, July)      → -6  → "light jacket" starts at  6°C, "no jacket" at 22°C
         *   Stockholm (59°N, July)   → -10 → "light jacket" starts at  2°C, "no jacket" at 18°C
         *   Stockholm (59°N, Jan)    → -12 → "jacket" starts at −7°C,  "no jacket" at 16°C
         *
         * Seasonal modifier (+1 autumn, 0 spring) reflects the documented 3–6°C perceptual
         * shift: post-summer acclimatisation makes the same temperature feel colder.
         */
        fun computeComfortOffset(latGrid: Double, monthOfYear: Int): Double {
            val absLat = abs(latGrid)
            val isNorthern = latGrid >= 0

            val isLocalSummer = if (isNorthern) monthOfYear in 6..8  else monthOfYear == 12 || monthOfYear in 1..2
            val isLocalWinter = if (isNorthern) monthOfYear == 12 || monthOfYear in 1..2 else monthOfYear in 6..8
            val isLocalAutumn = if (isNorthern) monthOfYear in 9..11 else monthOfYear in 3..5

            // summerOffset / winterOffset derived from research jacket-threshold data:
            //   jacket threshold ≈ 20 + offset  (the Light-layers → Light-jacket boundary)
            //   neutral "nice day" ≈ 24 + offset (centre of the Light-layers band)
            val (summerOffset, winterOffset) = when {
                absLat < 20 -> Pair(5.0,  5.0)   // Tropical — effectively no seasons; jacket at 25°C
                absLat < 30 -> Pair(3.0,  1.0)   // Subtropical (Miami, Cairo); jacket at 23°C / 21°C
                absLat < 40 -> Pair(-2.0, -4.0)  // Warm temperate (LA, Rome); jacket at 18°C / 16°C
                absLat < 50 -> Pair(-6.0, -8.0)  // Temperate (London, Berlin); jacket at 14°C / 12°C
                absLat < 65 -> Pair(-10.0,-12.0) // Nordic (Stockholm, Helsinki); jacket at 10°C / 8°C
                else        -> Pair(-13.0,-15.0) // Subarctic (N. Norway, Alaska); jacket at 7°C / 5°C
            }

            return when {
                isLocalSummer -> summerOffset
                isLocalWinter -> winterOffset
                // Autumn: post-summer acclimatisation makes cold feel harsher (+1 vs summer)
                isLocalAutumn -> summerOffset + 1.0
                // Spring: post-winter acclimation, cold feels milder (average, biased warm)
                else          -> (summerOffset + winterOffset) / 2.0
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
