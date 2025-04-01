package dev.meloda.fast.data.api.messages

import com.slack.eithernet.ApiResult
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkAttachmentHistoryMessage
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.network.RestApiErrorDomain

interface MessagesRepository {

    suspend fun getHistory(
        conversationId: Long,
        offset: Int?,
        count: Int?
    ): ApiResult<MessagesHistoryInfo, RestApiErrorDomain>

    suspend fun getById(
        peerCmIds: List<Long>?,
        peerId: Long?,
        messagesIds: List<Long>?,
        cmIds: List<Long>?,
        extended: Boolean?,
        fields: String?
    ): ApiResult<List<VkMessage>, RestApiErrorDomain>

    suspend fun send(
        peerId: Long,
        randomId: Long,
        message: String?,
        replyTo: Long?,
        attachments: List<VkAttachment>?
    ): ApiResult<Long, RestApiErrorDomain>

    suspend fun markAsRead(
        peerId: Long,
        startMessageId: Long?
    ): ApiResult<Int, RestApiErrorDomain>

    suspend fun getHistoryAttachments(
        peerId: Long,
        count: Int?,
        offset: Int?,
        attachmentTypes: List<String>,
        conversationMessageId: Long
    ): ApiResult<List<VkAttachmentHistoryMessage>, RestApiErrorDomain>

    suspend fun createChat(
        userIds: List<Long>?,
        title: String?
    ): ApiResult<Long, RestApiErrorDomain>

    suspend fun pin(
        peerId: Long,
        messageId: Long?,
        conversationMessageId: Long?
    ): ApiResult<VkMessage, RestApiErrorDomain>

    suspend fun unpin(
        peerId: Long
    ): ApiResult<Int, RestApiErrorDomain>

    suspend fun markAsImportant(
        peerId: Long,
        messageIds: List<Long>?,
        conversationMessageIds: List<Long>?,
        important: Boolean
    ): ApiResult<List<Long>, RestApiErrorDomain>

    suspend fun delete(
        peerId: Long,
        messageIds: List<Long>?,
        conversationMessageIds: List<Long>?,
        spam: Boolean,
        deleteForAll: Boolean
    ): ApiResult<List<Any>, RestApiErrorDomain>

    suspend fun storeMessages(messages: List<VkMessage>)
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
