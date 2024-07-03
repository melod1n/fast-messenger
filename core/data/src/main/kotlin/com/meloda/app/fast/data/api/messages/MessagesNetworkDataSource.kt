package com.meloda.app.fast.data.api.messages

import com.meloda.app.fast.model.api.domain.VkMessage
import com.meloda.app.fast.network.RestApiErrorDomain
import com.slack.eithernet.ApiResult

interface MessagesNetworkDataSource {

    suspend fun getMessagesHistory(
        conversationId: Int,
        offset: Int?,
        count: Int?,
    ): ApiResult<MessagesHistoryDomain, RestApiErrorDomain>

    suspend fun getMessage(messageId: Int): VkMessage?
}
