package dev.meloda.fast.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import dev.meloda.fast.common.extensions.createTimerFlow
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.domain.AccountUseCase
import dev.meloda.fast.data.processState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    private val useCase: AccountUseCase by inject()

    private var timerJob: Job? = null
    private var onlineJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(STATE_TAG, "onBind: intent: $intent")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (startId > 1) return START_STICKY

        Log.d(STATE_TAG, "onStartCommand: flags: $flags; startId: $startId\ninstance: $this")

        createTimer()

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

        Log.d(TAG, "setOnline()")

        onlineJob = coroutineScope.launch {
            val token = UserConfig.fastToken ?: UserConfig.accessToken

            if (token.isBlank()) {
                Log.d(TAG, "setOnline: token is empty")
                return@launch
            }

            useCase.setOnline(
                voip = false,
                accessToken = token
            ).onEach { state ->
                state.processState(
                    error = { error ->
                        Log.w(TAG, "setOnline(): error: $error")
                    },
                    success = { response ->
                        Log.d(TAG, "setOnline(): success: $response")
                    }
                )
            }.collect()

        }.also { coroutine -> coroutine.invokeOnCompletion { onlineJob = null } }
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
