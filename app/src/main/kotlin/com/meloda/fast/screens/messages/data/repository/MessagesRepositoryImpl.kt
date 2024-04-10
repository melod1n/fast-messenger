package com.meloda.fast.screens.messages.data.repository

import com.meloda.fast.api.model.data.VkChatData
import com.meloda.fast.api.model.data.VkMessageData
import com.meloda.fast.api.network.messages.MessagesDeleteRequest
import com.meloda.fast.api.network.messages.MessagesEditRequest
import com.meloda.fast.api.network.messages.MessagesGetByIdRequest
import com.meloda.fast.api.network.messages.MessagesGetByIdResponse
import com.meloda.fast.api.network.messages.MessagesGetChatRequest
import com.meloda.fast.api.network.messages.MessagesGetConversationMembersRequest
import com.meloda.fast.api.network.messages.MessagesGetConversationMembersResponse
import com.meloda.fast.api.network.messages.MessagesGetHistoryRequest
import com.meloda.fast.api.network.messages.MessagesGetHistoryResponse
import com.meloda.fast.api.network.messages.MessagesMarkAsImportantRequest
import com.meloda.fast.api.network.messages.MessagesMarkAsReadRequest
import com.meloda.fast.api.network.messages.MessagesPinMessageRequest
import com.meloda.fast.api.network.messages.MessagesRemoveChatUserRequest
import com.meloda.fast.api.network.messages.MessagesSendRequest
import com.meloda.fast.api.network.messages.MessagesUnPinMessageRequest
import com.meloda.fast.base.RestApiErrorDomain
import com.meloda.fast.base.mapResult
import com.meloda.fast.screens.messages.data.service.MessagesService
import com.meloda.fast.screens.messages.domain.repository.MessagesRepository
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessagesRepositoryImpl(private val messagesService: MessagesService) : MessagesRepository {

    override suspend fun getHistory(
        params: MessagesGetHistoryRequest
    ): ApiResult<MessagesGetHistoryResponse, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        messagesService.getHistory(params.map).mapResult(
            successMapper = { response -> response.requireResponse() },
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun send(
        params: MessagesSendRequest
    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        messagesService.send(params.map).mapResult(
            successMapper = { response -> response.requireResponse() },
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun markAsImportant(
        params: MessagesMarkAsImportantRequest
    ): ApiResult<List<Int>, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        messagesService.markAsImportant(params.map).mapResult(
            successMapper = { response -> response.requireResponse() },
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun pin(
        params: MessagesPinMessageRequest
    ): ApiResult<VkMessageData, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        messagesService.pin(params.map).mapResult(
            successMapper = { response -> response.requireResponse() },
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun unpin(
        params: MessagesUnPinMessageRequest
    ): ApiResult<Unit, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        messagesService.unpin(params.map).mapResult(
            successMapper = {},
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun delete(
        params: MessagesDeleteRequest
    ): ApiResult<Unit, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        messagesService.delete(params.map).mapResult(
            successMapper = {},
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun edit(
        params: MessagesEditRequest
    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        messagesService.edit(params.map).mapResult(
            successMapper = { response -> response.requireResponse() },
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun getById(
        params: MessagesGetByIdRequest
    ): ApiResult<MessagesGetByIdResponse, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        messagesService.getById(params.map).mapResult(
            successMapper = { response -> response.requireResponse() },
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun markAsRead(
        params: MessagesMarkAsReadRequest
    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        messagesService.markAsRead(params.map).mapResult(
            successMapper = { response -> response.requireResponse() },
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun getChat(
        params: MessagesGetChatRequest
    ): ApiResult<VkChatData, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        messagesService.getChat(params.map).mapResult(
            successMapper = { response -> response.requireResponse() },
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun getConversationMembers(
        params: MessagesGetConversationMembersRequest
    ): ApiResult<MessagesGetConversationMembersResponse, RestApiErrorDomain> =
        withContext(Dispatchers.IO) {
            messagesService.getConversationMembers(params.map).mapResult(
                successMapper = { response -> response.requireResponse() },
                errorMapper = { error -> error?.toDomain() }
            )
        }

    override suspend fun removeChatUser(
        params: MessagesRemoveChatUserRequest
    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        messagesService.removeChatUser(params.map).mapResult(
            successMapper = { response -> response.requireResponse() },
            errorMapper = { error -> error?.toDomain() }
        )
    }
}
