package com.weatherapp.model

import com.weatherapp.data.db.entity.ForecastHour
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class VerdictGeneratorTest {

    private lateinit var generator: VerdictGenerator

    // Base epoch: a weekday at midnight UTC so hour offsets are predictable.
    // 2026-03-09 00:00:00 UTC = 1741478400
    private val baseEpoch = 1741478400L

    private fun hour(
        offsetHours: Int,
        tempC: Double = 15.0,
        precipProb: Double = 0.0,
        windKmh: Double = 10.0,
        weatherCode: Int = 1
    ) = ForecastHour(
        hourEpoch = baseEpoch + offsetHours * 3600L,
        temperatureC = tempC,
        precipitationProbability = precipProb,
        windSpeedKmh = windKmh,
        weatherCode = weatherCode
    )

    // Build a clear day covering daylight hours (7am–8pm UTC).
    // precipProb=0.12 keeps clear-window criteria (< 0.20) without triggering sunscreen (< 0.10).
    // weatherCode=3 (overcast) keeps clear-window criteria (0..3) without triggering sunscreen (0..2).
    private fun clearDayHours(peakTempC: Double = 15.0) = (7..20).map { h ->
        hour(h, tempC = peakTempC, precipProb = 0.12, windKmh = 10.0, weatherCode = 3)
    }

    @Before
    fun setup() {
        generator = VerdictGenerator()
    }

    // --- Temperature band tests ---

    @Test
    fun `hot temperature produces no jacket verdict`() {
        val result = generator.generateVerdict(clearDayHours(peakTempC = 30.0))
        // All-clear overrides clothing verdict when conditions are clear; force not all-clear with rain
        val hotRainyHour = listOf(hour(10, tempC = 30.0, precipProb = 0.5))
        val r = generator.generateVerdict(hotRainyHour)
        assertEquals("Nothing to grab. Warm all day.", r.verdictText)
    }

    @Test
    fun `warm temperature produces light layers verdict`() {
        val hours = listOf(hour(10, tempC = 22.0, precipProb = 0.5))
        val result = generator.generateVerdict(hours)
        assertEquals("Light layers, if anything.", result.verdictText)
    }

    @Test
    fun `mild temperature produces light jacket verdict`() {
        val hours = listOf(hour(10, tempC = 15.0, precipProb = 0.5))
        val result = generator.generateVerdict(hours)
        assertEquals("Light jacket weather.", result.verdictText)
    }

    @Test
    fun `cool temperature produces jacket weather verdict`() {
        val hours = listOf(hour(10, tempC = 8.0, precipProb = 0.5))
        val result = generator.generateVerdict(hours)
        assertEquals("Jacket day. Don't skip it.", result.verdictText)
    }

    @Test
    fun `cold temperature produces bundle up verdict`() {
        val hours = listOf(hour(10, tempC = 2.0, precipProb = 0.5))
        val result = generator.generateVerdict(hours)
        assertEquals("Bundle up. It's a cold one.", result.verdictText)
    }

    // --- Umbrella threshold boundary tests ---

    @Test
    fun `precip at exactly 40 percent triggers umbrella`() {
        val hours = listOf(hour(10, precipProb = 0.40))
        val bringList = generator.evaluateBringList(hours)
        assertTrue(bringList.contains("☂ Umbrella"))
    }

    @Test
    fun `precip at 39 percent does not trigger umbrella`() {
        val hours = listOf(hour(10, precipProb = 0.39))
        val bringList = generator.evaluateBringList(hours)
        assertFalse(bringList.contains("☂ Umbrella"))
    }

    // --- Bring list empty ---

    @Test
    fun `bring list is empty when precipitation is zero and not sunny afternoon`() {
        // Night hours only → no sunny afternoon trigger
        val hours = listOf(
            hour(0, precipProb = 0.0, weatherCode = 0),
            hour(1, precipProb = 0.0, weatherCode = 0)
        )
        val bringList = generator.evaluateBringList(hours)
        assertTrue(bringList.isEmpty())
    }

    // --- Best window tests ---

    @Test
    fun `three consecutive clear hours produce correct time range string`() {
        // Hours 9, 10, 11 UTC = 9am, 10am, 11am; clear conditions
        val hours = (9..11).map { h ->
            hour(h, precipProb = 0.05, windKmh = 10.0, weatherCode = 1)
        }
        val window = generator.calculateBestWindow(hours)
        assertNotNull("Expected non-null best window for 3 clear hours", window)
        assertTrue("Expected time range format (contains –)", window!!.contains("–"))
    }

    @Test
    fun `only one clear hour returns null best window`() {
        // Only hour 10 is clear, surrounded by rain
        val hours = listOf(
            hour(9, precipProb = 0.5, weatherCode = 61),
            hour(10, precipProb = 0.05, windKmh = 10.0, weatherCode = 1),
            hour(11, precipProb = 0.5, weatherCode = 61)
        )
        val window = generator.calculateBestWindow(hours)
        assertNull(window)
    }

    @Test
    fun `no clear hours returns null best window`() {
        val hours = (7..12).map { h -> hour(h, precipProb = 0.8, weatherCode = 63) }
        val window = generator.calculateBestWindow(hours)
        assertNull(window)
    }

    // --- All-clear tests ---

    @Test
    fun `all criteria met produces all-clear true and all-clear message`() {
        val hours = clearDayHours(peakTempC = 18.0)
        val result = generator.generateVerdict(hours)
        assertTrue(result.isAllClear)
        assertEquals(VerdictGenerator.ALL_CLEAR_MESSAGE, result.verdictText)
    }

    @Test
    fun `rain present disables all-clear`() {
        val hours = clearDayHours().toMutableList().also {
            it.add(hour(10, precipProb = 0.5))
        }
        val result = generator.generateVerdict(hours)
        assertFalse(result.isAllClear)
    }

    // --- Mood line tests ---

    @Test
    fun `storm produces stay-in mood line`() {
        val hours = listOf(hour(10, weatherCode = 95))
        val mood = generator.generateMoodLine(hours, isAllClear = false)
        assertEquals("Stay in if you can. This one means it.", mood)
    }

    @Test
    fun `heavy rain produces proper rain mood line`() {
        val hours = listOf(hour(10, precipProb = 0.75))
        val mood = generator.generateMoodLine(hours, isAllClear = false)
        assertEquals("Proper rain today. You'll want that umbrella.", mood)
    }

    @Test
    fun `moderate rain produces cosy mood line`() {
        val hours = listOf(hour(10, precipProb = 0.45))
        val mood = generator.generateMoodLine(hours, isAllClear = false)
        assertEquals("Good day to stay cosy — or just embrace the drizzle.", mood)
    }

    @Test
    fun `high wind produces breezy mood line`() {
        val hours = listOf(hour(10, windKmh = 55.0))
        val mood = generator.generateMoodLine(hours, isAllClear = false)
        assertEquals("Breezy today. Tie your hat down.", mood)
    }

    @Test
    fun `all-clear warm produces lovely mood line`() {
        val hours = listOf(hour(10, tempC = 22.0))
        val mood = generator.generateMoodLine(hours, isAllClear = true)
        assertEquals("Honestly lovely today. Eat lunch outside.", mood)
    }

    @Test
    fun `all-clear mild produces good day mood line`() {
        val hours = listOf(hour(10, tempC = 15.0))
        val mood = generator.generateMoodLine(hours, isAllClear = true)
        assertEquals("A genuinely good day. Don't forget to step outside.", mood)
    }

    @Test
    fun `overcast non-clear produces grey but manageable mood line`() {
        val hours = listOf(hour(10, precipProb = 0.1, weatherCode = 3))
        val mood = generator.generateMoodLine(hours, isAllClear = false)
        assertEquals("Grey but manageable. You've got this.", mood)
    }

    // --- Empty data guard ---

    @Test
    fun `empty hourly data returns loading state`() {
        val result = generator.generateVerdict(emptyList())
        assertFalse(result.isAllClear)
        assertNull(result.bestWindow)
        assertTrue(result.bringList.isEmpty())
        assertEquals("Checking the forecast...", result.verdictText)
    }
}
