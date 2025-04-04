package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.conversations.ConversationsRepository
import dev.meloda.fast.data.mapToState
import dev.meloda.fast.model.api.domain.VkConversation
import kotlinx.coroutines.flow.Flow

class LoadConversationsByIdUseCase(
    private val conversationsRepository: ConversationsRepository
) : BaseUseCase {

    operator fun invoke(
        peerIds: List<Long>,
        extended: Boolean? = null,
        fields: String? = null
    ): Flow<State<List<VkConversation>>> = flowNewState {
        conversationsRepository
            .getConversationsById(
                peerIds = peerIds,
                extended = extended,
                fields = fields,
            ).mapToState()
    }
}
