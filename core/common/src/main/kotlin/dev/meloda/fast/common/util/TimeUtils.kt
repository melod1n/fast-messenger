package dev.meloda.fast.common.util

import com.conena.nanokt.jvm.util.dayOfMonth
import com.conena.nanokt.jvm.util.hour
import com.conena.nanokt.jvm.util.hourOfDay
import com.conena.nanokt.jvm.util.millisecond
import com.conena.nanokt.jvm.util.minute
import com.conena.nanokt.jvm.util.month
import com.conena.nanokt.jvm.util.second
import com.conena.nanokt.jvm.util.year
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeUtils {

    fun removeTime(date: Date): Long {
        return Calendar.getInstance().apply {
            time = date
            hourOfDay = 0
            minute = 0
            second = 0
            millisecond = 0
        }.timeInMillis
    }

    fun getLocalizedDate(
        date: Long,
        yesterday: () -> String,
        today: () -> String
    ): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().also { it.timeInMillis = date }

        val pattern = when {
            now.year != then.year -> "dd MMM yyyy"
            now.month != then.month -> "dd MMMM"
            now.dayOfMonth != then.dayOfMonth -> {
                if (now.dayOfMonth - then.dayOfMonth == 1) {
                    return yesterday()
                } else {
                    "dd MMMM"
                }
            }

            else -> return today()
        }

        return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    }

    fun getLocalizedTime(
        date: Long,
        yearShort: () -> String,
        monthShort: () -> String,
        weekShort: () -> String,
        dayShort: () -> String,
        now: () -> String
    ): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().also { it.timeInMillis = date }

        return when {
            now.year != then.year -> {
                "${now.year - then.year}${yearShort().lowercase()}"
            }

            now.month != then.month -> {
                "${now.month - then.month}${monthShort().lowercase()}"
            }

            now.dayOfMonth != then.dayOfMonth -> {
                val change = now.dayOfMonth - then.dayOfMonth

                if (change % 7 == 0) {
                    "${change / 7}${weekShort().lowercase()}"
                } else {
                    "$change${dayShort().lowercase()}"
                }
            }

            now.hour == then.hour && now.minute == then.minute -> {
                now().lowercase()
            }

            else -> {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            }
        }
    }
}
