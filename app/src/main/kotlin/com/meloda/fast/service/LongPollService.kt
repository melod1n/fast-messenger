package com.meloda.fast.service

import android.util.Log
import com.meloda.fast.api.model.request.MessagesGetLongPollServerRequest
import com.meloda.fast.api.network.datasource.MessagesDataSource
import com.meloda.fast.api.network.repo.LongPollRepo
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class LongPollService {
}

class LongPollTask @Inject constructor(
    private val dataSource: MessagesDataSource,
    private val longPollRepo: LongPollRepo
) : CoroutineScope {

    companion object {
        const val TAG = "LongPollTask"
    }

    private val job = SupervisorJob()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d(TAG, "error: $throwable")
        throwable.printStackTrace()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job + exceptionHandler

    fun startPolling(): Job {
        if (job.isCompleted || job.isCancelled) throw Exception("Job is over")

        return launch {
            val serverInfo = dataSource.getLongPollServer(
                MessagesGetLongPollServerRequest(
                    needPts = true,
                    version = 10
                )
            )

            println("TESTJOPAAAAAA: $serverInfo")
//            val response = serverInfo.response ?: return@launch


        }
    }
}