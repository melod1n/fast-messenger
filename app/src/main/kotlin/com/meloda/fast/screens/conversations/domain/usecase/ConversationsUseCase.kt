package com.meloda.fast.screens.conversations.domain.usecase

import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.api.model.domain.VkGroupDomain
import com.meloda.fast.api.model.domain.VkMessageDomain
import com.meloda.fast.api.model.domain.VkUserDomain
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

    suspend fun storeConversations(conversations: List<VkConversationDomain>)
    suspend fun storeGroups(groups: List<VkGroupDomain>)
    suspend fun storeMessages(messages: List<VkMessageDomain>)
}
