package com.meloda.fast.service.longpolling.domain.repository

import com.meloda.fast.api.model.data.VkLongPollData
import com.meloda.fast.api.network.longpoll.LongPollGetUpdatesRequest
import com.meloda.fast.api.network.messages.MessagesGetLongPollServerRequest
import com.meloda.fast.base.RestApiErrorDomain
import com.meloda.fast.service.longpolling.data.LongPollUpdates
import com.slack.eithernet.ApiResult

interface LongPollRepository {

    suspend fun getLongPollServer(
        params: MessagesGetLongPollServerRequest
    ): ApiResult<VkLongPollData, RestApiErrorDomain>

    suspend fun getLongPollUpdates(
        serverUrl: String,
        params: LongPollGetUpdatesRequest
    ): ApiResult<LongPollUpdates, RestApiErrorDomain>
}
