package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.messages.MessagesHistoryInfo
import dev.meloda.fast.data.api.messages.MessagesRepository
import dev.meloda.fast.data.mapToState
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkAttachmentHistoryMessage
import dev.meloda.fast.model.api.domain.VkMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MessagesUseCaseImpl(
    private val repository: MessagesRepository,
) : MessagesUseCase {

    override fun getMessagesHistory(
        conversationId: Long,
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
        peerCmIds: List<Long>?,
        peerId: Long?,
        messageIds: List<Long>?,
        cmIds: List<Long>?,
        extended: Boolean?,
        fields: String?
    ): Flow<State<List<VkMessage>>> = flow {
        emit(State.Loading)

        val newState = repository.getById(
            peerCmIds = peerCmIds,
            peerId = peerId,
            messagesIds = messageIds,
            cmIds = cmIds,
            extended = extended,
            fields = fields
        ).mapToState()

        emit(newState)
    }

    override fun sendMessage(
        peerId: Long,
        randomId: Long,
        message: String?,
        replyTo: Long?,
        attachments: List<VkAttachment>?
    ): Flow<State<Long>> = flow {
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
        peerId: Long,
        startMessageId: Long
    ): Flow<State<Int>> = flow {
        emit(State.Loading)

        val newState = repository.markAsRead(
            peerId = peerId,
            startMessageId = startMessageId
        ).mapToState()

        emit(newState)
    }

    override fun getHistoryAttachments(
        peerId: Long,
        count: Int?,
        offset: Int?,
        attachmentTypes: List<String>,
        conversationMessageId: Long
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

    override fun createChat(userIds: List<Long>?, title: String?): Flow<State<Long>> = flow {
        emit(State.Loading)
        val newState = repository.createChat(userIds, title).mapToState()
        emit(newState)
    }

    override fun pin(
        peerId: Long,
        messageId: Long?,
        conversationMessageId: Long?
    ): Flow<State<VkMessage>> = flow {
        emit(State.Loading)

        val newState = repository.pin(
            peerId = peerId,
            messageId = messageId,
            conversationMessageId = conversationMessageId
        ).mapToState()

        emit(newState)
    }

    override fun unpin(peerId: Long): Flow<State<Int>> = flow {
        emit(State.Loading)
        val newState = repository.unpin(peerId = peerId).mapToState()
        emit(newState)
    }

    override fun markAsImportant(
        peerId: Long,
        messageIds: List<Long>,
        important: Boolean
    ): Flow<State<List<Long>>> = flow {
        emit(State.Loading)

        val newState = repository.markAsImportant(
            peerId = peerId,
            messageIds = messageIds,
            conversationMessageIds = null,
            important = important
        ).mapToState()

        emit(newState)
    }

    override fun delete(
        peerId: Long,
        messageIds: List<Long>,
        spam: Boolean,
        deleteForAll: Boolean
    ): Flow<State<List<Any>>> = flow {
        emit(State.Loading)

        val newState = repository.delete(
            peerId = peerId,
            messageIds = messageIds,
            conversationMessageIds = null,
            spam = spam,
            deleteForAll = deleteForAll
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
