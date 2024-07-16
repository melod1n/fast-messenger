package dev.meloda.fast.data.api.longpoll

import dev.meloda.fast.model.api.data.LongPollUpdates
import dev.meloda.fast.model.api.data.VkLongPollData
import dev.meloda.fast.model.api.requests.LongPollGetUpdatesRequest
import dev.meloda.fast.model.api.requests.MessagesGetLongPollServerRequest
import dev.meloda.fast.network.RestApiErrorDomain
import dev.meloda.fast.network.mapApiResult
import dev.meloda.fast.network.mapDefault
import dev.meloda.fast.network.service.longpoll.LongPollService
import dev.meloda.fast.network.service.messages.MessagesService
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
