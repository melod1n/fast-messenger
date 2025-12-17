package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.convos.ConvosRepository
import dev.meloda.fast.data.mapToState
import dev.meloda.fast.model.api.domain.VkConvo
import kotlinx.coroutines.flow.Flow

class LoadConvosByIdUseCase(
    private val convosRepository: ConvosRepository
) : BaseUseCase {

    operator fun invoke(
        peerIds: List<Long>,
        extended: Boolean? = null,
        fields: String? = null
    ): Flow<State<List<VkConvo>>> = flowNewState {
        convosRepository
            .getConvosById(
                peerIds = peerIds,
                extended = extended,
                fields = fields,
            ).mapToState()
    }
}
