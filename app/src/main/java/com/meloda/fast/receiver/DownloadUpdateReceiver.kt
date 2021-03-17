package com.meloda.fast.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.meloda.vksdk.OnResponseListener

open class DownloadUpdateReceiver : BroadcastReceiver() {

    var listener: OnResponseListener<Any?>? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        listener?.onResponse(null)
    }


}