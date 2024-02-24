package com.meloda.fast.screens.conversations.data.repository

import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.api.network.conversations.ConversationsDeleteRequest
import com.meloda.fast.api.network.conversations.ConversationsGetRequest
import com.meloda.fast.api.network.conversations.ConversationsGetResponse
import com.meloda.fast.api.network.conversations.ConversationsPinRequest
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

    suspend fun pin(params: ConversationsPinRequest) = conversationsService.pin(params.map)

    suspend fun unpin(params: ConversationsUnpinRequest) = conversationsService.unpin(params.map)

    suspend fun store(conversations: List<VkConversationDomain>) {
        // TODO: 17/12/2023, Danil Nikolaev: implement
    }

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
}
