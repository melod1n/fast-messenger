package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.messages.MessagesHistoryInfo
import dev.meloda.fast.data.api.messages.MessagesRepository
import dev.meloda.fast.data.mapToState
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkAttachmentHistoryMessage
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.model.api.responses.MessagesSendResponse
import kotlinx.coroutines.flow.Flow

class MessagesUseCaseImpl(
    private val repository: MessagesRepository,
) : MessagesUseCase {

    override suspend fun storeMessage(message: VkMessage) {
        repository.storeMessages(listOf(message))
    }

    override suspend fun storeMessages(messages: List<VkMessage>) {
        repository.storeMessages(messages)
    }

    override fun getMessagesHistory(
        conversationId: Long,
        count: Int?,
        offset: Int?
    ): Flow<State<MessagesHistoryInfo>> = flowNewState {
        repository.getHistory(
            conversationId = conversationId,
            offset = offset,
            count = count
        ).mapToState()
    }

    override fun getById(
        peerCmIds: List<Long>?,
        peerId: Long?,
        messageIds: List<Long>?,
        cmIds: List<Long>?,
        extended: Boolean?,
        fields: String?
    ): Flow<State<List<VkMessage>>> = flowNewState {
        repository.getById(
            peerCmIds = peerCmIds,
            peerId = peerId,
            messagesIds = messageIds,
            cmIds = cmIds,
            extended = extended,
            fields = fields
        ).mapToState()
    }

    override fun sendMessage(
        peerId: Long,
        randomId: Long,
        message: String?,
        replyTo: Long?,
        attachments: List<VkAttachment>?
    ): Flow<State<MessagesSendResponse>> = flowNewState {
        repository.send(
            peerId = peerId,
            randomId = randomId,
            message = message,
            replyTo = replyTo,
            attachments = attachments
        ).mapToState()
    }

    override fun markAsRead(
        peerId: Long,
        startMessageId: Long
    ): Flow<State<Int>> = flowNewState {
        repository.markAsRead(
            peerId = peerId,
            startMessageId = startMessageId
        ).mapToState()
    }

    override fun getHistoryAttachments(
        peerId: Long,
        count: Int?,
        offset: Int?,
        attachmentTypes: List<String>,
        cmId: Long
    ): Flow<State<List<VkAttachmentHistoryMessage>>> = flowNewState {
        repository.getHistoryAttachments(
            peerId = peerId,
            count = count,
            offset = offset,
            attachmentTypes = attachmentTypes,
            cmId = cmId
        ).mapToState()
    }

    override fun createChat(
        userIds: List<Long>?,
        title: String
    ): Flow<State<Long>> = flowNewState {
        repository.createChat(userIds, title).mapToState()
    }

    override fun pin(
        peerId: Long,
        messageId: Long?,
        cmId: Long?
    ): Flow<State<VkMessage>> = flowNewState {
        repository.pin(
            peerId = peerId,
            messageId = messageId,
            cmId = cmId
        ).mapToState()
    }

    override fun unpin(peerId: Long): Flow<State<Int>> = flowNewState {
        repository.unpin(peerId = peerId).mapToState()
    }

    override fun markAsImportant(
        peerId: Long,
        messageIds: List<Long>?,
        cmIds: List<Long>?,
        important: Boolean
    ): Flow<State<List<Long>>> = flowNewState {
        repository.markAsImportant(
            peerId = peerId,
            messageIds = messageIds,
            cmIds = cmIds,
            important = important
        ).mapToState()
    }

    override fun delete(
        peerId: Long,
        messageIds: List<Long>?,
        cmIds: List<Long>?,
        spam: Boolean,
        deleteForAll: Boolean
    ): Flow<State<List<Any>>> = flowNewState {
        repository.delete(
            peerId = peerId,
            messageIds = messageIds,
            cmIds = cmIds,
            spam = spam,
            deleteForAll = deleteForAll
        ).mapToState()
    }
}
