package com.meloda.fast.screens.conversations.domain.repository

import com.meloda.fast.api.base.RestApiError
import com.meloda.fast.api.network.conversations.ConversationsDeleteRequest
import com.meloda.fast.api.network.conversations.ConversationsGetRequest
import com.meloda.fast.api.network.conversations.ConversationsGetResponse
import com.meloda.fast.base.RestApiErrorDomain
import com.slack.eithernet.ApiResult

interface ConversationsRepository {

    suspend fun getConversations(
        params: ConversationsGetRequest
    ): ApiResult<ConversationsGetResponse, RestApiErrorDomain>

    suspend fun delete(
        params: ConversationsDeleteRequest
    ): ApiResult<Unit, RestApiErrorDomain>
}
