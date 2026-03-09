package com.weatherapp.data.calendar

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Returns upcoming calendar events within the next [daysAhead] days.
     * Returns an empty list if READ_CALENDAR permission is not granted or any error occurs.
     */
    fun getUpcomingEvents(daysAhead: Int = 7): List<CalendarEvent> {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Timber.w("CalendarRepository: READ_CALENDAR permission not granted — returning empty list")
            return emptyList()
        }

        return try {
            queryEvents(daysAhead)
        } catch (e: SecurityException) {
            Timber.w(e, "CalendarRepository: SecurityException while querying calendar")
            emptyList()
        } catch (e: Exception) {
            Timber.e(e, "CalendarRepository: unexpected error while querying calendar")
            emptyList()
        }
    }

    private fun queryEvents(daysAhead: Int): List<CalendarEvent> {
        val nowMs = System.currentTimeMillis()
        val endMs = nowMs + daysAhead * 86400L * 1000L

        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION
        )

        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
        val selectionArgs = arrayOf(nowMs.toString(), endMs.toString())
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

        val events = mutableListOf<CalendarEvent>()

        val cursor = context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        ) ?: return emptyList()

        cursor.use { c ->
            val idIdx       = c.getColumnIndexOrThrow(CalendarContract.Events._ID)
            val titleIdx    = c.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
            val startIdx    = c.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)
            val endIdx      = c.getColumnIndexOrThrow(CalendarContract.Events.DTEND)
            val locationIdx = c.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION)

            while (c.moveToNext()) {
                val eventId   = c.getLong(idIdx).toString()
                val rawTitle  = c.getString(titleIdx)
                val startMs   = c.getLong(startIdx)
                val endMs2    = c.getLong(endIdx)
                val location  = c.getString(locationIdx)?.takeIf { it.isNotBlank() }

                events.add(
                    CalendarEvent(
                        eventId    = eventId,
                        title      = sanitizeTitle(rawTitle),
                        startEpoch = startMs / 1000L,
                        endEpoch   = endMs2 / 1000L,
                        location   = location
                    )
                )
            }
        }

        Timber.d("CalendarRepository: queried ${events.size} events in next $daysAhead days")
        return events
    }

    internal fun sanitizeTitle(raw: String?): String {
        if (raw.isNullOrBlank()) return "Untitled Event"
        val safe = raw.filter { c ->
            c.isLetterOrDigit() || c == ' ' || c == '\'' || c == '-' || c == ',' || c == '.' || c == '!' || c == '?' || c == '(' || c == ')'
        }.trim()
        val result = if (safe.isEmpty()) "Untitled Event" else safe
        return result.take(100)
    }
}
