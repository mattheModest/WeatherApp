package com.weatherapp.model

import com.weatherapp.data.db.entity.ForecastHour
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

private typealias ZonedPool = Map<ClimateZone, List<String>>

class VerdictGenerator {

    /** Override in tests to pin pool selection to a specific index. */
    internal var testDateIndex: Int? = null

    private enum class WeatherCondition {
        STORM, HEAVY_RAIN, RAIN, DRIZZLE, SNOW, VERY_WINDY, WINDY, BREEZY, OVERCAST, CLEAR
    }

    fun generateVerdict(
        hourlyData: List<ForecastHour>,
        comfortOffset: Double = 0.0,
        climateZone: ClimateZone = ClimateZone.TEMPERATE
    ): VerdictResult {
        if (hourlyData.isEmpty()) {
            return VerdictResult(
                verdictText = "Checking the forecast...",
                bringList = emptyList(),
                bestWindow = null,
                isAllClear = false,
                moodLine = "One moment."
            )
        }
        val condition = detectCondition(hourlyData)
        val bringList = evaluateBringList(hourlyData)
        val bestWindow = calculateBestWindow(hourlyData)
        val isAllClear = bringList.isEmpty() && bestWindow != null &&
            condition !in setOf(
                WeatherCondition.STORM, WeatherCondition.HEAVY_RAIN,
                WeatherCondition.SNOW, WeatherCondition.VERY_WINDY
            )
        val verdictText = generateVerdictText(condition, hourlyData, comfortOffset, climateZone, isAllClear)
        val moodLine = generateMoodLine(condition, isAllClear, hourlyData, comfortOffset, climateZone)
        return VerdictResult(verdictText, bringList, bestWindow, isAllClear, moodLine)
    }

    private fun detectCondition(hourlyData: List<ForecastHour>): WeatherCondition {
        val hasStorm     = hourlyData.any { it.weatherCode in 95..99 }
        val hasHeavyRain = hourlyData.any { it.weatherCode in listOf(63, 65, 66, 67, 82) }
        val hasSnow      = hourlyData.any { it.weatherCode in listOf(71, 73, 75, 77, 85, 86) }
        val hasRain      = hourlyData.any { it.weatherCode in listOf(61, 80, 81) }
        val hasDrizzle   = hourlyData.any { it.weatherCode in 51..57 }
        val maxWind      = hourlyData.maxOf { it.windSpeedKmh }
        return when {
            hasStorm      -> WeatherCondition.STORM
            hasHeavyRain  -> WeatherCondition.HEAVY_RAIN
            hasSnow       -> WeatherCondition.SNOW
            hasRain       -> WeatherCondition.RAIN
            hasDrizzle    -> WeatherCondition.DRIZZLE
            maxWind >= 65 -> WeatherCondition.VERY_WINDY
            maxWind >= 40 -> WeatherCondition.WINDY
            maxWind >= 25 -> WeatherCondition.BREEZY
            hourlyData.any { it.weatherCode == 3 || it.weatherCode in 45..48 } -> WeatherCondition.OVERCAST
            else          -> WeatherCondition.CLEAR
        }
    }

    private fun pick(pool: ZonedPool, zone: ClimateZone): String {
        val list = pool[zone] ?: pool[ClimateZone.TEMPERATE]!!
        val index = testDateIndex ?: (LocalDate.now().toEpochDay() % list.size).toInt()
        return list[index]
    }

    private fun generateVerdictText(
        condition: WeatherCondition,
        hourlyData: List<ForecastHour>,
        comfortOffset: Double,
        zone: ClimateZone,
        isAllClear: Boolean
    ): String = when (condition) {
        WeatherCondition.STORM      -> pick(Pools.stormVerdict, zone)
        WeatherCondition.HEAVY_RAIN -> pick(Pools.heavyRainVerdict, zone)
        WeatherCondition.SNOW       -> pick(Pools.snowVerdict, zone)
        WeatherCondition.RAIN       -> pick(Pools.rainVerdict, zone)
        WeatherCondition.DRIZZLE    -> pick(Pools.drizzleVerdict, zone)
        WeatherCondition.VERY_WINDY -> pick(Pools.veryWindyVerdict, zone)
        WeatherCondition.WINDY      -> pick(Pools.windyVerdict, zone)
        else -> if (isAllClear) pick(Pools.allClearVerdict, zone) else generateClothingVerdict(hourlyData, comfortOffset, zone)
    }

