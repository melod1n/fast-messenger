package com.meloda.fast.util

import android.content.Context
import com.meloda.fast.R
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    const val ONE_DAY_IN_SECONDS = 86400

    fun removeTime(date: Date): Long {
        return Calendar.getInstance().apply {
            time = date
            this[Calendar.HOUR_OF_DAY] = 0
            this[Calendar.MINUTE] = 0
            this[Calendar.SECOND] = 0
            this[Calendar.MILLISECOND] = 0
        }.timeInMillis
    }

    fun getLocalizedDate(context: Context, date: Long): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().also { it.timeInMillis = date }

        val pattern =
            if (now[Calendar.YEAR] != then[Calendar.YEAR]) {
                "dd MMM yyyy"
            } else if (now[Calendar.MONTH] != then[Calendar.MONTH]) {
                "dd MMMM"
            } else if (now[Calendar.DAY_OF_MONTH] != then[Calendar.DAY_OF_MONTH]) {
                if (now[Calendar.DAY_OF_MONTH] - then[Calendar.DAY_OF_MONTH] == 1) {
                    return context.getString(R.string.yesterday)
                } else {
                    "dd MMMM"
                }
            } else {
                return context.getString(R.string.today)
            }

        return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    }

    fun getLocalizedTime(context: Context, date: Long): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().also { it.timeInMillis = date }

        return when {
            now[Calendar.YEAR] != then[Calendar.YEAR] -> {
                "${now[Calendar.YEAR] - then[Calendar.YEAR]}${
                    context.getString(R.string.year_short).lowercase()
                }"
            }
            now[Calendar.MONTH] != then[Calendar.MONTH] -> {
                "${now[Calendar.MONTH] - then[Calendar.MONTH]}${
                    context.getString(R.string.month_short).lowercase()
                }"
            }
            now[Calendar.DAY_OF_MONTH] != then[Calendar.DAY_OF_MONTH] -> {
                val change = now[Calendar.DAY_OF_MONTH] - then[Calendar.DAY_OF_MONTH]
                if (change >= 7) {
                    "${change / 7}${context.getString(R.string.week_short).lowercase()}"
                } else {
                    "$change${context.getString(R.string.day_short).lowercase()}"
                }
            }
            else -> {
                if (now[Calendar.MINUTE] == then[Calendar.MINUTE]) {
                    context.getString(R.string.time_now).lowercase()
                } else {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                }
            }
        }
    }
}