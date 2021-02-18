package ru.melod1n.project.vkm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.melod1n.project.vkm.common.TimeManager

class MinuteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        TimeManager.broadcastMinute()
    }

}