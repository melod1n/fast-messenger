package com.meloda.app.fast.data.api.messages

import com.meloda.app.fast.model.api.domain.VkAttachment
import com.meloda.app.fast.model.api.domain.VkMessage
import com.meloda.app.fast.network.RestApiErrorDomain
import com.slack.eithernet.ApiResult

interface MessagesNetworkDataSource {

    suspend fun getMessagesHistory(
        conversationId: Int,
        offset: Int?,
        count: Int?,
    ): ApiResult<MessagesHistoryDomain, RestApiErrorDomain>

    suspend fun getMessageById(
        messagesIds: List<Int>,
        extended: Boolean?,
        fields: String?
    ): ApiResult<VkMessage, RestApiErrorDomain>

    suspend fun send(
        peerId: Int,
        randomId: Int,
        message: String?,
        replyTo: Int?,
        attachments: List<VkAttachment>?
    ): ApiResult<Int, RestApiErrorDomain>

    suspend fun markAsRead(
        peerId: Int,
        startMessageId: Int?
    ): ApiResult<Int, RestApiErrorDomain>

    suspend fun getMessage(messageId: Int): VkMessage?
}
