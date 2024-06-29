package com.meloda.app.fast.data.api.conversations

import com.meloda.app.fast.model.api.domain.VkConversation
import com.meloda.app.fast.network.RestApiErrorDomain
import com.slack.eithernet.ApiResult

interface ConversationsRepository {

    suspend fun getConversations(
        count: Int?,
        offset: Int?
    ): ApiResult<List<VkConversation>, RestApiErrorDomain>

    suspend fun storeConversations(conversations: List<VkConversation>)
    suspend fun delete(peerId: Int): ApiResult<Boolean, RestApiErrorDomain>
    suspend fun pin(peerId: Int): ApiResult<Boolean, RestApiErrorDomain>
    suspend fun unpin(peerId: Int): ApiResult<Boolean, RestApiErrorDomain>
}
