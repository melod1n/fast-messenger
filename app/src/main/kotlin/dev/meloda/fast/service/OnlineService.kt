package dev.meloda.fast.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import dev.meloda.fast.common.extensions.createTimerFlow
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.data.processState
import dev.meloda.fast.domain.AccountUseCase
import dev.meloda.fast.logger.FastLogger
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

    private val logger: FastLogger by inject()

    private val job = SupervisorJob()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.error(this::class.java, "CoroutineException", throwable)
    }

    private val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job + exceptionHandler

    private val coroutineScope = CoroutineScope(coroutineContext)

    private val useCase: AccountUseCase by inject()

    private var timerJob: Job? = null
    private var onlineJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? {
        logger.debug(this::class, "STATE: onBind(): intent: $intent")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (startId > 1) return START_STICKY

        logger.debug(
            this::class,
            "STATE: onStartCommand(): flags: %s; startId: %s;\ninstance: %s"
                .format("$flags", "$startId", "$this")
        )

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

        logger.debug(this::class, "setOnline()")

        onlineJob = coroutineScope.launch {
            val token = UserConfig.fastToken ?: UserConfig.accessToken

            if (token.isBlank()) {
                logger.debug(this::class, "setOnline(): token is empty")
                return@launch
            }

            useCase.setOnline(
                voip = false,
                accessToken = token
            ).onEach { state ->
                state.processState(
                    error = { error ->
                        logger.error(this@OnlineService::class, "setOnline(): ERROR: $error")
                    },
                    success = { response ->
                        logger.debug(this@OnlineService::class, "setOnline(): response: $response")
                    }
                )
            }.collect()

        }.also { coroutine -> coroutine.invokeOnCompletion { onlineJob = null } }
    }

    override fun onDestroy() {
        logger.debug(this::class, "onDestroy()")

        timerJob?.cancel("OnlineService destroyed")
        onlineJob?.cancel("OnlineService destroyed")

        super.onDestroy()
    }

    companion object {
        private const val TAG = "OnlineService"
        private const val STATE_TAG = "OnlineServiceState"
    }
}
