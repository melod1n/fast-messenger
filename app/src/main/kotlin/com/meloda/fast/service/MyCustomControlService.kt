@file:RequiresApi(Build.VERSION_CODES.R)

package com.meloda.fast.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.service.controls.Control
import android.service.controls.ControlsProviderService
import android.service.controls.DeviceTypes
import android.service.controls.actions.ControlAction
import androidx.annotation.RequiresApi
import com.meloda.fast.screens.main.activity.MainActivity
import kotlinx.coroutines.jdk9.flowPublish
import java.util.concurrent.Flow
import java.util.function.Consumer

private const val LIGHT_ID = 1234
private const val LIGHT_TITLE = "Enable Long Polling"
private const val LIGHT_TYPE = DeviceTypes.TYPE_DOOR


class MyCustomControlService : ControlsProviderService() {

    override fun createPublisherForAllAvailable() =
        flowPublish {
            send(createStatelessControl(LIGHT_ID, LIGHT_TITLE, LIGHT_TYPE))
        }

    private fun createStatelessControl(id: Int, title: String, type: Int): Control {
        val intent = Intent(this, MainActivity::class.java)
            .putExtra(EXTRA_MESSAGE, title)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val action = PendingIntent.getActivity(
            this,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Control.StatelessBuilder(id.toString(), action)
            .setTitle(title)
            .setDeviceType(type)
            .build()
    }

    override fun createPublisherFor(controlIds: MutableList<String>): Flow.Publisher<Control> {
        TODO("Not yet implemented")
    }

    override fun performControlAction(
        controlId: String,
        action: ControlAction,
        consumer: Consumer<Int>
    ) {

    }
}
