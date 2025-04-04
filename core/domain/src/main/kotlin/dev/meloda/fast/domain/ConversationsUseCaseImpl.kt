package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.conversations.ConversationsRepository
import dev.meloda.fast.data.mapToState
import dev.meloda.fast.model.ConversationsFilter
import dev.meloda.fast.model.api.domain.VkConversation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ConversationsUseCaseImpl(
    private val repository: ConversationsRepository,
) : ConversationsUseCase {

    override suspend fun storeConversations(
        conversations: List<VkConversation>
    ) = withContext(Dispatchers.IO) {
        repository.storeConversations(conversations)
    }

    override fun getConversations(
        count: Int?,
        offset: Int?,
        filter: ConversationsFilter
    ): Flow<State<List<VkConversation>>> = flowNewState {
        repository.getConversations(
            count = count,
            offset = offset,
            filter = filter
        ).mapToState()
    }

    override fun getById(
        peerIds: List<Long>,
        extended: Boolean?,
        fields: String?
    ): Flow<State<List<VkConversation>>> = flowNewState {
        repository.getConversationsById(
            peerIds = peerIds,
            extended = extended,
            fields = fields
        ).mapToState()
    }

    override fun delete(peerId: Long): Flow<State<Long>> = flowNewState {
        repository.delete(peerId = peerId).mapToState()
    }

    override fun changePinState(
        peerId: Long,
        pin: Boolean
    ): Flow<State<Int>> = flowNewState {
        if (pin) {
            repository.pin(peerId)
        } else {
            repository.unpin(peerId)
        }.mapToState()
    }

    override fun changeArchivedState(
        peerId: Long,
        archive: Boolean
    ): Flow<State<Int>> = flowNewState {
        if (archive) {
            repository.archive(peerId)
        } else {
            repository.unarchive(peerId)
        }.mapToState()
    }
}
