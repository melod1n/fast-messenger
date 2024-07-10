package com.meloda.app.fast.data.api.messages

import com.meloda.app.fast.model.api.domain.VkAttachment
import com.meloda.app.fast.model.api.domain.VkMessage
import com.meloda.app.fast.network.RestApiErrorDomain
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.flow.Flow

interface MessagesRepository {

    suspend fun getMessagesHistory(
        conversationId: Int,
        offset: Int?,
        count: Int?
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

    suspend fun getMessage(messageId: Int): Flow<VkMessage?>

    suspend fun storeMessages(messages: List<VkMessage>)

//    suspend fun getHistory(
//        params: MessagesGetHistoryRequest
//    ): ApiResult<MessagesGetHistoryResponse, RestApiErrorDomain>

//    suspend fun markAsImportant(
//        params: MessagesMarkAsImportantRequest
//    ): ApiResult<List<Int>, RestApiErrorDomain>
//
//    suspend fun pin(
//        params: MessagesPinMessageRequest
//    ): ApiResult<VkMessageData, RestApiErrorDomain>
//
//    suspend fun unpin(
//        params: MessagesUnPinMessageRequest
//    ): ApiResult<Unit, RestApiErrorDomain>
//
//    suspend fun delete(
//        params: MessagesDeleteRequest
//    ): ApiResult<Unit, RestApiErrorDomain>
//
//    suspend fun edit(
//        params: MessagesEditRequest
//    ): ApiResult<Int, RestApiErrorDomain>
//
//    suspend fun getChat(
//        params: MessagesGetChatRequest
//    ): ApiResult<VkChatData, RestApiErrorDomain>
//
//    suspend fun getConversationMembers(
//        params: MessagesGetConversationMembersRequest
//    ): ApiResult<MessagesGetConversationMembersResponse, RestApiErrorDomain>
//
//    suspend fun removeChatUser(
//        params: MessagesRemoveChatUserRequest
//    ): ApiResult<Int, RestApiErrorDomain>
}
