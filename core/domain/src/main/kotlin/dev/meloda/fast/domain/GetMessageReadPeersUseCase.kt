package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.messages.MessagesRepository
import dev.meloda.fast.data.mapToState
import kotlinx.coroutines.flow.Flow

class GetMessageReadPeersUseCase(
    private val repository: MessagesRepository
) : BaseUseCase {

    operator fun invoke(
        peerId: Long,
        cmId: Long
    ): Flow<State<Int>> = flowNewState {
        repository.getMessageReadPeers(
            peerId = peerId,
            cmId = cmId
        ).mapToState(successMapper = { it.totalCount })
    }
}
