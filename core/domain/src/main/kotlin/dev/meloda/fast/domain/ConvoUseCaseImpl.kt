package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.convos.ConvosRepository
import dev.meloda.fast.data.mapToState
import dev.meloda.fast.model.ConvosFilter
import dev.meloda.fast.model.api.domain.VkConvo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ConvoUseCaseImpl(
    private val repository: ConvosRepository,
) : ConvoUseCase {

    override suspend fun storeConvos(
        convos: List<VkConvo>
    ) = withContext(Dispatchers.IO) {
        repository.storeConvos(convos)
    }

    override fun getConvos(
        count: Int?,
        offset: Int?,
        filter: ConvosFilter
    ): Flow<State<List<VkConvo>>> = flowNewState {
        repository.getConvos(
            count = count,
            offset = offset,
            filter = filter
        ).mapToState()
    }

    override fun getById(
        peerIds: List<Long>,
        extended: Boolean?,
        fields: String?
    ): Flow<State<List<VkConvo>>> = flowNewState {
        repository.getConvosById(
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
