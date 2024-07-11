package com.meloda.app.fast.data.api.messages

import com.meloda.app.fast.data.State
import com.meloda.app.fast.model.api.domain.VkAttachment
import com.meloda.app.fast.model.api.domain.VkAttachmentHistoryMessage
import com.meloda.app.fast.model.api.domain.VkMessage
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
