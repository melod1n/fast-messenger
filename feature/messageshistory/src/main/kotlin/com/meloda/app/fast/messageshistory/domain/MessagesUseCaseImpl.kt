package com.meloda.app.fast.messageshistory.domain

import com.meloda.app.fast.data.State
import com.meloda.app.fast.data.api.messages.MessagesHistoryInfo
import com.meloda.app.fast.data.api.messages.MessagesRepository
import com.meloda.app.fast.data.api.messages.MessagesUseCase
import com.meloda.app.fast.data.mapToState
import com.meloda.app.fast.model.api.domain.VkAttachment
import com.meloda.app.fast.model.api.domain.VkAttachmentHistoryMessage
import com.meloda.app.fast.model.api.domain.VkMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MessagesUseCaseImpl(
    private val repository: MessagesRepository
) : MessagesUseCase {

    override fun getMessagesHistory(
        conversationId: Int,
        count: Int?,
        offset: Int?
    ): Flow<State<MessagesHistoryInfo>> = flow {
        emit(State.Loading)

        val newState = repository.getHistory(
            conversationId = conversationId,
            offset = offset,
            count = count
        ).mapToState()

        emit(newState)
    }

    override fun getById(
        messageIds: List<Int>,
        extended: Boolean?,
        fields: String?
    ): Flow<State<List<VkMessage>>> = flow {
        emit(State.Loading)

        val newState = repository.getById(
            messagesIds = messageIds,
            extended = extended,
            fields = fields
        ).mapToState()

        emit(newState)
    }

    override fun sendMessage(
        peerId: Int,
        randomId: Int,
        message: String?,
        replyTo: Int?,
        attachments: List<VkAttachment>?
    ): Flow<State<Int>> = flow {
        emit(State.Loading)

        val newState = repository.send(
            peerId = peerId,
            randomId = randomId,
            message = message,
            replyTo = replyTo,
            attachments = attachments
        ).mapToState()

        emit(newState)
    }

    override fun markAsRead(
        peerId: Int,
        startMessageId: Int
    ): Flow<State<Int>> = flow {
        emit(State.Loading)

        val newState = repository.markAsRead(
            peerId = peerId,
            startMessageId = startMessageId
        ).mapToState()

        emit(newState)
    }

    override fun getHistoryAttachments(
        peerId: Int,
        count: Int?,
        offset: Int?,
        attachmentTypes: List<String>,
        conversationMessageId: Int
    ): Flow<State<List<VkAttachmentHistoryMessage>>> = flow {
        emit(State.Loading)

        val newState = repository.getHistoryAttachments(
            peerId = peerId,
            count = count,
            offset = offset,
            attachmentTypes = attachmentTypes,
            conversationMessageId = conversationMessageId
        ).mapToState()

        emit(newState)
    }

    override suspend fun storeMessage(message: VkMessage) {
        repository.storeMessages(listOf(message))
    }

    override suspend fun storeMessages(messages: List<VkMessage>) {
        repository.storeMessages(messages)
    }
}
