package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.messages.MessagesHistoryInfo
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkAttachmentHistoryMessage
import dev.meloda.fast.model.api.domain.VkMessage
import kotlinx.coroutines.flow.Flow

interface MessagesUseCase {

    fun getMessagesHistory(
        conversationId: Int,
        count: Int?,
        offset: Int?
    ): Flow<State<MessagesHistoryInfo>>

    fun getById(
        messageIds: List<Int>,
        extended: Boolean?,
        fields: String?
    ): Flow<State<List<VkMessage>>>

    fun sendMessage(
        peerId: Int,
        randomId: Int,
        message: String?,
        replyTo: Int?,
        attachments: List<VkAttachment>?
    ): Flow<State<Int>>

    fun markAsRead(
        peerId: Int,
        startMessageId: Int
    ): Flow<State<Int>>

    fun getHistoryAttachments(
        peerId: Int,
        count: Int?,
        offset: Int?,
        attachmentTypes: List<String>,
        conversationMessageId: Int
    ): Flow<State<List<VkAttachmentHistoryMessage>>>

    suspend fun storeMessage(message: VkMessage)
    suspend fun storeMessages(messages: List<VkMessage>)
}
