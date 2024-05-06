package com.meloda.app.fast.data.api.longpoll

import com.meloda.app.fast.model.api.data.LongPollUpdates
import com.meloda.app.fast.model.api.data.VkLongPollData
import com.meloda.app.fast.model.api.requests.LongPollGetUpdatesRequest
import com.meloda.app.fast.model.api.requests.MessagesGetLongPollServerRequest

interface LongPollRepository {

    suspend fun getLongPollServer(
        params: MessagesGetLongPollServerRequest
    ): VkLongPollData?

    suspend fun getLongPollUpdates(
        serverUrl: String,
        params: LongPollGetUpdatesRequest
    ): LongPollUpdates?
}
