package com.meloda.fast.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.meloda.fast.common.TimeManager

class MinuteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        TimeManager.broadcastMinute()
    }

}