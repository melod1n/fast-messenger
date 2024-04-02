package com.meloda.fast.screens.conversations.domain.usecase

import com.meloda.fast.api.network.conversations.ConversationsResponseDomain
import com.meloda.fast.base.State
import kotlinx.coroutines.flow.Flow

interface ConversationsUseCase {

    fun getConversations(
        count: Int? = null,
        offset: Int? = null,
        fields: String = "",
        filter: String = "all",
        extended: Boolean? = true,
        startMessageId: Int? = null
    ): Flow<State<ConversationsResponseDomain>>

    fun delete(peerId: Int): Flow<State<Unit>>

    fun pin(peerId: Int): Flow<State<Unit>>

    fun unpin(peerId: Int): Flow<State<Unit>>
}
