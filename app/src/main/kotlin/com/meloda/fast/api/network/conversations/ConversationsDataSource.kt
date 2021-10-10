package com.meloda.fast.api.network.conversations

import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.database.dao.ConversationsDao
import javax.inject.Inject

class ConversationsDataSource @Inject constructor(
    private val repo: ConversationsRepo,
    private val dao: ConversationsDao
) {

    suspend fun get(params: ConversationsGetRequest) = repo.get(params.map)

    suspend fun delete(params: ConversationsDeleteRequest) = repo.delete(params.map)

    suspend fun pin(params: ConversationsPinRequest) = repo.pin(params.map)

    suspend fun unpin(params: ConversationsUnpinRequest) = repo.unpin(params.map)

    suspend fun store(conversations: List<VkConversation>) = dao.insert(conversations)

}