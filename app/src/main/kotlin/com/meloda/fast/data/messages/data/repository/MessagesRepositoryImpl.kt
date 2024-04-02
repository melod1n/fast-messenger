package com.meloda.fast.data.messages.data.repository

import com.meloda.fast.api.model.data.VkChatData
import com.meloda.fast.api.model.data.VkLongPollData
import com.meloda.fast.api.model.data.VkMessageData
import com.meloda.fast.api.network.longpoll.LongPollGetUpdatesRequest
import com.meloda.fast.api.network.messages.MessagesDeleteRequest
import com.meloda.fast.api.network.messages.MessagesEditRequest
import com.meloda.fast.api.network.messages.MessagesGetByIdRequest
import com.meloda.fast.api.network.messages.MessagesGetByIdResponse
import com.meloda.fast.api.network.messages.MessagesGetConversationMembersResponse
import com.meloda.fast.api.network.messages.MessagesGetHistoryRequest
import com.meloda.fast.api.network.messages.MessagesGetHistoryResponse
import com.meloda.fast.api.network.messages.MessagesGetLongPollServerRequest
import com.meloda.fast.api.network.messages.MessagesMarkAsImportantRequest
import com.meloda.fast.api.network.messages.MessagesPinMessageRequest
import com.meloda.fast.api.network.messages.MessagesSendRequest
import com.meloda.fast.api.network.messages.MessagesUnPinMessageRequest
import com.meloda.fast.base.RestApiErrorDomain
import com.meloda.fast.base.mapResult
import com.meloda.fast.data.longpoll.LongPollService
import com.meloda.fast.data.longpoll.LongPollUpdates
import com.meloda.fast.data.messages.data.service.MessagesService
import com.meloda.fast.data.messages.domain.repository.MessagesRepository
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessagesRepositoryImpl(
    private val messagesService: MessagesService,
    private val longPollService: LongPollService,
) : MessagesRepository {

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
        TODO("Not yet implemented")
    }

    override suspend fun pin(params: MessagesPinMessageRequest): ApiResult<VkMessageData, RestApiErrorDomain> {
        TODO("Not yet implemented")
    }

    override suspend fun unpin(params: MessagesUnPinMessageRequest): ApiResult<Unit, RestApiErrorDomain> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(params: MessagesDeleteRequest): ApiResult<Unit, RestApiErrorDomain> {
        TODO("Not yet implemented")
    }

    override suspend fun edit(params: MessagesEditRequest): ApiResult<Int, RestApiErrorDomain> {
        TODO("Not yet implemented")
    }

    override suspend fun getLongPollServer(
        params: MessagesGetLongPollServerRequest
    ): ApiResult<VkLongPollData, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        messagesService.getLongPollServer(params.map).mapResult(
            successMapper = { response -> response.requireResponse() },
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun getLongPollUpdates(
        serverUrl: String,
        params: LongPollGetUpdatesRequest
    ): ApiResult<LongPollUpdates, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        longPollService.getResponse(serverUrl, params.map).mapResult(
            successMapper = { response -> response },
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
        peerId: Int,
        messageIds: List<Int>?,
        startMessageId: Int?
    ): ApiResult<Int, RestApiErrorDomain> {
        TODO("Not yet implemented")
    }

    override suspend fun getChat(
        chatId: Int,
        fields: String?
    ): ApiResult<VkChatData, RestApiErrorDomain> {
        TODO("Not yet implemented")
    }

    override suspend fun getConversationMembers(
        peerId: Int,
        offset: Int?,
        count: Int?,
        extended: Boolean?,
        fields: String?
    ): ApiResult<MessagesGetConversationMembersResponse, RestApiErrorDomain> {
        TODO("Not yet implemented")
    }

    override suspend fun removeChatUser(
        chatId: Int,
        memberId: Int
    ): ApiResult<Int, RestApiErrorDomain> {
        TODO("Not yet implemented")
    }
}
