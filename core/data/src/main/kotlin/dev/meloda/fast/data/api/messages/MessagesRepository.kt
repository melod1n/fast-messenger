package dev.meloda.fast.data.api.messages

import com.slack.eithernet.ApiResult
import dev.meloda.fast.model.api.data.VkChatData
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkAttachmentHistoryMessage
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.model.api.responses.MessagesGetConversationMembersResponse
import dev.meloda.fast.model.api.responses.MessagesGetReadPeersResponse
import dev.meloda.fast.model.api.responses.MessagesSendResponse
import dev.meloda.fast.network.RestApiErrorDomain

interface MessagesRepository {

    suspend fun storeMessages(messages: List<VkMessage>)

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
        forward: String?,
        attachments: List<VkAttachment>?,
        formatData: VkMessage.FormatData?
    ): ApiResult<MessagesSendResponse, RestApiErrorDomain>

    suspend fun markAsRead(
        peerId: Long,
        startMessageId: Long?
    ): ApiResult<Int, RestApiErrorDomain>

    suspend fun getHistoryAttachments(
        peerId: Long,
        count: Int?,
        offset: Int?,
        attachmentTypes: List<String>,
        cmId: Long
    ): ApiResult<List<VkAttachmentHistoryMessage>, RestApiErrorDomain>

    suspend fun createChat(
        userIds: List<Long>?,
        title: String?
    ): ApiResult<Long, RestApiErrorDomain>

    suspend fun pin(
        peerId: Long,
        messageId: Long? = null,
        cmId: Long? = null
    ): ApiResult<VkMessage, RestApiErrorDomain>

    suspend fun unpin(
        peerId: Long
    ): ApiResult<Int, RestApiErrorDomain>

    suspend fun markAsImportant(
        peerId: Long,
        messageIds: List<Long>? = null,
        cmIds: List<Long>? = null,
        important: Boolean
    ): ApiResult<List<Long>, RestApiErrorDomain>

    suspend fun delete(
        peerId: Long,
        messageIds: List<Long>?,
        cmIds: List<Long>?,
        spam: Boolean,
        deleteForAll: Boolean
    ): ApiResult<List<Any>, RestApiErrorDomain>

    suspend fun edit(
        peerId: Long,
        messageId: Long? = null,
        cmId: Long? = null,
        message: String? = null,
        lat: Float? = null,
        long: Float? = null,
        attachments: List<VkAttachment>? = null,
        notParseLinks: Boolean = false,
        keepSnippets: Boolean = true,
        keepForwardedMessages: Boolean = true
    ): ApiResult<Int, RestApiErrorDomain>

    suspend fun getChat(
        chatId: Long,
        fields: String? = null
    ): ApiResult<VkChatData, RestApiErrorDomain>

    suspend fun getConversationMembers(
        peerId: Long,
        offset: Int? = null,
        count: Int? = null,
        extended: Boolean? = null,
        fields: String? = null
    ): ApiResult<MessagesGetConversationMembersResponse, RestApiErrorDomain>

    suspend fun removeChatUser(
        chatId: Long,
        memberId: Long
    ): ApiResult<Int, RestApiErrorDomain>

    suspend fun getMessageReadPeers(
        peerId: Long,
        cmId: Long
    ): ApiResult<MessagesGetReadPeersResponse, RestApiErrorDomain>
}
