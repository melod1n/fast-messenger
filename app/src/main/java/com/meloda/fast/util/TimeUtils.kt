package com.meloda.fast.util

import java.util.*

object TimeUtils {

    fun removeTime(date: Date): Long {
        return Calendar.getInstance().apply {
            time = date
            this[Calendar.HOUR_OF_DAY] = 0
            this[Calendar.MINUTE] = 0
            this[Calendar.SECOND] = 0
            this[Calendar.MILLISECOND] = 0
        }.timeInMillis
    }

}