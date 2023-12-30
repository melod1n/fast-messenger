package com.meloda.fast.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.network.account.AccountSetOfflineRequest
import com.meloda.fast.api.network.account.AccountSetOnlineRequest
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.data.account.AccountsRepository
import com.meloda.fast.ext.createTimerFlow
import com.meloda.fast.screens.settings.SettingsKeys
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.minutes

class OnlineService : Service() {

    private val job = SupervisorJob()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d(TAG, "error: $throwable")
        throwable.printStackTrace()
    }

    private val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job + exceptionHandler

    private val coroutineScope = CoroutineScope(coroutineContext)

    private val repository: AccountsRepository by inject()

    private var timerJob: Job? = null
    private var onlineJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(STATE_TAG, "onBind: intent: $intent")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (startId > 1) return START_STICKY

        Log.d(STATE_TAG, "onStartCommand: flags: $flags; startId: $startId\ninstance: $this")

        if (AppGlobal.preferences.getBoolean(
                SettingsKeys.KEY_VISIBILITY_SEND_ONLINE_STATUS,
                SettingsKeys.DEFAULT_VALUE_KEY_VISIBILITY_SEND_ONLINE_STATUS
            )
        ) {
            createTimer()
        }

        return START_STICKY
    }

    private fun createTimer() {
        timerJob = createTimerFlow(
            isNeedToEndCondition = { false },
            onStartAction = ::setOnline,
            onTickAction = ::setOnline,
            interval = 5.minutes
        ).launchIn(coroutineScope)
    }

    private fun setOnline() {
        if (onlineJob != null) return

        if (!AppGlobal.preferences.getBoolean(
                SettingsKeys.KEY_VISIBILITY_SEND_ONLINE_STATUS,
                SettingsKeys.DEFAULT_VALUE_KEY_VISIBILITY_SEND_ONLINE_STATUS
            )
        ) return

        Log.d(TAG, "setOnline()")

        onlineJob = coroutineScope.launch {
            val token = UserConfig.fastToken ?: UserConfig.accessToken

            if (token.isBlank()) {
                Log.d(TAG, "setOnline: token is empty")
                return@launch
            }

            val response = repository.setOnline(
                AccountSetOnlineRequest(
                    voip = false,
                    accessToken = token
                )
            )
            Log.d(TAG, "setOnline: response: $response")
        }.also { coroutine -> coroutine.invokeOnCompletion { onlineJob = null } }
    }

    private suspend fun setOffline() {
        Log.d(TAG, "setOffline()")

        val response = repository.setOffline(
            AccountSetOfflineRequest(
                accessToken = UserConfig.accessToken
            )
        )

        Log.d(TAG, "setOffline: response: $response")
    }

    override fun onLowMemory() {
        Log.d(STATE_TAG, "onLowMemory")
        super.onLowMemory()
    }

    override fun onDestroy() {
        Log.d(STATE_TAG, "onDestroy")

        timerJob?.cancel("OnlineService destroyed")
        onlineJob?.cancel("OnlineService destroyed")

        super.onDestroy()
    }

    companion object {
        private const val TAG = "OnlineService"
        private const val STATE_TAG = "OnlineServiceState"
    }
}
