package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.model.ConversationsFilter
import dev.meloda.fast.model.api.domain.VkConversation
import kotlinx.coroutines.flow.Flow

interface ConversationsUseCase : BaseUseCase {

    suspend fun storeConversations(conversations: List<VkConversation>)

    fun getConversations(
        count: Int? = null,
        offset: Int? = null,
        filter: ConversationsFilter
    ): Flow<State<List<VkConversation>>>

    fun getById(
        peerIds: List<Long>,
        extended: Boolean? = null,
        fields: String? = null
    ): Flow<State<List<VkConversation>>>

    fun delete(peerId: Long): Flow<State<Long>>

    fun changePinState(peerId: Long, pin: Boolean): Flow<State<Int>>

    fun changeArchivedState(peerId: Long, archive: Boolean): Flow<State<Int>>
}
