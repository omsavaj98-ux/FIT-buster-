package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FitBusterDateUtil {
    private val keyFormat: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val monthFormat: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM", Locale.US)

    private val displayFormat: SimpleDateFormat
        get() = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    private val shortDayFormat: SimpleDateFormat
        get() = SimpleDateFormat("EEE", Locale.US)

    fun todayKey(): String = keyFormat.format(Date())

    fun monthKey(dateKey: String = todayKey()): String =
        if (dateKey.length >= 7) dateKey.substring(0, 7) else monthFormat.format(Date())

    fun dateKeyDaysAgo(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return keyFormat.format(cal.time)
    }

    fun lastNDaysKeys(days: Int): List<String> =
        (days - 1 downTo 0).map { dateKeyDaysAgo(it) }

    fun shortDayLabel(dateKey: String): String {
        return try {
            val parsed = keyFormat.parse(dateKey) ?: return dateKey.takeLast(2)
            shortDayFormat.format(parsed)
        } catch (_: Exception) {
            dateKey.takeLast(2)
        }
    }

    fun formatDisplayDate(dateKey: String): String {
        return try {
            val parsed = keyFormat.parse(dateKey) ?: return dateKey
            displayFormat.format(parsed)
        } catch (_: Exception) {
            dateKey
        }
    }

    fun weekStartKey(): String {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return keyFormat.format(cal.time)
    }

    fun weekEndKey(): String {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.add(Calendar.DAY_OF_YEAR, 6)
        return keyFormat.format(cal.time)
    }

    fun monthEndKey(): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        return keyFormat.format(cal.time)
    }

    fun formatDurationHms(totalSeconds: Long): String {
        val hrs = totalSeconds / 3600
        val mins = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60
        return if (hrs > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format(Locale.US, "%02d:%02d", mins, secs)
        }
    }

    fun formatSprintMillis(millis: Long): String {
        val secs = millis / 1000.0
        return String.format(Locale.US, "%.2fs", secs)
    }

    fun formatPace(secPerKm: Double): String {
        if (secPerKm <= 0.0 || secPerKm > 3600.0) return "--:-- /km"
        val mins = (secPerKm / 60.0).toInt()
        val secs = (secPerKm % 60.0).toInt()
        return String.format(Locale.US, "%d:%02d /km", mins, secs)
    }
}
