package com.meloda.app.fast.data.api.longpoll

import com.meloda.app.fast.model.api.data.LongPollUpdates
import com.meloda.app.fast.model.api.data.VkLongPollData
import com.meloda.app.fast.network.RestApiErrorDomain
import com.slack.eithernet.ApiResult

interface LongPollRepository {

    suspend fun getLongPollServer(
        needPts: Boolean,
        version: Int
    ): ApiResult<VkLongPollData, RestApiErrorDomain>

    suspend fun getLongPollUpdates(
        serverUrl: String,
        act: String,
        key: String,
        ts: Int,
        wait: Int,
        mode: Int,
        version: Int
    ): ApiResult<LongPollUpdates, RestApiErrorDomain>
}
