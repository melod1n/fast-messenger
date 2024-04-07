package com.meloda.fast.screens.messages.domain.repository

import com.meloda.fast.api.model.data.VkChatData
import com.meloda.fast.api.model.data.VkMessageData
import com.meloda.fast.api.network.messages.MessagesDeleteRequest
import com.meloda.fast.api.network.messages.MessagesEditRequest
import com.meloda.fast.api.network.messages.MessagesGetByIdRequest
import com.meloda.fast.api.network.messages.MessagesGetByIdResponse
import com.meloda.fast.api.network.messages.MessagesGetConversationMembersResponse
import com.meloda.fast.api.network.messages.MessagesGetHistoryRequest
import com.meloda.fast.api.network.messages.MessagesGetHistoryResponse
import com.meloda.fast.api.network.messages.MessagesMarkAsImportantRequest
import com.meloda.fast.api.network.messages.MessagesPinMessageRequest
import com.meloda.fast.api.network.messages.MessagesSendRequest
import com.meloda.fast.api.network.messages.MessagesUnPinMessageRequest
import com.meloda.fast.base.RestApiErrorDomain
import com.slack.eithernet.ApiResult

interface MessagesRepository {

    suspend fun getHistory(
        params: MessagesGetHistoryRequest
    ): ApiResult<MessagesGetHistoryResponse, RestApiErrorDomain>

    suspend fun send(
        params: MessagesSendRequest
    ): ApiResult<Int, RestApiErrorDomain>

    suspend fun markAsImportant(
        params: MessagesMarkAsImportantRequest
    ): ApiResult<List<Int>, RestApiErrorDomain>

    suspend fun pin(
        params: MessagesPinMessageRequest
    ): ApiResult<VkMessageData, RestApiErrorDomain>

    suspend fun unpin(
        params: MessagesUnPinMessageRequest
    ): ApiResult<Unit, RestApiErrorDomain>

    suspend fun delete(
        params: MessagesDeleteRequest
    ): ApiResult<Unit, RestApiErrorDomain>

    suspend fun edit(
        params: MessagesEditRequest
    ): ApiResult<Int, RestApiErrorDomain>

    suspend fun getById(
        params: MessagesGetByIdRequest
    ): ApiResult<MessagesGetByIdResponse, RestApiErrorDomain>

    suspend fun markAsRead(
        peerId: Int,
        messageIds: List<Int>?,
        startMessageId: Int?
    ): ApiResult<Int, RestApiErrorDomain>

    suspend fun getChat(
        chatId: Int,
        fields: String?,
    ): ApiResult<VkChatData, RestApiErrorDomain>

    suspend fun getConversationMembers(
        peerId: Int,
        offset: Int?,
        count: Int?,
        extended: Boolean?,
        fields: String?,
    ): ApiResult<MessagesGetConversationMembersResponse, RestApiErrorDomain>

    suspend fun removeChatUser(
        chatId: Int,
        memberId: Int,
    ): ApiResult<Int, RestApiErrorDomain>
}
