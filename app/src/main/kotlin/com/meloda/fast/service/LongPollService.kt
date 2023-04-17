package com.meloda.fast.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.meloda.fast.R
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.base.ApiError
import com.meloda.fast.api.longpoll.LongPollUpdatesParser
import com.meloda.fast.api.model.base.BaseVkLongPoll
import com.meloda.fast.api.network.ApiAnswer
import com.meloda.fast.api.network.longpoll.LongPollGetUpdatesRequest
import com.meloda.fast.api.network.messages.MessagesGetLongPollServerRequest
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.data.messages.MessagesRepository
import com.meloda.fast.ext.isTrue
import com.meloda.fast.receiver.StopLongPollServiceReceiver
import com.meloda.fast.util.NotificationsUtils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class LongPollService : Service() {

    companion object {
        const val TAG = "LongPollTask"

        const val KeyLongPollWasDestroyed = "long_poll_was_destroyed"

        private const val NOTIFICATION_ID = 1001
    }

    private val job = SupervisorJob()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d(TAG, "error: $throwable")
        throwable.printStackTrace()
    }

    private val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job + exceptionHandler

    private val coroutineScope = CoroutineScope(coroutineContext)

    private val repository: MessagesRepository by inject()

    private val updatesParser: LongPollUpdatesParser by inject()

    private var asForeground = true
    private var foregroundNotification: Notification? = null

    override fun onCreate() {
        super.onCreate()

        val notificationBuilder =
            NotificationsUtils.createNotification(
                context = this,
                title = "LongPoll",
                contentText = "обновление ваших сообщений в фоне",
                notRemovable = false,
                channelId = "long_polling",
                priority = NotificationsUtils.NotificationPriority.Low,
                category = NotificationCompat.CATEGORY_SERVICE,
                customNotificationId = NOTIFICATION_ID
            )

        foregroundNotification = notificationBuilder.build()
        startForeground(NOTIFICATION_ID, foregroundNotification)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        asForeground = intent?.getBooleanExtra("foreground", false).isTrue

        Log.d(
            "LongPollService",
            "onStartCommand: asForeground: $asForeground; flags: $flags; startId: $startId"
        )

        coroutineScope.launch { startPolling().join() }

        val stopIntent = Intent(this, StopLongPollServiceReceiver::class.java).apply {
            action = StopLongPollServiceReceiver.ACTION_STOP
            putExtra(StopLongPollServiceReceiver.NOTIFICATION_ID, startId)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val action = NotificationCompat.Action(
            R.drawable.ic_round_close_24,
            getString(R.string.action_stop),
            stopPendingIntent
        )

        if (asForeground) {
            val notificationBuilder =
                NotificationsUtils.createNotification(
                    context = this,
                    title = "LongPoll",
                    contentText = "обновление ваших сообщений в фоне",
                    notRemovable = false,
                    channelId = "long_polling",
                    priority = NotificationsUtils.NotificationPriority.Low,
                    category = NotificationCompat.CATEGORY_SERVICE,
                    actions = listOf(action),
                    customNotificationId = NOTIFICATION_ID
                )

            foregroundNotification = notificationBuilder.build()

            startForeground(NOTIFICATION_ID, foregroundNotification)
        } else {
            if (foregroundNotification != null) {
                NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
                foregroundNotification = null
            }
        }
        return START_STICKY
    }

    private fun startPolling(): Job {
        if (job.isCompleted || job.isCancelled) {
            Log.d("LongPollService", "job is completed or cancelled. Fuck off")
            throw Exception("Job is over")
        }

        Log.d("LongPollService", "job started")

        return coroutineScope.launch {
            var serverInfo = getServerInfo()
                ?: throw ApiError(errorMessage = "bad VK response (server info)")

            var lastUpdatesResponse: JsonObject? = getUpdatesResponse(serverInfo)
                ?: throw ApiError(errorMessage = "initiation error: bad VK response (last updates)")

            var failCount = 0

            while (job.isActive) {
                if (lastUpdatesResponse == null) {
                    failCount++
                    serverInfo = getServerInfo()
                        ?: throw ApiError(errorMessage = "failed retrieving server info after error: bad VK response (server info #2)")
                    lastUpdatesResponse = getUpdatesResponse(serverInfo)
                    continue
                }

                when (lastUpdatesResponse["failed"]?.asInt) {
                    1 -> {
                        var newTs = lastUpdatesResponse["ts"]?.asInt
                        if (newTs == null) {
                            newTs = serverInfo.ts
                            failCount++
                        }

                        lastUpdatesResponse = getUpdatesResponse(serverInfo.copy(ts = newTs))
                    }

                    2, 3 -> {
                        serverInfo = getServerInfo()
                            ?: throw ApiError(
                                errorMessage = "failed retrieving server info after error: bad VK response (server info #3)"
                            )
                        lastUpdatesResponse = getUpdatesResponse(serverInfo)
                    }

                    else -> {
                        val newTs = lastUpdatesResponse["ts"]?.asInt

                        if (newTs == null) {
                            failCount++
                        } else {
                            val updates = lastUpdatesResponse["updates"]?.asJsonArray

                            if (updates == null) {
                                failCount++
                            } else {
                                updates.forEach { item ->
                                    item.asJsonArray?.also {
                                        launch {
                                            handleUpdateEvent(it)
                                        }
                                    } ?: failCount++
                                }
                            }

                            lastUpdatesResponse = getUpdatesResponse(serverInfo.copy(ts = newTs))
                        }
                    }
                }
            }
        }
    }

    private suspend fun getServerInfo(): BaseVkLongPoll? {
        val response = repository.getLongPollServer(
            MessagesGetLongPollServerRequest(
                needPts = true,
                version = VKConstants.LP_VERSION
            )
        )

        println("$TAG: serverInfoResponse: $response")

        if (response is ApiAnswer.Error) return null
        if (response is ApiAnswer.Success) {
            return response.data.response
        }

        return null
    }

    private suspend fun getUpdatesResponse(server: BaseVkLongPoll): JsonObject? {
        val response = repository.getLongPollUpdates(
            serverUrl = "https://${server.server}",
            params = LongPollGetUpdatesRequest(
                key = server.key,
                ts = server.ts,
                wait = 25,
                mode = 2 or 8 or 32 or 64 or 128,
                version = VKConstants.LP_VERSION
            )
        )

        println("$TAG: lastUpdateResponse: $response")

        if (response is ApiAnswer.Success) {
            return response.data
        }

        return null
    }

    private fun handleUpdateEvent(eventJson: JsonArray) {
        updatesParser.parseNextUpdate(eventJson)
    }

    override fun onDestroy() {
        Log.d("LongPollService", "onDestroy")
        try {
            AppGlobal.preferences.edit {
                putBoolean(KeyLongPollWasDestroyed, true)
            }
            job.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    override fun onLowMemory() {
        Log.d("LongPollService", "onLowMemory")
        super.onLowMemory()
    }
}
