package dev.meloda.fast.data.api.messages

import com.slack.eithernet.ApiResult
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkAttachmentHistoryMessage
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.network.RestApiErrorDomain

interface MessagesRepository {

    suspend fun getHistory(
        conversationId: Int,
        offset: Int?,
        count: Int?
    ): ApiResult<MessagesHistoryInfo, RestApiErrorDomain>

    suspend fun getById(
        messagesIds: List<Int>,
        extended: Boolean?,
        fields: String?
    ): ApiResult<List<VkMessage>, RestApiErrorDomain>

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

    suspend fun getHistoryAttachments(
        peerId: Int,
        count: Int?,
        offset: Int?,
        attachmentTypes: List<String>,
        conversationMessageId: Int
    ): ApiResult<List<VkAttachmentHistoryMessage>, RestApiErrorDomain>

    suspend fun createChat(
        userIds: List<Int>?,
        title: String?
    ): ApiResult<Int, RestApiErrorDomain>

    suspend fun pin(
        peerId: Int,
        messageId: Int?,
        conversationMessageId: Int?
    ): ApiResult<VkMessage, RestApiErrorDomain>

    suspend fun unpin(
        peerId: Int
    ): ApiResult<Int, RestApiErrorDomain>

    suspend fun storeMessages(messages: List<VkMessage>)

//    suspend fun markAsImportant(
//        params: MessagesMarkAsImportantRequest
//    ): ApiResult<List<Int>, RestApiErrorDomain>
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
