package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.messages.MessagesHistoryInfo
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkAttachmentHistoryMessage
import dev.meloda.fast.model.api.domain.VkMessage
import kotlinx.coroutines.flow.Flow

interface MessagesUseCase {

    fun getMessagesHistory(
        conversationId: Long,
        count: Int?,
        offset: Int?
    ): Flow<State<MessagesHistoryInfo>>

    fun getById(
        messageIds: List<Long>,
        extended: Boolean?,
        fields: String?
    ): Flow<State<List<VkMessage>>>

    fun sendMessage(
        peerId: Long,
        randomId: Long,
        message: String?,
        replyTo: Long?,
        attachments: List<VkAttachment>?
    ): Flow<State<Long>>

    fun markAsRead(
        peerId: Long,
        startMessageId: Long
    ): Flow<State<Int>>

    fun getHistoryAttachments(
        peerId: Long,
        count: Int?,
        offset: Int?,
        attachmentTypes: List<String>,
        conversationMessageId: Long
    ): Flow<State<List<VkAttachmentHistoryMessage>>>

    fun createChat(
        userIds: List<Long>?,
        title: String?
    ): Flow<State<Long>>

    fun pin(
        peerId: Long,
        messageId: Long?,
        conversationMessageId: Long?
    ): Flow<State<VkMessage>>

    fun unpin(
        peerId: Long
    ): Flow<State<Int>>

    fun markAsImportant(
        peerId: Long,
        messageIds: List<Long>,
        important: Boolean
    ): Flow<State<List<Long>>>

    fun delete(
        peerId: Long,
        messageIds: List<Long>,
        spam: Boolean = false,
        deleteForAll: Boolean = false
    ): Flow<State<List<Any>>>

    suspend fun storeMessage(message: VkMessage)
    suspend fun storeMessages(messages: List<VkMessage>)
}