    private fun generateClothingVerdict(
        hourlyData: List<ForecastHour>,
        comfortOffset: Double,
        zone: ClimateZone
    ): String {
        val peakTempC = hourlyData.maxOf { it.temperatureC }
        val pool = when {
            peakTempC >= 28 + comfortOffset -> Pools.hotVerdict
            peakTempC >= 20 + comfortOffset -> Pools.warmVerdict
            peakTempC >= 12 + comfortOffset -> Pools.lightJacketVerdict
            peakTempC >= 5  + comfortOffset -> Pools.jacketVerdict
            else                            -> Pools.bundleUpVerdict
        }
        return pick(pool, zone)
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
            "${formatHour(bestStart)}–${formatHour(bestEnd)}"
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

    /** Returns all candidate strings for the current condition+zone — no picking, used by the UI to rotate on each resume. */
    fun generateVerdictCandidates(
        hourlyData: List<ForecastHour>,
        comfortOffset: Double = 0.0,
        climateZone: ClimateZone = ClimateZone.TEMPERATE
    ): List<String> {
        if (hourlyData.isEmpty()) return listOf("Checking the forecast...")
        val condition = detectCondition(hourlyData)
        val bringList = evaluateBringList(hourlyData)
        val bestWindow = calculateBestWindow(hourlyData)
        val isAllClear = bringList.isEmpty() && bestWindow != null &&
            condition !in setOf(
                WeatherCondition.STORM, WeatherCondition.HEAVY_RAIN,
                WeatherCondition.SNOW, WeatherCondition.VERY_WINDY
            )
        return when (condition) {
            WeatherCondition.STORM      -> getCandidates(Pools.stormVerdict, climateZone)
            WeatherCondition.HEAVY_RAIN -> getCandidates(Pools.heavyRainVerdict, climateZone)
            WeatherCondition.SNOW       -> getCandidates(Pools.snowVerdict, climateZone)
            WeatherCondition.RAIN       -> getCandidates(Pools.rainVerdict, climateZone)
            WeatherCondition.DRIZZLE    -> getCandidates(Pools.drizzleVerdict, climateZone)
            WeatherCondition.VERY_WINDY -> getCandidates(Pools.veryWindyVerdict, climateZone)
            WeatherCondition.WINDY      -> getCandidates(Pools.windyVerdict, climateZone)
            else -> if (isAllClear) getCandidates(Pools.allClearVerdict, climateZone)
                    else getClothingCandidates(hourlyData, comfortOffset, climateZone)
        }
    }

    /** Returns all candidate mood line strings for the current condition+zone. */
    fun generateMoodCandidates(
        hourlyData: List<ForecastHour>,
        isAllClear: Boolean,
        comfortOffset: Double = 0.0,
        climateZone: ClimateZone = ClimateZone.TEMPERATE
    ): List<String> {
        if (hourlyData.isEmpty()) return listOf("One moment.")
        val condition = detectCondition(hourlyData)
        val peakTemp = hourlyData.maxOf { it.temperatureC }
        val lovelyThreshold = 20.0 + comfortOffset
        return when (condition) {
            WeatherCondition.STORM                   -> getCandidates(Pools.stormMood, climateZone)
            WeatherCondition.HEAVY_RAIN              -> getCandidates(Pools.heavyRainMood, climateZone)
            WeatherCondition.RAIN                    -> getCandidates(Pools.rainMood, climateZone)
            WeatherCondition.DRIZZLE                 -> getCandidates(Pools.drizzleMood, climateZone)
            WeatherCondition.SNOW                    -> getCandidates(Pools.snowMood, climateZone)
            WeatherCondition.VERY_WINDY,
            WeatherCondition.WINDY                   -> getCandidates(Pools.windMood, climateZone)
            WeatherCondition.BREEZY                  -> if (isAllClear && peakTemp >= lovelyThreshold)
                                                            getCandidates(Pools.allClearWarmMood, climateZone)
                                                        else getCandidates(Pools.breezeMood, climateZone)
            else -> when {
                isAllClear && peakTemp >= lovelyThreshold -> getCandidates(Pools.allClearWarmMood, climateZone)
                isAllClear                                -> getCandidates(Pools.allClearNeutralMood, climateZone)
                else                                      -> getCandidates(Pools.greyMood, climateZone)
            }
        }
    }

    private fun getCandidates(pool: ZonedPool, zone: ClimateZone): List<String> =
        pool[zone] ?: pool[ClimateZone.TEMPERATE]!!

    private fun getClothingCandidates(
        hourlyData: List<ForecastHour>,
        comfortOffset: Double,
        zone: ClimateZone
    ): List<String> {
        val peakTempC = hourlyData.maxOf { it.temperatureC }
        val pool = when {
            peakTempC >= 28 + comfortOffset -> Pools.hotVerdict
            peakTempC >= 20 + comfortOffset -> Pools.warmVerdict
            peakTempC >= 12 + comfortOffset -> Pools.lightJacketVerdict
            peakTempC >= 5  + comfortOffset -> Pools.jacketVerdict
            else                            -> Pools.bundleUpVerdict
        }
        return getCandidates(pool, zone)
    }

    /** Public overload kept for test compatibility — climateZone defaults to TEMPERATE. */
    fun generateMoodLine(
        hourlyData: List<ForecastHour>,
        isAllClear: Boolean,
        comfortOffset: Double = 0.0,
        climateZone: ClimateZone = ClimateZone.TEMPERATE
    ): String {
        val condition = detectCondition(hourlyData)
        return generateMoodLine(condition, isAllClear, hourlyData, comfortOffset, climateZone)
    }

    private fun generateMoodLine(
        condition: WeatherCondition,
        isAllClear: Boolean,
        hourlyData: List<ForecastHour>,
        comfortOffset: Double,
        zone: ClimateZone
    ): String {
        val peakTemp = hourlyData.maxOf { it.temperatureC }
        val lovelyThreshold = 20.0 + comfortOffset
        return when (condition) {
            WeatherCondition.STORM                   -> pick(Pools.stormMood, zone)
            WeatherCondition.HEAVY_RAIN              -> pick(Pools.heavyRainMood, zone)
            WeatherCondition.RAIN                    -> pick(Pools.rainMood, zone)
            WeatherCondition.DRIZZLE                 -> pick(Pools.drizzleMood, zone)
            WeatherCondition.SNOW                    -> pick(Pools.snowMood, zone)
            WeatherCondition.VERY_WINDY,
            WeatherCondition.WINDY                   -> pick(Pools.windMood, zone)
            WeatherCondition.BREEZY                  -> if (isAllClear && peakTemp >= lovelyThreshold)
                                                            pick(Pools.allClearWarmMood, zone)
                                                        else pick(Pools.breezeMood, zone)
            else -> when {
                isAllClear && peakTemp >= lovelyThreshold -> pick(Pools.allClearWarmMood, zone)
                isAllClear                                -> pick(Pools.allClearNeutralMood, zone)
                else                                      -> pick(Pools.greyMood, zone)
            }
        }
    }

    companion object {
        const val UMBRELLA_THRESHOLD = 0.40

        /** Kept as a named constant — also lives at index 0 of allClearVerdict[TEMPERATE]. */
        const val ALL_CLEAR_MESSAGE = "You're good. Go live your day."

        fun climateZoneFromAbsLat(absLat: Double): ClimateZone = when {
            absLat < 20 -> ClimateZone.TROPICAL
            absLat < 32 -> ClimateZone.SUBTROPICAL
            absLat < 45 -> ClimateZone.TEMPERATE
            absLat < 57 -> ClimateZone.OCEANIC
            else        -> ClimateZone.NORDIC
        }

        /**
         * Returns a temperature offset (°C) based on the user's latitude and current month.
         * Applied to all clothing thresholds: positive = heat-acclimated (higher bar to feel cool),
         * negative = cold-acclimated (lower bar to feel warm).
         */
        fun computeComfortOffset(latGrid: Double, monthOfYear: Int): Double {
            val absLat = abs(latGrid)
            val isNorthern = latGrid >= 0
            val isLocalSummer = if (isNorthern) monthOfYear in 6..8  else monthOfYear == 12 || monthOfYear in 1..2
            val isLocalWinter = if (isNorthern) monthOfYear == 12 || monthOfYear in 1..2 else monthOfYear in 6..8
            val isLocalAutumn = if (isNorthern) monthOfYear in 9..11 else monthOfYear in 3..5
            val (summerOffset, winterOffset) = when {
                absLat < 20 -> Pair(5.0,  5.0)
                absLat < 30 -> Pair(3.0,  1.0)
                absLat < 40 -> Pair(-2.0, -4.0)
                absLat < 50 -> Pair(-6.0, -8.0)
                absLat < 65 -> Pair(-10.0,-12.0)
                else        -> Pair(-13.0,-15.0)
            }
            return when {
                isLocalSummer -> summerOffset
                isLocalWinter -> winterOffset
                isLocalAutumn -> summerOffset + 1.0
                else          -> (summerOffset + winterOffset) / 2.0
            }
        }

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

// ---------------------------------------------------------------------------
// Copy pools — all date-seeded, all zone-aware
// Index 0 of ClimateZone.TEMPERATE is the canonical "anchor" line for each pool
// (used by tests to assert deterministic output via testDateIndex = 0).
// ---------------------------------------------------------------------------

private object Pools {
    private val T  = ClimateZone.TROPICAL
    private val ST = ClimateZone.SUBTROPICAL
    private val TE = ClimateZone.TEMPERATE
    private val OC = ClimateZone.OCEANIC
    private val NO = ClimateZone.NORDIC

    // ── Verdict pools ────────────────────────────────────────────────────────

    val allClearVerdict: ZonedPool = mapOf(
        T  to listOf(
            "You're good. Go live your day.",
            "Nothing to carry. Hot and clear — enjoy it.",
            "Forecast is clean. No excuses to stay inside.",
            "Zero weather drama today. Enjoy it.",
            "Clear skies. Go touch some sunshine."
        ),
        ST to listOf(
            "You're good. Go live your day.",
            "Clear skies. Nothing to carry, nowhere to be but outside.",
            "Clean forecast. Go enjoy it.",
            "Not a cloud in sight. Make the most of it.",
            "Zero weather drama today."
        ),
        TE to listOf(
            "You're good. Go live your day.",
            "Nothing to grab. Nothing to dread. Weird, right?",
            "Forecast is clean. You have no excuse to stay in.",
            "Zero weather drama today. Enjoy it while it lasts.",
            "Clear skies. Go touch grass. Literally."
        ),
        OC to listOf(
            "You're good. Go live your day.",
            "Actually a decent day — don't waste it indoors.",
            "Rare one today. Get outside while you can.",
            "The weather's cooperating for once. Return the favor.",
            "Clear and calm. Practically a holiday."
        ),
        NO to listOf(
            "You're good. Go live your day.",
            "This is what you waited all year for. Get outside.",
            "It's genuinely nice out. Don't spend it indoors.",
            "The sun showed up. The least you can do is meet it halfway.",
            "A proper good day. Don't overthink it, just go."
        )
    )

    val stormVerdict: ZonedPool = mapOf(
        T  to listOf(
            "Stay inside. This is a real storm.",
            "Don't go out. Seriously.",
            "Storm incoming. This isn't negotiable.",
            "Dangerous weather today. Stay in.",
            "This is a bad one. Don't chance it."
        ),
        ST to listOf(
            "Storm day. Stay in if you possibly can.",
            "Not a good day to be outside. Stay home.",
            "Serious weather today. Take it seriously.",
            "The kind of storm that makes the news. Stay in.",
            "Stay inside. This one means it."
        ),
        TE to listOf(
            "Stay in if you can. This one means it.",
            "Not a day to be a hero. Stay inside.",
            "The forecast is using its angry voice today.",
            "Nature's having a moment. Respect it.",
            "Serious weather. Take it seriously or find out."
        ),
        OC to listOf(
            "Storm today. Even by your standards, it's rough out.",
            "Worse than usual — and that's saying something.",
            "This is a proper one. Stay in if you can.",
            "Nastier than normal out there. Take it seriously.",
            "Even locals should think twice today. Stay in."
        ),
        NO to listOf(
            "Storm day. Even for you, it's rough out there.",
            "Worse than the usual grim. Stay in if possible.",
            "This one's legitimate. Stay inside.",
            "Even your weather tolerance has limits. Today tests them.",
            "Properly bad. Even by Nordic standards."
        )
    )

    val heavyRainVerdict: ZonedPool = mapOf(
        T  to listOf(
            "Heavy rain. Get covered — properly covered.",
            "Serious rain today. Waterproofing required.",
            "It's going to properly rain on you. Plan for it.",
            "Real rain today. Don't fight it, just dress for it.",
            "The sky means business today."
        ),
        ST to listOf(
            "Heavy rain today. Umbrella is non-negotiable.",
            "This is a real rain day. Dress like you mean it.",
            "Proper rain incoming. No such thing as overdressed.",
            "It's going to pour. The umbrella isn't optional.",
            "Heavy rain. You know what to do."
        ),
        TE to listOf(
            "Proper rain today. You'll want that umbrella.",
            "This isn't drizzle. This is a weather event.",
            "Heavy rain incoming. The umbrella is non-negotiable.",
            "It's going to properly rain on you. Dress like you know it.",
            "No such thing as overdressed for this one."
        ),
        OC to listOf(
            "Heavy rain today — worse than the usual drizzle.",
            "Actually serious rain. Umbrella and waterproofs.",
            "Proper downpour today. Don't be underprepared.",
            "Real rain. Even by your standards, this is a wet one.",
            "Heavy one today. Treat it accordingly."
        ),
        NO to listOf(
            "Heavy rain. Properly heavy. You know what to do.",
            "Real rain — not the gentle kind. Dress for it.",
            "This is a wet day even by local standards.",
            "Umbrella. Waterproof. Go.",
            "Proper downpour today. You know the drill."
        )
    )

    val rainVerdict: ZonedPool = mapOf(
        T  to listOf(
            "Carry an umbrella. Rain likely.",
            "Chance of rain. Better covered than not.",
            "Probably going to rain. Plan for it.",
            "Bring something waterproof — just in case.",
            "Rain on the way. Cover yourself."
        ),
        ST to listOf(
            "Bring the umbrella. Rain's coming.",
            "Rain likely today. Go prepared.",
            "You'll want an umbrella. Rain's in the forecast.",
            "Carry something waterproof. Rain's on the way.",
            "Umbrella weather."
        ),
        TE to listOf(
            "Yeah, bring the umbrella.",
            "It's a wet one today.",
            "Don't fight the rain. Just dress for it.",
            "Soggy out there — plan accordingly.",
            "It's going to rain on you. Just accept it."
        ),
        OC to listOf(
            "Rain again. You knew when you moved here.",
            "More rain. Business as usual. Bring the umbrella.",
            "Raining. Obviously. You know what to do.",
            "Wet day ahead. Not exactly a surprise.",
            "Umbrella. You don't even need to think about it at this point."
        ),
        NO to listOf(
            "Rain today. Hardly worth mentioning, but here we are.",
            "Wet one. You've survived worse.",
            "Rain. The usual. Umbrella if you care.",
            "It's raining. Fine. Dress for it.",
            "More rain. This is just the weather."
        )
    )

    val drizzleVerdict: ZonedPool = mapOf(
        T  to listOf(
            "Light rain out there. Might want an umbrella.",
            "Drizzle today — carry something just in case.",
            "A bit of light rain. Not terrible, but cover up.",
            "Drizzly out there. Better with an umbrella.",
            "Might get a little wet. Worth carrying something."
        ),
        ST to listOf(
            "Drizzle today. Light jacket or umbrella.",
            "On-and-off showers — go prepared.",
            "Light rain likely. Carry something just in case.",
            "Drizzly out there. Not terrible but go covered.",
            "Grab an umbrella. Just in case."
        ),
        TE to listOf(
            "Drizzly out there. Could be worse.",
            "Light rain — carry something waterproof.",
            "On and off showers. Umbrella wouldn't hurt.",
            "Damp day. You'll be fine, but go prepared.",
            "Classic grey and drizzly. You know how this goes."
        ),
        OC to listOf(
            "Drizzle. Obviously. Carry an umbrella.",
            "Light rain — the usual. You're used to this.",
            "Grey and damp. Standard. Go prepared.",
            "Drizzly. Standard issue.",
            "More drizzle. This is just the weather doing weather things."
        ),
        NO to listOf(
            "Drizzle. Fine. Carry something.",
            "Light rain — barely worth noting. Umbrella if you want.",
            "Damp out there. Not new information.",
            "Drizzly and grey. You've seen worse. Go.",
            "It's drizzling. You've dressed for worse."
        )
    )

    val snowVerdict: ZonedPool = mapOf(
        T  to listOf(
            "Snow. This is historic. Also dress like it's an emergency.",
            "SNOW. Wear everything warm you own.",
            "It's actually snowing. Dress extremely warm.",
            "Snow day. This is not normal — treat it accordingly.",
            "Snowing. Every warm layer you own. Now."
        ),
        ST to listOf(
            "Snow today — rare but real. Dress for it.",
            "It's snowing. Bundle up properly.",
            "Snow day. Dress warmer than you think you need to.",
            "Rare snow event. Layer up completely.",
            "It's snowing out there. Take it seriously."
        ),
        TE to listOf(
            "Snow day. Boots and real layers, obviously.",
            "It's snowing. Dress like you've seen winter before.",
            "Cold, white, and unforgiving. You know what to wear.",
            "Winter's showing off again. Play along with proper gear.",
            "The boots aren't optional today."
        ),
        OC to listOf(
            "Snow today — rarer here than you'd think. Dress for it.",
            "It's actually snowing. Layer up properly.",
            "Snow out there. Don't be underdressed.",
            "White stuff's falling. Treat it like the real thing it is.",
            "Snow day. Boots and layers — the full kit."
        ),
        NO to listOf(
            "Snow. Yes, again. Boots and layers, you know the drill.",
            "It's snowing. This is Tuesday. Dress accordingly.",
            "More snow. Normal. You've got this.",
            "Snow day. You've been training for this your whole life.",
            "Snowing. Standard. Dress like you know how cold it is."
        )
    )

    val veryWindyVerdict: ZonedPool = mapOf(
        T  to listOf(
            "Dangerous winds today. Stay inside if you can.",
            "Very strong winds. Not a good day to be out.",
            "Wind is intense today. Limit time outside.",
            "Strong gusts — potentially dangerous. Be careful.",
            "Extreme wind. Stay in if possible."
        ),
        ST to listOf(
            "Very windy today. Secure anything loose.",
            "Strong gusts out there. Not a day for umbrellas.",
            "Intense wind today. Hold onto your stuff.",
            "Properly wild out there. Be careful.",
            "High winds. Things will blow around. You've been warned."
        ),
        TE to listOf(
            "Hang onto everything. The wind's not messing around.",
            "Strong gusts today. Loose items will find new homes.",
            "Windy enough to make walking feel like an argument.",
            "It's wild out there. Hold your ground — literally.",
            "Very windy. Umbrellas will die. Don't even try."
        ),
        OC to listOf(
            "Even by your standards, it's wild out today.",
            "Worse than the usual breezy. Properly windy.",
            "This is real wind — beyond the background stuff. Be careful.",
            "Nastier than usual out there. Head down.",
            "Strong gusts — not just breezy. Dress and brace accordingly."
        ),
        NO to listOf(
            "Very windy — even for here. Take it seriously.",
            "Winds are intense today. Even your tolerance has limits.",
            "This is beyond normal windy. Head down and hold on.",
            "Wild out there today. Don't underestimate it.",
            "Strong winds. Properly strong. Dress for it and be careful."
        )
    )

    val windyVerdict: ZonedPool = mapOf(
        T  to listOf(
            "Gusty out there today. Not great for being outside.",
            "Strong winds today. Worth noting.",
            "Windy out there — more than you're used to.",
            "Decent wind today. Hold onto your hat.",
            "Windy. Unusual for you. Worth noting."
        ),
        ST to listOf(
            "Gusty out there. Say goodbye to your hat.",
            "It's properly windy. Accept your new hairstyle now.",
            "Strong gusts today. Loose items will find new homes.",
            "Hold onto your stuff. The wind's in charge today.",
            "Windy enough to make the day a bit dramatic."
        ),
        TE to listOf(
            "Gusty out there. Say goodbye to your hat.",
            "It's properly windy. Accept your new hairstyle now.",
            "Hold onto everything. The wind's not messing around.",
            "Strong gusts today. Loose items will find new homes.",
            "Windy enough to make walking feel like an argument."
        ),
        OC to listOf(
            "Windy — more than the usual breezy. Head down.",
            "Gusts today. Beyond the background noise.",
            "It's blowing out there. You'll feel this one.",
            "Properly windy. Not just the usual.",
            "Wind's up today. More than normal."
        ),
        NO to listOf(
            "Windy. Not unusual, but worth noting today.",
            "Gusty out there. You've dressed for worse.",
            "It's blowing. Dress for wind and get on with it.",
            "Strong breeze. You know the routine.",
            "Wind's picking up. Nothing you haven't seen."
        )
    )

    val hotVerdict: ZonedPool = mapOf(
        T  to listOf(
            "Nothing to grab. Hot as always.",
            "Same as yesterday. You know the drill.",
            "Classic day. Light clothes, done.",
            "Warm and then some. Keep it minimal.",
            "You don't need layers — you need water."
        ),
        ST to listOf(
            "Nothing to grab. Warm all day.",
            "It's hot. Leave everything at home. Just go.",
            "Pure summer energy. Don't ruin it with layers.",
            "Warm enough that you'll regret anything extra.",
            "Leave the jacket. You'll know why."
        ),
        TE to listOf(
            "Nothing to grab. Warm all day.",
            "Actually warm today. Dress light and enjoy the novelty.",
            "Hot one incoming. Keep it simple.",
            "Jacket? Absolutely not. Don't even look at it.",
            "Warm enough that layers would be a mistake."
        ),
        OC to listOf(
            "Nothing to grab. Genuinely warm today — savor it.",
            "Hot by local standards. Dress accordingly and enjoy.",
            "Warm out there. This doesn't happen often — dress light.",
            "Actual warmth. No layers. Go live.",
            "It's properly warm. Don't waste it overthinking your outfit."
        ),
        NO to listOf(
            "Shorts weather. Yes, actually.",
            "It's warm. By any definition. Go outside immediately.",
            "Summer arrived. No layers. No questions.",
            "This is the good stuff. Dress light and go be in it.",
            "Warm enough for shorts. You've earned this."
        )
    )

    val warmVerdict: ZonedPool = mapOf(
        T  to listOf(
            "Surprisingly cool today. You might want a light layer.",
            "A little on the cooler side — bring something thin.",
            "Not your average hot day. Light layer just in case.",
            "Cooler than usual. A thin something wouldn't hurt.",
            "Light layers — it's actually not that warm today."
        ),
        ST to listOf(
            "Light layers, if anything.",
            "A little something — or nothing. Your call.",
            "Could get cool, but don't overthink it.",
            "One layer should cover it today.",
            "Comfortable out there. Dress easy."
        ),
        TE to listOf(
            "Light layers, if anything.",
            "Thin layer — or don't. You'll survive either way.",
            "That annoying in-between temperature. Have fun deciding.",
            "Technically you could skip the jacket. Technically.",
            "Not cold enough to complain about. Not warm enough to be happy."
        ),
        OC to listOf(
            "Light layers — it's pleasant but don't push it.",
            "Not bad out. A thin layer and you're sorted.",
            "Mild enough to enjoy, cool enough to need something.",
            "One layer. That's all. You've done this before.",
            "Actually quite nice. Light layer, you're set."
        ),
        NO to listOf(
            "Practically t-shirt weather. Light layer if you must.",
            "This counts as warm here. You know what to wear.",
            "Light layers — or honestly just a t-shirt. Live a little.",
            "Nice enough to underdress a bit. You've earned it.",
            "Warm by local standards. Dress accordingly and enjoy."
        )
    )

    val lightJacketVerdict: ZonedPool = mapOf(
        T  to listOf(
            "Cold day. Jacket or at least layers.",
            "Chilly for you. Don't underestimate it.",
            "This is cold. Bring a proper jacket.",
            "Surprisingly cold today. Take it seriously.",
            "You'll want a jacket — yes, really."
        ),
        ST to listOf(
            "Light jacket weather.",
            "That thin jacket in the back of the closet? Today's its day.",
            "Jacket optional, but you'll want it.",
            "Cool enough to feel crisp. Grab a light layer.",
            "Not cold, not warm. Jacket territory."
        ),
        TE to listOf(
            "Light jacket weather.",
            "That jacket you forgot you owned? Today's its moment.",
            "Grab something light. You'll be smug later.",
            "Cool, crisp, and slightly judging you if you underdress.",
            "Jacket weather. Not negotiating on this one."
        ),
        OC to listOf(
            "Light jacket — pretty standard day, honestly.",
            "Normal day. Jacket, done.",
            "Grab a jacket. Nothing dramatic.",
            "Jacket weather — which is most days. You know the drill.",
            "The usual. Jacket and get on with it."
        ),
        NO to listOf(
            "Light jacket at most. Not that cold.",
            "Jacket if you want — you'll probably be fine without.",
            "Mild enough. Something light if it makes you feel better.",
            "Not exactly warm, but hardly jacket-mandatory.",
            "A layer or two. This barely qualifies as cold."
        )
    )

    val jacketVerdict: ZonedPool = mapOf(
        T  to listOf(
            "Extremely cold today. Full jacket, layers underneath.",
            "This is genuinely cold weather. Dress accordingly.",
            "Bundle up — this isn't normal for you.",
            "Cold enough to be a real problem. Take it seriously.",
            "You're going to want everything warm you own."
        ),
        ST to listOf(
            "Jacket day. Don't skip it.",
            "It's cold. Wear the jacket. No, the real one.",
            "A proper cold day. The thin one won't cut it.",
            "Dress like you know what cold actually feels like.",
            "Jacket on. No arguments."
        ),
        TE to listOf(
            "Jacket day. Don't skip it.",
            "It's cold. Wear the jacket. No, the real one.",
            "A proper coat day. The thin jacket won't cut it.",
            "Dress like you know what 5 degrees feels like.",
            "Your past self already knows you need the jacket."
        ),
        OC to listOf(
            "Proper jacket day. No half measures.",
            "Cold enough to mean it. Dress properly.",
            "Real coat weather today. The thin one stays home.",
            "It's cold. The jacket isn't optional today.",
            "Full jacket. You'll regret skimping."
        ),
        NO to listOf(
            "Jacket weather. Which is most of the year, but still.",
            "Cold, but nothing dramatic. Jacket and move on.",
            "Jacket on. Standard.",
            "Not exactly warm, but you've seen worse. Jacket and go.",
            "This is just Tuesday in jacket form. You know the drill."
        )
    )

    val bundleUpVerdict: ZonedPool = mapOf(
        T  to listOf(
            "This is an emergency by your standards. Everything warm you own.",
            "Genuinely freezing for your climate. Pile on the layers.",
            "This is not a drill. Wear everything warm you own.",
            "Dangerously cold by local standards. Bundle up completely.",
            "Cold snap. Every warm thing you own. All of it."
        ),
        ST to listOf(
            "Bundle up. It's a cold one.",
            "Full winter kit today. No compromises.",
            "Layers. All of them.",
            "The kind of cold that gets into everything.",
            "Don't underestimate it out there today."
        ),
        TE to listOf(
            "Bundle up. It's a cold one.",
            "Layers. All of them. No, more than that.",
            "It's the kind of cold that has an opinion about you.",
            "Don't underdress and then act surprised at the bus stop.",
            "Full winter armor. This is not a drill."
        ),
        OC to listOf(
            "Bundle up. Properly cold today.",
            "Full winter kit. No shortcuts.",
            "It's genuinely cold. Don't test it.",
            "Everything warm. This is a real one.",
            "Cold enough to regret cutting corners. Don't."
        ),
        NO to listOf(
            "Bundle up. Even by your standards.",
            "Cold enough to matter, even for you. Layer properly.",
            "This is actual cold. Not the mild stuff. Dress for it.",
            "Full kit today. You know what cold really means — this qualifies.",
            "Proper cold. Even you'll feel it. Bundle up."
        )
    )

    // ── Mood line pools ───────────────────────────────────────────────────────

    val stormMood: ZonedPool = mapOf(
        T  to listOf(
            "Dangerous out there. Don't test it.",
            "This is not the weather to go out in. Stay inside.",
            "Nature's in a genuinely bad mood today.",
            "The storm will pass. Until then, stay put."
        ),
        ST to listOf(
            "Not a good day to be a hero. Stay inside.",
            "The forecast is using its angry voice today.",
            "Nature's having a moment. Respect it.",
            "This isn't a drill. Stay in."
        ),
        TE to listOf(
            "Stay in if you can. This one means it.",
            "The forecast is using its angry voice today.",
            "Nature's having a moment. Respect it.",
            "Serious weather. Take it seriously or find out."
        ),
        OC to listOf(
            "Worse than usual — and it's usually rough. Stay in.",
            "This is legitimately bad, even by local standards.",
            "Storm today. Properly. Not the drizzle kind.",
            "Even locals are staying in. Take the hint."
        ),
        NO to listOf(
            "Worse than the background grim. Take it seriously.",
            "This is legitimate bad weather, even for you.",
            "Even your weather tolerance has limits. Today tests them.",
            "Properly bad. Stay in if you can."
        )
    )

    val heavyRainMood: ZonedPool = mapOf(
        T  to listOf(
            "The sky means business today.",
            "Heavy rain incoming. Have a plan.",
            "It's going to properly soak you. Plan accordingly.",
            "Real rain today. Don't fight it."
        ),
        ST to listOf(
            "Proper rain today. You'll want that umbrella.",
            "Heavy rain incoming. The umbrella is non-negotiable.",
            "It's going to properly pour. Dress accordingly.",
            "No such thing as overdressed for this one."
        ),
        TE to listOf(
            "Proper rain today. You'll want that umbrella.",
            "No such thing as overdressed for this one.",
            "Heavy rain incoming. Take it seriously.",
            "It's going to pour. You've been warned."
        ),
        OC to listOf(
            "Heavy rain — worse than the usual grey. Umbrella up.",
            "Actually serious rain. Not just the normal overcast.",
            "Proper downpour. Even by local standards, this is wet.",
            "Heavy one. Take the umbrella seriously today."
        ),
        NO to listOf(
            "Heavy rain. The real kind. You know what to do.",
            "This is a wet day even by local standards.",
            "Properly heavy today. Not just spitting.",
            "It's going to pour. Even you'll feel it."
        )
    )

    val rainMood: ZonedPool = mapOf(
        T  to listOf(
            "Rain on the way. Cover yourself.",
            "Wet out there. You know the drill.",
            "Rain likely — plan for it.",
            "On-and-off rain. Stay covered."
        ),
        ST to listOf(
            "Don't fight the rain. You will lose.",
            "Soggy out there. Plan accordingly.",
            "It's going to rain on you. Best just accept it.",
            "Drizzly and dramatic. Classic."
        ),
        TE to listOf(
            "Good day to stay cosy — or just embrace the drizzle.",
            "It's going to rain on you. Best just accept it.",
            "Don't fight the rain. You will lose.",
            "Soggy out there. Plan accordingly."
        ),
        OC to listOf(
            "Rain. Again. You knew when you moved here.",
            "More rain. Business as usual.",
            "Raining. Naturally. You know the drill.",
            "Wet and grey. Standard."
        ),
        NO to listOf(
            "Rain. Fine. You've dealt with worse.",
            "It's raining. Carry something. Move on.",
            "Rain today. You'll be fine. You always are.",
            "More rain. This is basically baseline weather."
        )
    )

    val drizzleMood: ZonedPool = mapOf(
        T  to listOf(
            "Light rain out there. Might want an umbrella.",
            "Drizzle today. Better prepared than not.",
            "A bit of light rain. Cover up.",
            "Might get a little wet. Worth having an umbrella."
        ),
        ST to listOf(
            "Drizzle today. Not a big deal, but carry something.",
            "On-and-off showers. Go prepared.",
            "Grey and light rain. Grab something just in case.",
            "Light rain likely. Worth having an umbrella."
        ),
        TE to listOf(
            "Drizzly out there. Could be worse.",
            "On and off showers. Umbrella wouldn't hurt.",
            "Damp day. You'll be fine, but go prepared.",
            "Classic grey and drizzly. You know how this goes."
        ),
        OC to listOf(
            "Drizzle. Standard. Carry an umbrella.",
            "Light rain — the usual. You're used to this.",
            "Grey and damp. Standard.",
            "More drizzle. This is just the weather doing its thing."
        ),
        NO to listOf(
            "Drizzle. Fine. Carry something.",
            "Light rain — barely worth noting.",
            "Damp out there. Not new information.",
            "It's drizzling. You've dressed for worse."
        )
    )

    val snowMood: ZonedPool = mapOf(
        T  to listOf(
            "This is genuinely unusual. Stay warm and be careful.",
            "Snow day — an event, really. Enjoy it carefully.",
            "Not normal for you. Dress accordingly and take it easy.",
            "The snow's pretty but be careful out there."
        ),
        ST to listOf(
            "Snow day — rarer here than you'd think.",
            "It's snowing. Take it seriously.",
            "Layer up and go carefully.",
            "Not your everyday weather. Dress for it."
        ),
        TE to listOf(
            "Pretty out there if you dress for it.",
            "Looks like a hot drink kind of morning.",
            "The world's quieter in the snow. Enjoy it if you can.",
            "Winter's doing its thing. Lean into it."
        ),
        OC to listOf(
            "Snow today — rarer here than you'd expect.",
            "Actual snow. Dress accordingly and take it easy.",
            "White stuff's falling. Enjoy the novelty.",
            "Snow day. A bit of extra care goes a long way."
        ),
        NO to listOf(
            "Snow. Tuesday. You know what to do.",
            "More of the white stuff. Standard.",
            "It's snowing. Nothing you haven't handled before.",
            "Snow day. Not exactly unusual. Dress accordingly."
        )
    )

    val windMood: ZonedPool = mapOf(
        T  to listOf(
            "Not ideal out there. Stay careful.",
            "Wind's intense today. More than you're used to.",
            "Gusty and a bit dramatic. Take care.",
            "Hold onto your stuff. Literally."
        ),
        ST to listOf(
            "Don't bother with an umbrella, it'll just flip.",
            "Blustery out there. Great hair day — for someone else.",
            "Wind's in a bad mood. Don't take it personally.",
            "Hold onto your hat. Literally."
        ),
        TE to listOf(
            "Don't bother with an umbrella, it'll just flip.",
            "Blustery out there. Great hair day — for someone else.",
            "Nature's fan is on full blast today.",
            "Wind's in a bad mood. Don't take it personally.",
            "Properly dramatic out there. Duck down."
        ),
        OC to listOf(
            "Windier than usual — even by local standards.",
            "Don't bother with an umbrella, it'll just flip.",
            "Blustery out there. More so than normal.",
            "Wind's up today. Properly up."
        ),
        NO to listOf(
            "Windy. Nothing new, but worth respecting.",
            "It's blowing out there. You know the drill.",
            "Wind's up. Dress for it.",
            "Gusty and cold. Standard combo. You've got this."
        )
    )

    val breezeMood: ZonedPool = mapOf(
        T  to listOf(
            "Breezy out there. A nice change, honestly.",
            "Light wind — refreshing today.",
            "A little breeze. Not bad at all.",
            "Cooler than usual with the wind. Enjoy it."
        ),
        ST to listOf(
            "Breezy today. Keeps things interesting.",
            "A bit of wind out there. Nothing dramatic.",
            "Light breeze. Pretty nice, actually.",
            "Windy enough to feel it. Not enough to worry."
        ),
        TE to listOf(
            "Breezy today. Tie your hat down.",
            "A bit of wind out there. Refreshing.",
            "Breezy but manageable.",
            "Light wind today. Nothing dramatic."
        ),
        OC to listOf(
            "Breezy. The usual.",
            "Wind's doing its thing. Background stuff.",
            "Light breeze. Nothing unusual.",
            "Windy — which is just how it goes here."
        ),
        NO to listOf(
            "Breezy. Baseline.",
            "Light wind. Nothing to think about.",
            "It's a bit breezy. And?",
            "Wind's there. As always. Dress for it."
        )
    )

    val allClearWarmMood: ZonedPool = mapOf(
        T  to listOf(
            "Hot and clear. Classic.",
            "Another beautiful day. Go enjoy it.",
            "Perfect out there. You know what to do.",
            "Lovely day. Get outside."
        ),
        ST to listOf(
            "Honestly lovely today. Eat lunch outside.",
            "The forecast is embarrassingly good today.",
            "This is a genuinely nice one. Go feel it.",
            "Take the long way home. It's worth it today."
        ),
        TE to listOf(
            "Honestly lovely today. Eat lunch outside.",
            "The forecast is embarrassingly good today.",
            "This is a genuinely nice one. Go feel it.",
            "Take the long way home. It's worth it today.",
            "Weather's showing off. You should be outside."
        ),
        OC to listOf(
            "Actually nice out. Properly nice. Go outside.",
            "This is a rare one. Don't waste it.",
            "The weather's cooperating. Return the favor.",
            "Genuinely lovely. Make the most of it."
        ),
        NO to listOf(
            "This is what you waited all winter for. Get outside.",
            "Genuinely warm and clear. Get outside immediately.",
            "The sun's out and it's warm. Go. Go now.",
            "Don't you dare spend this indoors."
        )
    )

    val allClearNeutralMood: ZonedPool = mapOf(
        T  to listOf(
            "Hot and clear. Another solid day.",
            "Nothing dramatic. Just go.",
            "Clear and warm. Standard.",
            "Clean forecast. Enjoy it."
        ),
        ST to listOf(
            "A genuinely good day. Don't forget to step outside.",
            "Not flashy, but solid. Take the win.",
            "Nothing dramatic. Just a good, boring, lovely day.",
            "No complaints from the sky. Make the most of it."
        ),
        TE to listOf(
            "A genuinely good day. Don't forget to step outside.",
            "Not flashy, but solid. Take the win.",
            "Nothing dramatic. Just a good, boring, lovely day.",
            "No complaints from the sky. Make the most of it.",
            "Quietly nice out there. Easy one."
        ),
        OC to listOf(
            "Not bad, actually. Go enjoy it.",
            "Decent out there. More than you might expect.",
            "Clean and calm. Take the opportunity.",
            "Better than usual. Go outside."
        ),
        NO to listOf(
            "Nice by local standards. Get outside.",
            "A good day. Not exceptional, but good.",
            "Clear and not cold. Go be in it.",
            "Decent out there. Don't overthink it."
        )
    )

    val greyMood: ZonedPool = mapOf(
        T  to listOf(
            "Not the best, not the worst. You'll manage.",
            "Grey and okay. Fine.",
            "Manageable out there. Just go.",
            "Not ideal but not terrible."
        ),
        ST to listOf(
            "Grey but manageable. You've got this.",
            "Not beautiful, not terrible. Very Tuesday.",
            "The weather's fine. It's just not trying very hard.",
            "Forgettable forecast. Which is fine, actually."
        ),
        TE to listOf(
            "Grey but manageable. You've got this.",
            "Not beautiful, not terrible. Very Tuesday.",
            "The weather's fine. It's just not trying very hard.",
            "Forgettable forecast. Which is fine, actually.",
            "It's whatever out there. You'll be fine."
        ),
        OC to listOf(
            "Grey and overcast. As per usual.",
            "Standard grey day. You're used to this.",
            "Not exciting, but fine. Go do your thing.",
            "The usual. You'll survive."
        ),
        NO to listOf(
            "Grey and cold. Normal.",
            "Standard day. You've had worse.",
            "Nothing special out there. Just go.",
            "Bleak but manageable. As expected."
        )
    )
}
