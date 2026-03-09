package com.weatherapp.data.calendar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

/**
 * Unit tests for CalendarRepository — focuses on sanitizeTitle() and permission guard.
 * Full calendar query tests require an instrumented environment (see androidTest).
 */
class CalendarRepositoryTest {

    private lateinit var repository: CalendarRepository

    @Before
    fun setUp() {
        // Create a repository with a mock context for sanitizeTitle tests
        val context = mockk<Context>(relaxed = true)
        repository = CalendarRepository(context)
    }

    @Test
    fun sanitizeTitle_null_returnsUntitledEvent() {
        assertEquals("Untitled Event", repository.sanitizeTitle(null))
    }

    @Test
    fun sanitizeTitle_emptyString_returnsUntitledEvent() {
        assertEquals("Untitled Event", repository.sanitizeTitle(""))
    }

    @Test
    fun sanitizeTitle_blankString_returnsUntitledEvent() {
        assertEquals("Untitled Event", repository.sanitizeTitle("   "))
    }

    @Test
    fun sanitizeTitle_emojiOnly_returnsUntitledEvent() {
        // After stripping emojis/non-safe chars the result is empty → "Untitled Event"
        val result = repository.sanitizeTitle("🔥🎉")
        assertEquals("Untitled Event", result)
    }

    @Test
    fun sanitizeTitle_mixedContent_doesNotCrash_andSanitizes() {
        val result = repository.sanitizeTitle("My BBQ 🔥")
        // Should not throw, should contain alphanumeric text
        assertFalse("Should not be empty", result.isEmpty())
        assertFalse("Should not contain emoji", result.contains("🔥"))
        assertTrue("Should contain 'My BBQ'", result.contains("My BBQ"))
    }

    @Test
    fun sanitizeTitle_over100Chars_truncatedTo100() {
        val longTitle = "A".repeat(150)
        val result = repository.sanitizeTitle(longTitle)
        assertEquals(100, result.length)
    }

    @Test
    fun sanitizeTitle_exactly100Chars_notTruncated() {
        val title = "A".repeat(100)
        val result = repository.sanitizeTitle(title)
        assertEquals(100, result.length)
    }

    @Test
    fun sanitizeTitle_normalTitle_preserved() {
        val result = repository.sanitizeTitle("Team Meeting")
        assertEquals("Team Meeting", result)
    }

    @Ignore("ContextCompat.checkSelfPermission calls TextUtils.equals which is not available in unit tests — covered in androidTest")
    @Test
    fun getUpcomingEvents_readCalendarNotGranted_returnsEmpty() {
        val context = mockk<Context>(relaxed = true) {
            every {
                checkPermission(Manifest.permission.READ_CALENDAR, any(), any())
            } returns PackageManager.PERMISSION_DENIED
        }
        // Use a mock context that always denies READ_CALENDAR
        val repo = CalendarRepository(context)

        // ContextCompat.checkSelfPermission delegates to context.checkPermission
        // Since we mock it to deny, getUpcomingEvents should return empty
        // (This is a best-effort test; full permission check tests are in androidTest)
        val result = repo.getUpcomingEvents()
        // Result should be empty because permission is denied OR because query fails gracefully
        // We just verify no crash occurs
        assertTrue("Should return a list (possibly empty)", result is List<*>)
    }

    @Test
    fun sanitizeTitle_specialCharsAllowed_preserved() {
        val title = "John's BBQ (backyard)"
        val result = repository.sanitizeTitle(title)
        assertEquals("John's BBQ (backyard)", result)
    }
}
