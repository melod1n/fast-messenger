package com.meloda.app.fast.data.api.longpoll

import com.meloda.app.fast.model.api.data.LongPollUpdates
import com.meloda.app.fast.model.api.data.VkLongPollData
import com.meloda.app.fast.model.api.requests.LongPollGetUpdatesRequest
import com.meloda.app.fast.model.api.requests.MessagesGetLongPollServerRequest
import com.meloda.app.fast.network.service.longpoll.LongPollService
import com.meloda.app.fast.network.service.messages.MessagesService

class LongPollRepositoryImpl(
    private val longPollService: LongPollService,
    private val messagesService: MessagesService
) : LongPollRepository {

    override suspend fun getLongPollServer(params: MessagesGetLongPollServerRequest): VkLongPollData? {
        TODO("Not yet implemented")
    }

    override suspend fun getLongPollUpdates(
        serverUrl: String,
        params: LongPollGetUpdatesRequest
    ): LongPollUpdates? {
        TODO("Not yet implemented")
    }

    //    override suspend fun getLongPollServer(
//        params: MessagesGetLongPollServerRequest
//    ): ApiResult<VkLongPollData, RestApiErrorDomain> = withContext(Dispatchers.IO) {
//        messagesService.getLongPollServer(params.map).mapResult(
//            successMapper = { response -> response.requireResponse() },
//            errorMapper = { error -> error?.toDomain() }
//        )
//    }
//
//    override suspend fun getLongPollUpdates(
//        serverUrl: String,
//        params: LongPollGetUpdatesRequest
//    ): ApiResult<LongPollUpdates, RestApiErrorDomain> = withContext(Dispatchers.IO) {
//        longPollService.getResponse(serverUrl, params.map).mapResult(
//            successMapper = { response -> response },
//            errorMapper = { error -> error?.toDomain() }
//        )
//    }
}
