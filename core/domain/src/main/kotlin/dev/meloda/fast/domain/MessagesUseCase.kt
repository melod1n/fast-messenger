package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.messages.MessagesHistoryInfo
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkAttachmentHistoryMessage
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.model.api.responses.MessagesSendResponse
import kotlinx.coroutines.flow.Flow

interface MessagesUseCase : BaseUseCase {

    suspend fun storeMessage(message: VkMessage)
    suspend fun storeMessages(messages: List<VkMessage>)

    fun getMessagesHistory(
        conversationId: Long,
        count: Int?,
        offset: Int?
    ): Flow<State<MessagesHistoryInfo>>

    fun getById(
        peerCmIds: List<Long>?,
        peerId: Long?,
        messageIds: List<Long>?,
        cmIds: List<Long>?,
        extended: Boolean?,
        fields: String?
    ): Flow<State<List<VkMessage>>>

    fun sendMessage(
        peerId: Long,
        randomId: Long,
        message: String?,
        replyTo: Long?,
        attachments: List<VkAttachment>?,
    ): Flow<State<MessagesSendResponse>>

    fun markAsRead(
        peerId: Long,
        startMessageId: Long
    ): Flow<State<Int>>

    fun getHistoryAttachments(
        peerId: Long,
        count: Int? = null,
        offset: Int? = null,
        attachmentTypes: List<String>,
        cmId: Long
    ): Flow<State<List<VkAttachmentHistoryMessage>>>

    fun createChat(
        userIds: List<Long>? = null,
        title: String
    ): Flow<State<Long>>

    fun pin(
        peerId: Long,
        messageId: Long? = null,
        cmId: Long? = null
    ): Flow<State<VkMessage>>

    fun unpin(
        peerId: Long
    ): Flow<State<Int>>

    fun markAsImportant(
        peerId: Long,
        messageIds: List<Long>? = null,
        cmIds: List<Long>? = null,
        important: Boolean
    ): Flow<State<List<Long>>>

    fun delete(
        peerId: Long,
        messageIds: List<Long>? = null,
        cmIds: List<Long>? = null,
        spam: Boolean = false,
        deleteForAll: Boolean = false
    ): Flow<State<List<Any>>>
}
