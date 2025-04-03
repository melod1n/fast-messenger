package dev.meloda.fast.data.api.conversations

import com.slack.eithernet.ApiResult
import dev.meloda.fast.model.ConversationsFilter
import dev.meloda.fast.model.api.domain.VkConversation
import dev.meloda.fast.network.RestApiErrorDomain

interface ConversationsRepository {

    suspend fun storeConversations(conversations: List<VkConversation>)

    suspend fun getConversations(
        count: Int?,
        offset: Int?,
        filter: ConversationsFilter
    ): ApiResult<List<VkConversation>, RestApiErrorDomain>

    suspend fun getConversationsById(
        peerIds: List<Long>,
        extended: Boolean? = null,
        fields: String? = null
    ): ApiResult<List<VkConversation>, RestApiErrorDomain>

    suspend fun delete(peerId: Long): ApiResult<Long, RestApiErrorDomain>
    suspend fun pin(peerId: Long): ApiResult<Int, RestApiErrorDomain>
    suspend fun unpin(peerId: Long): ApiResult<Int, RestApiErrorDomain>
    suspend fun reorderPinned(peerIds: List<Long>): ApiResult<Int, RestApiErrorDomain>
    suspend fun archive(peerId: Long): ApiResult<Int, RestApiErrorDomain>
    suspend fun unarchive(peerId: Long): ApiResult<Int, RestApiErrorDomain>
}
