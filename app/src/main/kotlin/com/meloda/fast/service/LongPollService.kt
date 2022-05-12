package com.meloda.fast.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.content.edit
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.VKException
import com.meloda.fast.api.longpoll.LongPollUpdatesParser
import com.meloda.fast.api.model.base.BaseVkLongPoll
import com.meloda.fast.api.network.Answer
import com.meloda.fast.api.network.longpoll.LongPollGetUpdatesRequest
import com.meloda.fast.api.network.longpoll.LongPollRepo
import com.meloda.fast.api.network.messages.MessagesDataSource
import com.meloda.fast.api.network.messages.MessagesGetLongPollServerRequest
import com.meloda.fast.common.AppGlobal
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class LongPollService : Service(), CoroutineScope {

    companion object {
        const val TAG = "LongPollTask"

        const val KeyLongPollWasDestroyed = "long_poll_was_destroyed"
    }

    private val job = SupervisorJob()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d(TAG, "error: $throwable")
        throwable.printStackTrace()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job + exceptionHandler

    @Inject
    lateinit var dataSource: MessagesDataSource

    @Inject
    lateinit var longPollRepo: LongPollRepo

    @Inject
    lateinit var updatesParser: LongPollUpdatesParser

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LongPollService", "onStartCommand: flags: $flags; startId: $startId")
        launch { startPolling().join() }
        return START_STICKY
    }

    private fun startPolling(): Job {
        if (job.isCompleted || job.isCancelled) {
            Log.d("LongPollService", "job is completed or cancelled. Fuck off")
            throw Exception("Job is over")
        }

        Log.d("LongPollService", "job started")

        return launch {
            var serverInfo = getServerInfo()
                ?: throw VKException(error = "bad VK response (server info)")

            var lastUpdatesResponse: JsonObject? = getUpdatesResponse(serverInfo)
                ?: throw VKException(error = "initiation error: bad VK response (last updates)")

            var failCount = 0

            while (job.isActive) {
                if (lastUpdatesResponse == null) {
                    failCount++
                    serverInfo = getServerInfo()
                        ?: throw VKException(error = "failed retrieving server info after error: bad VK response (server info #2)")
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
                            ?: throw VKException(
                                error = "failed retrieving server info after error: bad VK response (server info #3)"
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
        val response = dataSource.getLongPollServer(
            MessagesGetLongPollServerRequest(
                needPts = true,
                version = VKConstants.LP_VERSION
            )
        )

        println("$TAG: serverInfoResponse: $response")

        if (response is Answer.Error) return null
        if (response is Answer.Success) {
            return response.data.response
        }

        return null
    }

    private suspend fun getUpdatesResponse(server: BaseVkLongPoll): JsonObject? {
        val response = dataSource.getLongPollUpdates(
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

        if (response is Answer.Error) return null

        if (response is Answer.Success) {
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
}