package com.meloda.fast.util

import android.content.Context
import com.conena.nanokt.jvm.util.dayOfMonth
import com.conena.nanokt.jvm.util.hour
import com.conena.nanokt.jvm.util.hourOfDay
import com.conena.nanokt.jvm.util.millisecond
import com.conena.nanokt.jvm.util.minute
import com.conena.nanokt.jvm.util.month
import com.conena.nanokt.jvm.util.second
import com.conena.nanokt.jvm.util.year
import com.meloda.fast.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeUtils {

    val OneDayInSeconds get() = TimeUnit.DAYS.toSeconds(1)

    fun removeTime(date: Date): Long {
        return Calendar.getInstance().apply {
            time = date
            hourOfDay = 0
            minute = 0
            second = 0
            millisecond = 0
        }.timeInMillis
    }

    fun getLocalizedDate(context: Context, date: Long): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().also { it.timeInMillis = date }

        val pattern = when {
            now.year != then.year -> "dd MMM yyyy"
            now.month != then.month -> "dd MMMM"
            now.dayOfMonth != then.dayOfMonth -> {
                if (now.dayOfMonth - then.dayOfMonth == 1) {
                    return context.getString(R.string.yesterday)
                } else {
                    "dd MMMM"
                }
            }

            else -> return context.getString(R.string.today)
        }

        return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    }

    fun getLocalizedTime(context: Context, date: Long): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().also { it.timeInMillis = date }

        return when {
            now.year != then.year -> {
                "${now.year - then.year}${context.getString(R.string.year_short).lowercase()}"
            }

            now.month != then.month -> {
                "${now.month - then.month}${context.getString(R.string.month_short).lowercase()}"
            }

            now.dayOfMonth != then.dayOfMonth -> {
                val change = now.dayOfMonth - then.dayOfMonth

                if (change % 7 == 0) {
                    "${change / 7}${context.getString(R.string.week_short).lowercase()}"
                } else {
                    "$change${context.getString(R.string.day_short).lowercase()}"
                }
            }

            now.hour == then.hour && now.minute == then.minute -> {
                context.getString(R.string.time_now).lowercase()
            }

            else -> {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            }
        }
    }
}
