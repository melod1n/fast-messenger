package com.meloda.fast.screens.conversations.domain.repository

import com.meloda.fast.api.network.conversations.ConversationsDeleteRequest
import com.meloda.fast.api.network.conversations.ConversationsGetRequest
import com.meloda.fast.api.network.conversations.ConversationsGetResponse
import com.meloda.fast.api.network.conversations.ConversationsPinRequest
import com.meloda.fast.api.network.conversations.ConversationsReorderPinnedRequest
import com.meloda.fast.api.network.conversations.ConversationsUnpinRequest
import com.meloda.fast.base.RestApiErrorDomain
import com.slack.eithernet.ApiResult

interface ConversationsRepository {

    suspend fun getConversations(
        params: ConversationsGetRequest
    ): ApiResult<ConversationsGetResponse, RestApiErrorDomain>

    suspend fun delete(
        params: ConversationsDeleteRequest
    ): ApiResult<Unit, RestApiErrorDomain>

    suspend fun pin(
        params: ConversationsPinRequest
    ): ApiResult<Unit, RestApiErrorDomain>

    suspend fun unpin(
        params: ConversationsUnpinRequest
    ): ApiResult<Unit, RestApiErrorDomain>

    suspend fun reorderPinned(
        params: ConversationsReorderPinnedRequest
    ): ApiResult<Unit, RestApiErrorDomain>
}
