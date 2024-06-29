package com.meloda.app.fast.service.longpolling

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.conena.nanokt.android.app.stopForegroundCompat
import com.meloda.app.fast.common.UserConfig
import com.meloda.app.fast.common.VkConstants
import com.meloda.app.fast.common.extensions.listenValue
import com.meloda.app.fast.data.processState
import com.meloda.app.fast.datastore.SettingsController
import com.meloda.app.fast.datastore.SettingsKeys
import com.meloda.app.fast.model.api.data.LongPollUpdates
import com.meloda.app.fast.model.api.data.VkLongPollData
import com.meloda.app.fast.util.NotificationsUtils
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
    private val preferences: SharedPreferences by inject()

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

        val asForeground = preferences.getBoolean(
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
            stopForegroundCompat(ServiceCompat.STOP_FOREGROUND_REMOVE)
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
                throw LongPollException(message = "No access token")
            }

            var serverInfo = getServerInfo()
                ?: throw LongPollException(message = "bad VK response (server info)")

            var lastUpdatesResponse: LongPollUpdates? = getUpdatesResponse(serverInfo)
                ?: throw LongPollException(message = "initiation error: bad VK response (last updates)")

            var failCount = 0

            while (job.isActive) {
                if (lastUpdatesResponse == null) {
                    failCount++
                    serverInfo = getServerInfo()
                        ?: throw LongPollException(message = "failed retrieving server info after error: bad VK response (server info #2)")
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
                            ?: throw LongPollException(
                                message = "failed retrieving server info after error: bad VK response (server info #3)"
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
                                updates.forEach(updatesParser::parseNextUpdate)
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
            version = VkConstants.LP_VERSION
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
            version = VkConstants.LP_VERSION
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

    override fun onDestroy() {
        Log.d(STATE_TAG, "onDestroy")
        try {
            SettingsController.edit {
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

    companion object {
        const val TAG = "LongPollTask"

        private const val STATE_TAG = "LongPollServiceState"

        const val KEY_LONG_POLL_WAS_DESTROYED = "long_poll_was_destroyed"

        private const val NOTIFICATION_ID = 1001
    }
}

private data class LongPollException(override val message: String) : Throwable()
