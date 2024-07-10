package com.meloda.app.fast.data.api.conversations

import com.meloda.app.fast.data.State
import com.meloda.app.fast.model.api.domain.VkConversation
import kotlinx.coroutines.flow.Flow

interface ConversationsUseCase {

    fun getConversations(
        count: Int?,
        offset: Int?,
    ): Flow<State<List<VkConversation>>>

    fun delete(peerId: Int): Flow<State<Int>>

    fun changePinState(peerId: Int, pin: Boolean): Flow<State<Int>>

    suspend fun storeConversations(conversations: List<VkConversation>)
}
