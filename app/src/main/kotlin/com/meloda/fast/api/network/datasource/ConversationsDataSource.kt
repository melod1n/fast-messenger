package com.meloda.fast.api.network.datasource

import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.network.repo.ConversationsRepo
import com.meloda.fast.api.model.request.ConversationsGetRequest
import com.meloda.fast.database.dao.ConversationsDao
import javax.inject.Inject

class ConversationsDataSource @Inject constructor(
    private val repo: ConversationsRepo,
    private val dao: ConversationsDao
) {

    suspend fun getAllChats(params: ConversationsGetRequest) = repo.getAllChats(params.map)

    suspend fun storeConversations(conversations: List<VkConversation>) = dao.insert(conversations)

}