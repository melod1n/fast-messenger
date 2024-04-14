package com.meloda.fast.service.longpolling

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.model.data.VkLongPollData
import com.meloda.fast.base.processState
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.ext.listenValue
import com.meloda.fast.screens.settings.SettingsKeys
import com.meloda.fast.service.longpolling.data.LongPollUpdates
import com.meloda.fast.service.longpolling.domain.usecase.LongPollUseCase
import com.meloda.fast.util.NotificationsUtils
import com.slack.eithernet.ApiException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LongPollingService : Service() {

    private val job = SupervisorJob()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "error: $throwable")
        throwable.printStackTrace()
    }

    private val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job + exceptionHandler

    private val coroutineScope = CoroutineScope(coroutineContext)

    private val longPollUseCase: LongPollUseCase by inject()
    private val updatesParser: LongPollUpdatesParser by inject()

    private var currentJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(STATE_TAG, "onCreate()")
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(STATE_TAG, "onBind: intent: $intent")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (startId > 1) return START_STICKY

        val asForeground = AppGlobal.preferences.getBoolean(
            SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
            SettingsKeys.DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND
        )

        Log.d(
            STATE_TAG,
            "onStartCommand: asForeground: $asForeground; flags: $flags; startId: $startId;\ninstance: $this"
        )

        if (currentJob != null) {
            currentJob?.cancel()
            currentJob = null
        }

        coroutineScope.launch {
            currentJob = startPolling().also { it.join() }
        }

        val openCategorySettingsIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                .putExtra(Settings.EXTRA_CHANNEL_ID, "long_polling")
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val openCategorySettingsPendingIntent = PendingIntent.getActivity(
            this,
            1,
            openCategorySettingsIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        if (asForeground) {
            val notification =
                NotificationsUtils.createNotification(
                    context = this,
                    title = "LongPoll",
                    contentText = "нажмите, чтобы убрать уведомление",
                    notRemovable = false,
                    channelId = "long_polling",
                    priority = NotificationsUtils.NotificationPriority.Low,
                    category = NotificationCompat.CATEGORY_SERVICE,
                    customNotificationId = NOTIFICATION_ID,
                    contentIntent = openCategorySettingsPendingIntent
                ).build()

            startForeground(NOTIFICATION_ID, notification)
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
        return START_STICKY
    }

    private fun startPolling(): Job {
        if (job.isCompleted || job.isCancelled) {
            Log.d(STATE_TAG, "job is completed or cancelled")
            throw Exception("Job is over")
        }

        Log.d(STATE_TAG, "job started")

        return coroutineScope.launch {
            if (UserConfig.accessToken.isEmpty()) {
                throw NoAccessTokenException
            }

            var serverInfo = getServerInfo()
                ?: throw ApiException(error = "bad VK response (server info)")

            var lastUpdatesResponse: LongPollUpdates? = getUpdatesResponse(serverInfo)
                ?: throw ApiException(error = "initiation error: bad VK response (last updates)")

            var failCount = 0

            while (job.isActive) {
                if (lastUpdatesResponse == null) {
                    failCount++
                    serverInfo = getServerInfo()
                        ?: throw ApiException(error = "failed retrieving server info after error: bad VK response (server info #2)")
                    lastUpdatesResponse = getUpdatesResponse(serverInfo)
                    continue
                }

                when (lastUpdatesResponse.failed) {
                    1 -> {
                        val newTs = lastUpdatesResponse.ts ?: kotlin.run {
                            failCount++
                            serverInfo.ts
                        }

                        lastUpdatesResponse = getUpdatesResponse(serverInfo.copy(ts = newTs))
                    }

                    2, 3 -> {
                        serverInfo = getServerInfo()
                            ?: throw ApiException(
                                error = "failed retrieving server info after error: bad VK response (server info #3)"
                            )
                        lastUpdatesResponse = getUpdatesResponse(serverInfo)
                    }

                    else -> {
                        val newTs = lastUpdatesResponse.ts

                        if (newTs == null) {
                            failCount++
                        } else {
                            val updates = lastUpdatesResponse.updates

                            if (updates == null) {
                                failCount++
                            } else {
                                updates.forEach { item ->
                                    handleUpdateEvent(item)
                                }
                            }

                            lastUpdatesResponse = getUpdatesResponse(serverInfo.copy(ts = newTs))
                        }
                    }
                }
            }
        }
    }

    private suspend fun getServerInfo(): VkLongPollData? = suspendCoroutine {
        longPollUseCase.getLongPollServer(
            needPts = true,
            version = VKConstants.LP_VERSION
        ).listenValue(coroutineScope) { state ->
            state.processState(
                success = { response ->
                    Log.d(TAG, "getServerInfo: serverInfoResponse: $response")
                    it.resume(response)
                },
                error = { error ->
                    Log.e(TAG, "getServerInfo: $error")
                    it.resume(null)
                }
            )
        }
    }

    private suspend fun getUpdatesResponse(
        server: VkLongPollData
    ): LongPollUpdates? = suspendCoroutine {
        longPollUseCase.getLongPollUpdates(
            serverUrl = "https://${server.server}",
            key = server.key,
            ts = server.ts,
            wait = 25,
            mode = 2 or 8 or 32 or 64 or 128,
            version = VKConstants.LP_VERSION
        ).listenValue(coroutineScope) { state ->
            state.processState(
                success = { response ->
                    Log.d(TAG, "lastUpdateResponse: $response")
                    it.resume(response)
                },
                error = { error ->
                    Log.d(TAG, "getUpdatesResponse: error: $error")
                    it.resume(null)
                }
            )
        }
    }

    private fun handleUpdateEvent(event: List<Any>) {
        updatesParser.parseNextUpdate(event)
    }

    override fun onDestroy() {
        Log.d(STATE_TAG, "onDestroy")
        try {
            AppGlobal.preferences.edit {
                putBoolean(KEY_LONG_POLL_WAS_DESTROYED, true)
            }
            job.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    override fun onLowMemory() {
        Log.d(STATE_TAG, "onLowMemory")
        super.onLowMemory()
    }

    private object NoAccessTokenException : Exception()

    companion object {
        const val TAG = "LongPollTask"

        private const val STATE_TAG = "LongPollServiceState"

        const val KEY_LONG_POLL_WAS_DESTROYED = "long_poll_was_destroyed"

        private const val NOTIFICATION_ID = 1001
    }
}
