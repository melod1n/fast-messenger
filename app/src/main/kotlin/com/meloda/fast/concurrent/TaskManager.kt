package com.meloda.fast.concurrent

object TaskManager {

    private const val TAG = "TaskManager"

    private val listeners = arrayListOf<OnEventListener>()

    fun addOnEventListener(listener: OnEventListener) {
        listeners.add(listener)
    }

    fun removeOnEventListener(listener: OnEventListener?) {
        listeners.remove(listener)
    }

    fun execute(runnable: Runnable?) {
        LowThread(runnable).start()
    }

    fun sendEvent(eventInfo: EventInfo<*>) {
        for (listener in listeners) {
            listener.onNewEvent(eventInfo)
        }
    }

    interface OnEventListener {
        fun onNewEvent(info: EventInfo<*>)
    }
}