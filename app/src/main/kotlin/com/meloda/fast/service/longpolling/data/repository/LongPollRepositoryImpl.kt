package com.meloda.fast.service.longpolling.data.repository

import com.meloda.fast.api.model.data.VkLongPollData
import com.meloda.fast.api.network.longpoll.LongPollGetUpdatesRequest
import com.meloda.fast.api.network.messages.MessagesGetLongPollServerRequest
import com.meloda.fast.base.RestApiErrorDomain
import com.meloda.fast.base.mapResult
import com.meloda.fast.service.longpolling.data.LongPollUpdates
import com.meloda.fast.service.longpolling.data.service.LongPollService
import com.meloda.fast.service.longpolling.domain.repository.LongPollRepository
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LongPollRepositoryImpl(
    private val longPollService: LongPollService
) : LongPollRepository {

    override suspend fun getLongPollServer(
        params: MessagesGetLongPollServerRequest
    ): ApiResult<VkLongPollData, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        longPollService.getLongPollServer(params.map).mapResult(
            successMapper = { response -> response.requireResponse() },
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun getLongPollUpdates(
        serverUrl: String,
        params: LongPollGetUpdatesRequest
    ): ApiResult<LongPollUpdates, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        longPollService.getResponse(serverUrl, params.map).mapResult(
            successMapper = { response -> response },
            errorMapper = { error -> error?.toDomain() }
        )
    }
}
