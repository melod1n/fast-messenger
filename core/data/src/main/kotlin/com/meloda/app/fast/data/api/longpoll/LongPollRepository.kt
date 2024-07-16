package dev.meloda.fast.data.api.longpoll

import dev.meloda.fast.model.api.data.LongPollUpdates
import dev.meloda.fast.model.api.data.VkLongPollData
import dev.meloda.fast.network.RestApiErrorDomain
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
