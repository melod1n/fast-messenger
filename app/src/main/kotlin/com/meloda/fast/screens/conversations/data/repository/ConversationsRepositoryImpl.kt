package com.meloda.fast.screens.conversations.data.repository

import com.meloda.fast.api.network.conversations.ConversationsDeleteRequest
import com.meloda.fast.api.network.conversations.ConversationsGetRequest
import com.meloda.fast.api.network.conversations.ConversationsGetResponse
import com.meloda.fast.api.network.conversations.ConversationsPinRequest
import com.meloda.fast.api.network.conversations.ConversationsReorderPinnedRequest
import com.meloda.fast.api.network.conversations.ConversationsUnpinRequest
import com.meloda.fast.base.RestApiErrorDomain
import com.meloda.fast.base.mapResult
import com.meloda.fast.screens.conversations.data.service.ConversationsService
import com.meloda.fast.screens.conversations.domain.repository.ConversationsRepository
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConversationsRepositoryImpl(
    private val conversationsService: ConversationsService,
) : ConversationsRepository {

    override suspend fun getConversations(
        params: ConversationsGetRequest
    ): ApiResult<ConversationsGetResponse, RestApiErrorDomain> =
        withContext(Dispatchers.IO) {
            conversationsService.getConversations(params.map).mapResult(
                successMapper = { response -> response.requireResponse() },
                errorMapper = { error -> error?.toDomain() }
            )
        }


    override suspend fun delete(
        params: ConversationsDeleteRequest
    ): ApiResult<Unit, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        conversationsService.delete(params.map).mapResult(
            successMapper = {},
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun pin(
        params: ConversationsPinRequest
    ): ApiResult<Unit, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        conversationsService.pin(params.map).mapResult(
            successMapper = {},
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun unpin(
        params: ConversationsUnpinRequest
    ): ApiResult<Unit, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        conversationsService.unpin(params.map).mapResult(
            successMapper = {},
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun reorderPinned(
        params: ConversationsReorderPinnedRequest
    ): ApiResult<Unit, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        conversationsService.reorderPinned(params.map).mapResult(
            successMapper = {},
            errorMapper = { error -> error?.toDomain() }
        )
    }
}
