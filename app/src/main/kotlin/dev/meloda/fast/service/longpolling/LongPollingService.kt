package dev.meloda.fast.service.longpolling

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.conena.nanokt.android.app.stopForegroundCompat
import dev.meloda.fast.common.AppConstants
import dev.meloda.fast.common.LongPollController
import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.model.LongPollState
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.data.processState
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.domain.LongPollUpdatesParser
import dev.meloda.fast.domain.LongPollUseCase
import dev.meloda.fast.model.api.data.LongPollUpdates
import dev.meloda.fast.model.api.data.VkLongPollData
import dev.meloda.fast.ui.R
import dev.meloda.fast.util.NotificationsUtils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.seconds

class LongPollingService : Service() {

    private val longPollController: LongPollController by inject()

    private val job = SupervisorJob()

    private val exceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            handleError(throwable)
        }

    private val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job + exceptionHandler

    private val coroutineScope = CoroutineScope(coroutineContext)

    private val longPollUseCase: LongPollUseCase by inject()
    private val updatesParser: LongPollUpdatesParser by inject()

    private var currentJob: Job? = null

    private val inBackground get() = AppSettings.Experimental.longPollInBackground

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

        Log.d(
            STATE_TAG,
            "onStartCommand: asForeground: $inBackground; flags: $flags; startId: $startId;\ninstance: $this"
        )

        startJob()

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

        if (inBackground) {
            val notification =
                NotificationsUtils.createNotification(
                    context = this,
                    title = getString(R.string.long_polling_service_notification_title),
                    contentText = getString(R.string.long_polling_service_notification_content),
                    notRemovable = false,
                    channelId = AppConstants.NOTIFICATION_CHANNEL_LONG_POLLING,
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

    private fun startJob() {
        if (currentJob != null) {
            currentJob?.cancel()
            currentJob = null
        }

        coroutineScope.launch {
            currentJob = startPolling().also { it.join() }
        }
    }

    private fun startPolling(): Job {
        if (job.isCompleted || job.isCancelled) {
            Log.d(STATE_TAG, "Job is completed or cancelled")
            throw Exception("Job is over")
        }

        Log.d(STATE_TAG, "Starting job...")

        return coroutineScope.launch(coroutineContext) {
            longPollController.updateCurrentState(
                if (inBackground) LongPollState.Background
                else LongPollState.InApp
            )

            if (UserConfig.accessToken.isEmpty()) {
                throw NoAccessTokenException()
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

    private fun handleError(throwable: Throwable) {
        Log.e(TAG, "error: $throwable")

        if (throwable !is NoAccessTokenException) {
            throwable.printStackTrace()
        }

        coroutineScope.launch {
            delay(5.seconds)
            startJob()
        }

        longPollController.updateCurrentState(LongPollState.Exception)
    }

    override fun onDestroy() {
        Log.d(STATE_TAG, "onDestroy")
        longPollController.updateCurrentState(LongPollState.Stopped)
        try {
            AppSettings.edit { putBoolean(KEY_LONG_POLL_WAS_DESTROYED, true) }
            job.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    override fun onTrimMemory(level: Int) {
        Log.d(STATE_TAG, "onTrimMemory. Level: $level")
        super.onTrimMemory(level)
    }

    companion object {
        const val TAG = "LongPollTask"

        private const val STATE_TAG = "LongPollServiceState"

        const val KEY_LONG_POLL_WAS_DESTROYED = "long_poll_was_destroyed"

        private const val NOTIFICATION_ID = 1001
    }
}

private data class LongPollException(override val message: String) : Throwable()
private class NoAccessTokenException : Throwable()
