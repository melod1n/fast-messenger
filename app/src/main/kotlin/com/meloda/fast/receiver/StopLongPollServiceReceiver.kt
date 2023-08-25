package com.meloda.fast.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.screens.main.activity.LongPollState
import com.meloda.fast.screens.main.activity.MainActivity
import com.meloda.fast.screens.settings.presentation.SettingsFragment
import kotlinx.coroutines.flow.update

class StopLongPollServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_STOP) {
            val notificationId = intent.getIntExtra(NOTIFICATION_ID, -1)

            if (notificationId != -1) {
                NotificationManagerCompat.from(context).cancel(notificationId)
            }

            AppGlobal.preferences.edit {
                putBoolean(SettingsFragment.KEY_FEATURES_LONG_POLL_IN_BACKGROUND, false)
            }

            MainActivity.longPollState.update { LongPollState.Stop }
            MainActivity.longPollState.update { LongPollState.DefaultService }
        }
    }

    companion object {
        const val ACTION_STOP = "stop_long_poll"
        const val NOTIFICATION_ID = "notification_id"
    }
}
