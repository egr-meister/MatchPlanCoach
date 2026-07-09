package com.matchplan.coach.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Small, defensive date/time helpers. Everything is string based
 * (YYYY-MM-DD for dates, HH:mm for times). No native pickers required.
 * Nothing here throws; invalid input returns safe values / false.
 */
object DateUtils {

    private val DATE_FORMAT = "yyyy-MM-dd"
    private val TIME_FORMAT = "HH:mm"

    private fun dateFormatter() = SimpleDateFormat(DATE_FORMAT, Locale.US).apply {
        isLenient = false
    }

    /** Device-local today as YYYY-MM-DD. */
    fun today(): String = SimpleDateFormat(DATE_FORMAT, Locale.US).format(Date())

    /** today + [days] as YYYY-MM-DD (used for the default 10-day window). */
    fun todayPlusDays(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return SimpleDateFormat(DATE_FORMAT, Locale.US).format(cal.time)
    }

    /** Current timestamp in ISO-ish form for createdAt / updatedAt. */
    fun nowTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())

    fun isValidDate(value: String): Boolean {
        if (value.isBlank()) return false
        return try {
            dateFormatter().parse(value)
            // Extra guard: enforce exact YYYY-MM-DD shape.
            Regex("""\d{4}-\d{2}-\d{2}""").matches(value)
        } catch (e: Exception) {
            false
        }
    }

    fun isValidTime(value: String): Boolean {
        if (value.isBlank()) return true // time is optional
        return Regex("""^([01]\d|2[0-3]):[0-5]\d$""").matches(value)
    }

    /** dateTo must not be earlier than dateFrom (both assumed valid). */
    fun isRangeOrdered(dateFrom: String, dateTo: String): Boolean {
        return try {
            val f = dateFormatter().parse(dateFrom) ?: return false
            val t = dateFormatter().parse(dateTo) ?: return false
            !t.before(f)
        } catch (e: Exception) {
            false
        }
    }

    /** Pretty date for cards, e.g. "Thu, 09 Jul 2026". Falls back to raw text. */
    fun prettyDate(value: String): String {
        return try {
            val d = dateFormatter().parse(value) ?: return value
            SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).format(d)
        } catch (e: Exception) {
            value.ifBlank { "No date" }
        }
    }

    /** Extract YYYY-MM-DD from a UTC ISO instant. Safe on bad input. */
    fun dateFromUtc(utc: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val d = parser.parse(utc) ?: return ""
            val local = SimpleDateFormat(DATE_FORMAT, Locale.US)
            local.format(d)
        } catch (e: Exception) {
            if (utc.length >= 10) utc.substring(0, 10) else ""
        }
    }

    /** Extract HH:mm (device local) from a UTC ISO instant. Safe on bad input. */
    fun timeFromUtc(utc: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val d = parser.parse(utc) ?: return ""
            SimpleDateFormat(TIME_FORMAT, Locale.US).format(d)
        } catch (e: Exception) {
            ""
        }
    }
}
