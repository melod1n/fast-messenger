package com.meloda.fast.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.network.account.AccountDataSource
import com.meloda.fast.api.network.account.AccountSetOfflineRequest
import com.meloda.fast.api.network.account.AccountSetOnlineRequest
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
        Log.d(MessagesUpdateService.TAG, "error: $throwable")
        throwable.printStackTrace()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job + exceptionHandler

    @Inject
    lateinit var dataSource: AccountDataSource

    private var timer: Timer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        timer = Timer().apply {
            schedule(delay = 0, period = 60_000) {
                launch {
                    setOffline()
                    delay(5000)
                    setOnline()
                }
            }
        }

        return START_STICKY_COMPATIBILITY
    }

    private suspend fun setOnline() {
        val fastToken = UserConfig.fastToken

        if (fastToken == null) {
            Log.d("OnlineService", "setOnline: fastToken is null")
            return
        } else {
            Log.d("OnlineService", "setOnline()")
            val response = dataSource.setOnline(
                AccountSetOnlineRequest(
                    voip = false,
                    accessToken = fastToken
                )
            )
            Log.d("OnlineService", "setOnline: response: $response")
        }
    }

    private suspend fun setOffline() {
        Log.d("OnlineService", "setOffline()")
        val response = dataSource.setOffline(
            AccountSetOfflineRequest(
                accessToken = UserConfig.accessToken
            )
        )
        Log.d("OnlineService", "setOffline: response: $response")
    }

}