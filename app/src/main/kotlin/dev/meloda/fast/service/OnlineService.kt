package dev.meloda.fast.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import dev.meloda.fast.common.UserConfig
import dev.meloda.fast.common.extensions.createTimerFlow
import dev.meloda.fast.data.api.account.AccountUseCase
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

        // TODO: 05/05/2024, Danil Nikolaev: implement
//        if (AppGlobal.preferences.getBoolean(
//                SettingsKeys.KEY_VISIBILITY_SEND_ONLINE_STATUS,
//                SettingsKeys.DEFAULT_VALUE_KEY_VISIBILITY_SEND_ONLINE_STATUS
//            )
//        ) {
//            createTimer()
//        }

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


        // TODO: 05/05/2024, Danil Nikolaev: implement
//        if (!AppGlobal.preferences.getBoolean(
//                SettingsKeys.KEY_VISIBILITY_SEND_ONLINE_STATUS,
//                SettingsKeys.DEFAULT_VALUE_KEY_VISIBILITY_SEND_ONLINE_STATUS
//            )
//        ) return

        Log.d(TAG, "setOnline()")

        onlineJob = coroutineScope.launch {
            val token = UserConfig.fastToken ?: UserConfig.accessToken

            if (token.isBlank()) {
                Log.d(TAG, "setOnline: token is empty")
                return@launch
            }

            val response = useCase.setOnline(
                voip = false,
                accessToken = token
            )
            Log.d(TAG, "setOnline: response: $response")
        }.also { coroutine -> coroutine.invokeOnCompletion { onlineJob = null } }
    }

    private suspend fun setOffline() {
        Log.d(TAG, "setOffline()")

        val response = useCase.setOffline(
            accessToken = UserConfig.accessToken
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
