package com.meloda.fast.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DownloadManagerReceiver : BroadcastReceiver() {

    var onReceiveAction: (() -> Unit)? = null

    override fun onReceive(context: Context, intent: Intent) {
        onReceiveAction?.invoke()
    }
}