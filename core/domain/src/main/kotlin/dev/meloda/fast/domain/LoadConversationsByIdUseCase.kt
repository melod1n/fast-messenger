package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.conversations.ConversationsRepository
import dev.meloda.fast.data.mapToState
import dev.meloda.fast.model.api.domain.VkConversation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LoadConversationsByIdUseCase(
    private val conversationsRepository: ConversationsRepository
) {

    operator fun invoke(peerIds: List<Int>): Flow<State<List<VkConversation>>> = flow {
        emit(State.Loading)

        val newState = conversationsRepository
            .getConversationsById(peerIds = peerIds)
            .mapToState()

        emit(newState)
    }
}
