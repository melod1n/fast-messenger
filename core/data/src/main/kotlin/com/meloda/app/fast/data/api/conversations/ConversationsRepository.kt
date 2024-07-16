package dev.meloda.fast.data.api.conversations

import dev.meloda.fast.model.api.domain.VkConversation
import dev.meloda.fast.network.RestApiErrorDomain
import com.slack.eithernet.ApiResult

interface ConversationsRepository {

    suspend fun getConversations(
        count: Int?,
        offset: Int?
    ): ApiResult<List<VkConversation>, RestApiErrorDomain>

    suspend fun storeConversations(conversations: List<VkConversation>)
    suspend fun delete(peerId: Int): ApiResult<Int, RestApiErrorDomain>
    suspend fun pin(peerId: Int): ApiResult<Int, RestApiErrorDomain>
    suspend fun unpin(peerId: Int): ApiResult<Int, RestApiErrorDomain>
}
