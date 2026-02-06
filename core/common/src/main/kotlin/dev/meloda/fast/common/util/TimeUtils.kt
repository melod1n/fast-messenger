package dev.meloda.fast.common.util

import com.conena.nanokt.jvm.util.dayOfMonth
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
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

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
        minuteShort: () -> String,
        secondShort: () -> String,
        now: () -> String
    ): String {
        val now = Clock.System.now()
        val then = Instant.fromEpochMilliseconds(date)
        val diff = now - then

        return when {
            diff > 365.days -> "${diff.inWholeDays / 365}${yearShort().lowercase()}"
            diff > 30.days -> "${diff.inWholeDays / 30}${monthShort().lowercase()}"
            diff > 7.days -> "${diff.inWholeDays / 7}${weekShort().lowercase()}"
            diff > 1.days -> "${diff.inWholeDays}${dayShort().lowercase()}"
            diff > 1.hours -> "${diff.inWholeHours}h"
            diff > 1.minutes -> "${diff.inWholeMinutes}${minuteShort().lowercase()}"
            diff > 1.seconds -> "${diff.inWholeSeconds}${secondShort().lowercase()}"
            else -> now().lowercase()
        }
    }
}
