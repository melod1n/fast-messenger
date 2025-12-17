package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.model.ConvosFilter
import dev.meloda.fast.model.api.domain.VkConvo
import kotlinx.coroutines.flow.Flow

interface ConvoUseCase : BaseUseCase {

    suspend fun storeConvos(convos: List<VkConvo>)

    fun getConvos(
        count: Int? = null,
        offset: Int? = null,
        filter: ConvosFilter
    ): Flow<State<List<VkConvo>>>

    fun getById(
        peerIds: List<Long>,
        extended: Boolean? = null,
        fields: String? = null
    ): Flow<State<List<VkConvo>>>

    fun delete(peerId: Long): Flow<State<Long>>

    fun changePinState(peerId: Long, pin: Boolean): Flow<State<Int>>

    fun changeArchivedState(peerId: Long, archive: Boolean): Flow<State<Int>>
}
