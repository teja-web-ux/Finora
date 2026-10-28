package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.US)
    private val displayMonthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
    private val friendlyFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)

    fun currentDateIso(): String {
        return isoFormat.format(Date())
    }

    fun currentMonthString(): String {
        return monthFormat.format(Date())
    }

    fun formatMonthDisplay(monthStr: String): String {
        return try {
            val date = monthFormat.parse(monthStr)
            if (date != null) displayMonthFormat.format(date) else monthStr
        } catch (_: Exception) {
            monthStr
        }
    }

    fun adjustMonth(currentMonth: String, delta: Int): String {
        return try {
            val date = monthFormat.parse(currentMonth) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = date
                add(Calendar.MONTH, delta)
            }
            monthFormat.format(cal.time)
        } catch (_: Exception) {
            currentMonthString()
        }
    }

    fun formatFriendlyDate(dateStr: String): String {
        return try {
            val date = isoFormat.parse(dateStr) ?: return dateStr
            val calToday = Calendar.getInstance()
            val calDate = Calendar.getInstance().apply { time = date }

            if (calToday.get(Calendar.YEAR) == calDate.get(Calendar.YEAR) &&
                calToday.get(Calendar.DAY_OF_YEAR) == calDate.get(Calendar.DAY_OF_YEAR)
            ) {
                "Today"
            } else {
                calToday.add(Calendar.DAY_OF_YEAR, -1)
                if (calToday.get(Calendar.YEAR) == calDate.get(Calendar.YEAR) &&
                    calToday.get(Calendar.DAY_OF_YEAR) == calDate.get(Calendar.DAY_OF_YEAR)
                ) {
                    "Yesterday"
                } else {
                    friendlyFormat.format(date)
                }
            }
        } catch (_: Exception) {
            dateStr
        }
    }
}
