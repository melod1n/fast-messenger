package com.meloda.fast.data.conversations

import com.meloda.fast.api.model.data.VkConversation
import com.meloda.fast.api.network.conversations.ConversationsDeleteRequest
import com.meloda.fast.api.network.conversations.ConversationsGetRequest
import com.meloda.fast.api.network.conversations.ConversationsPinRequest
import com.meloda.fast.api.network.conversations.ConversationsUnpinRequest

class ConversationsRepository(
    private val conversationsApi: ConversationsApi,
    private val conversationsDao: ConversationsDao
) {

    suspend fun get(params: ConversationsGetRequest) = conversationsApi.get(params.map)

    suspend fun delete(params: ConversationsDeleteRequest) = conversationsApi.delete(params.map)

    suspend fun pin(params: ConversationsPinRequest) = conversationsApi.pin(params.map)

    suspend fun unpin(params: ConversationsUnpinRequest) = conversationsApi.unpin(params.map)

    suspend fun store(conversations: List<VkConversation>) = conversationsDao.insert(conversations)

}
