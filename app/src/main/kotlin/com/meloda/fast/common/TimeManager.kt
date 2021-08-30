package com.meloda.fast.common

import android.content.Context
import android.content.IntentFilter
import com.meloda.fast.receiver.MinuteReceiver
import java.util.*

object TimeManager {

    var currentHour = 0
    var currentMinute = 0
    var currentSecond = 0

    private val onHourChangeListeners: ArrayList<OnHourChangeListener> = ArrayList()
    private val onMinuteChangeListeners: ArrayList<OnMinuteChangeListener> = ArrayList()
    private val onSecondChangeListeners: ArrayList<OnSecondChangeListener> = ArrayList()
    private val onTimeChangeListeners: ArrayList<OnTimeChangeListener> = ArrayList()

    fun init(context: Context) {
        context.registerReceiver(MinuteReceiver(), IntentFilter("android.intent.action.TIME_TICK"))

        addOnMinuteChangeListener(minuteChangeListener)
    }

    private var minuteChangeListener = object : OnMinuteChangeListener {
        override fun onMinuteChange(currentMinute: Int) {
            TimeManager.currentMinute = currentMinute
        }
    }

    fun destroy() {
        removeOnMinuteChangeListener(minuteChangeListener)
    }

    fun broadcastMinute() {
        for (onMinuteChangeListener in onMinuteChangeListeners) {
            onMinuteChangeListener.onMinuteChange(0)
        }
    }

    val isMorning = currentHour in 7..11

    val isAfternoon = currentHour in 12..16

    val isEvening = currentHour in 17..22

    val isNight = currentHour == 23 || currentHour < 6 && currentHour > -1

    fun addOnHourChangeListener(onHourChangeListeners: OnHourChangeListener) {
        TimeManager.onHourChangeListeners.add(onHourChangeListeners)
    }

    fun removeOnHourChangeListener(onHourChangeListener: OnHourChangeListener?) {
        onHourChangeListeners.remove(onHourChangeListener)
    }

    fun addOnMinuteChangeListener(onMinuteChangeListener: OnMinuteChangeListener) {
        onMinuteChangeListeners.add(onMinuteChangeListener)
    }

    fun removeOnMinuteChangeListener(onMinuteChangeListener: OnMinuteChangeListener?) {
        onMinuteChangeListeners.remove(onMinuteChangeListener)
    }

    fun addOnSecondChangeListener(onSecondChangeListener: OnSecondChangeListener) {
        onSecondChangeListeners.add(onSecondChangeListener)
    }

    fun removeOnSecondChangeListener(onSecondChangeListener: OnSecondChangeListener?) {
        onSecondChangeListeners.remove(onSecondChangeListener)
    }

    fun addOnTimeChangeListener(onTimeChangeListener: OnTimeChangeListener) {
        onTimeChangeListeners.add(onTimeChangeListener)
    }

    fun removeOnTimeChangeListener(onTimeChangeListener: OnTimeChangeListener?) {
        onTimeChangeListeners.remove(onTimeChangeListener)
    }

    interface OnHourChangeListener {
        fun onHourChange(currentHour: Int)
    }

    interface OnMinuteChangeListener {
        fun onMinuteChange(currentMinute: Int)
    }

    interface OnSecondChangeListener {
        fun onSecondChange(currentSecond: Int)
    }

    interface OnTimeChangeListener {
        fun onHourChange(currentHour: Int)
        fun onMinuteChange(currentMinute: Int)
        fun onSecondChange(currentSecond: Int)
    }
}