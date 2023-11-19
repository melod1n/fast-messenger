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
import com.meloda.fast.screens.settings.SettingsKeys
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.coroutines.CoroutineContext

class OnlineService : Service(), CoroutineScope {

    private val job = SupervisorJob()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d(LongPollService.TAG, "error: $throwable")
        throwable.printStackTrace()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job + exceptionHandler

    private val repository: AccountsRepository by inject()

    private var timer: Timer? = null

    private var currentJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("OnlineService", "onStartCommand: flags: $flags; startId: $startId")

        if (AppGlobal.preferences.getBoolean(
                SettingsKeys.KEY_VISIBILITY_SEND_ONLINE_STATUS, true
            )
        ) {
            createTimer()
        }

        return START_STICKY_COMPATIBILITY
    }

    private fun createTimer() {
        timer = Timer().apply {
            schedule(delay = 0, period = 300 * 1000L) {
                setOnline()
            }
        }
    }

    private fun setOnline() {
        if (currentJob != null) return

        if (!AppGlobal.preferences.getBoolean(
                SettingsKeys.KEY_VISIBILITY_SEND_ONLINE_STATUS,
                false
            )
        ) return

        currentJob = launch {
            Log.d("OnlineService", "setOnline()")

            val token = UserConfig.fastToken ?: UserConfig.accessToken

            if (token.isBlank()) {
                Log.d("OnlineService", "setOnline: token is empty")
                return@launch
            }

            val response = repository.setOnline(
                AccountSetOnlineRequest(
                    voip = false,
                    accessToken = token
                )
            )
            Log.d("OnlineService", "setOnline: response: $response")
            currentJob = null
        }
    }

    private suspend fun setOffline() {
        Log.d("OnlineService", "setOffline()")
        val response = repository.setOffline(
            AccountSetOfflineRequest(
                accessToken = UserConfig.accessToken
            )
        )
        Log.d("OnlineService", "setOffline: response: $response")
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        currentJob?.cancel("OnlineService destroyed")
        Log.d("OnlineService", "onDestroy")
    }
}
