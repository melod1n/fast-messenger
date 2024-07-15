package com.meloda.app.fast.data.api.longpoll

import com.meloda.app.fast.model.api.data.LongPollUpdates
import com.meloda.app.fast.model.api.data.VkLongPollData
import com.meloda.app.fast.model.api.requests.LongPollGetUpdatesRequest
import com.meloda.app.fast.model.api.requests.MessagesGetLongPollServerRequest
import com.meloda.app.fast.network.RestApiErrorDomain
import com.meloda.app.fast.network.mapApiResult
import com.meloda.app.fast.network.mapDefault
import com.meloda.app.fast.network.service.longpoll.LongPollService
import com.meloda.app.fast.network.service.messages.MessagesService
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LongPollRepositoryImpl(
    private val longPollService: LongPollService,
    private val messagesService: MessagesService
) : LongPollRepository {

    override suspend fun getLongPollServer(
        needPts: Boolean,
        version: Int
    ): ApiResult<VkLongPollData, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = MessagesGetLongPollServerRequest(
            needPts = needPts,
            version = version
        )
        messagesService.getLongPollServer(requestModel.map).mapApiResult(
            successMapper = { response -> response.requireResponse() },
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun getLongPollUpdates(
        serverUrl: String,
        act: String,
        key: String,
        ts: Int,
        wait: Int,
        mode: Int,
        version: Int
    ): ApiResult<LongPollUpdates, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = LongPollGetUpdatesRequest(
            act = act,
            key = key,
            ts = ts,
            wait = wait,
            mode = mode,
            version = version
        )

        longPollService.getResponse(serverUrl, requestModel.map).mapDefault()
    }
}
