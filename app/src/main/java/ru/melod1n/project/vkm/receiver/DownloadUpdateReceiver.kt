package ru.melod1n.project.vkm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.melod1n.project.vkm.listener.OnResponseListener

open class DownloadUpdateReceiver : BroadcastReceiver() {

    var listener: OnResponseListener<Any?>? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        listener?.onResponse(null)
    }


}