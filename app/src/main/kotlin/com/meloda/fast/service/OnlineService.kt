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
import com.meloda.fast.screens.settings.SettingsPrefsFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class OnlineService : Service(), CoroutineScope {

    private val job = SupervisorJob()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d(LongPollService.TAG, "error: $throwable")
        throwable.printStackTrace()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job + exceptionHandler

    @Inject
    lateinit var repository: AccountsRepository

    private var timer: Timer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("OnlineService", "onStartCommand: flags: $flags; startId: $startId")
        createTimer()

        return START_STICKY_COMPATIBILITY
    }

    private fun createTimer() {
        timer = Timer().apply {
            schedule(delay = 0, period = 60 * 1000L) {
                launch { performJob() }
            }
        }
    }

    private suspend fun performJob() {
        if (!AppGlobal.preferences.getBoolean(SettingsPrefsFragment.PrefSendOnlineStatus, true)) {
            return
        }

        setOffline()
        delay(5000)
        setOnline()
    }

    private suspend fun setOnline() {
        Log.d("OnlineService", "setOnline()")

        val fastToken = UserConfig.fastToken

        val token =
            if (fastToken == null) {
                Log.d("OnlineService", "setOnline: Fast token is null. Using VK token")
                UserConfig.accessToken
            } else {
                fastToken
            }

        val response = repository.setOnline(
            AccountSetOnlineRequest(
                voip = false,
                accessToken = token
            )
        )
        Log.d("OnlineService", "setOnline: response: $response")
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
        Log.d("OnlineService", "onDestroy")
    }

}