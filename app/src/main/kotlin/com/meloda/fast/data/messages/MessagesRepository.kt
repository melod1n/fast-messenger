package com.meloda.fast.data.messages

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.model.domain.VkMessageDomain
import com.meloda.fast.api.model.data.VkLongPollData
import com.meloda.fast.api.network.longpoll.LongPollGetUpdatesRequest
import com.meloda.fast.api.network.messages.MessagesDeleteRequest
import com.meloda.fast.api.network.messages.MessagesEditRequest
import com.meloda.fast.api.network.messages.MessagesGetByIdRequest
import com.meloda.fast.api.network.messages.MessagesGetChatRequest
import com.meloda.fast.api.network.messages.MessagesGetConversationMembersRequest
import com.meloda.fast.api.network.messages.MessagesGetHistoryRequest
import com.meloda.fast.api.network.messages.MessagesGetLongPollServerRequest
import com.meloda.fast.api.network.messages.MessagesMarkAsImportantRequest
import com.meloda.fast.api.network.messages.MessagesPinMessageRequest
import com.meloda.fast.api.network.messages.MessagesRemoveChatUserRequest
import com.meloda.fast.api.network.messages.MessagesSendRequest
import com.meloda.fast.api.network.messages.MessagesUnPinMessageRequest
import com.meloda.fast.base.RestApiErrorDomain
import com.meloda.fast.base.mapResult
import com.meloda.fast.data.longpoll.LongPollApi
import com.meloda.fast.data.longpoll.LongPollUpdates
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessagesRepository(
    private val messagesApi: MessagesApi,
    private val longPollApi: LongPollApi,
) {

    suspend fun store(message: VkMessageDomain) = store(listOf(message))

    suspend fun store(messages: List<VkMessageDomain>) {}

    suspend fun getCached(peerId: Int) {}

    suspend fun getHistory(params: MessagesGetHistoryRequest) =
        messagesApi.getHistory(params.map)

    suspend fun send(params: MessagesSendRequest) =
        messagesApi.send(params.map)

    suspend fun markAsImportant(params: MessagesMarkAsImportantRequest) =
        messagesApi.markAsImportant(params.map)

    suspend fun getLongPollServer(
        params: MessagesGetLongPollServerRequest
    ): ApiResult<ApiResponse<VkLongPollData>, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        messagesApi.getLongPollServer(params.map).mapResult(
            successMapper = { response -> response },
            errorMapper = { error -> error?.toDomain() }
        )
    }

    suspend fun pin(params: MessagesPinMessageRequest) =
        messagesApi.pin(params.map)

    suspend fun unpin(params: MessagesUnPinMessageRequest) =
        messagesApi.unpin(params.map)

    suspend fun delete(params: MessagesDeleteRequest) =
        messagesApi.delete(params.map)

    suspend fun edit(params: MessagesEditRequest) =
        messagesApi.edit(params.map)

    suspend fun getLongPollUpdates(
        serverUrl: String,
        params: LongPollGetUpdatesRequest,
    ): ApiResult<LongPollUpdates, RestApiErrorDomain> =
        withContext(Dispatchers.IO) {
            longPollApi.getResponse(serverUrl, params.map).mapResult(
                successMapper = { response -> response },
                errorMapper = { error -> error?.toDomain() }
            )
        }

    suspend fun getById(params: MessagesGetByIdRequest) =
        messagesApi.getById(params.map)

    suspend fun markAsRead(
        peerId: Int,
        messagesIds: List<Int>? = null,
        startMessageId: Int? = null,
    ) = messagesApi.markAsRead(
        mutableMapOf("peer_id" to peerId.toString()).apply {
            messagesIds?.let {
                this["message_ids"] = messagesIds.joinToString { it.toString() }
            }
            startMessageId?.let {
                this["start_message_id"] = it.toString()
            }
        }
    )

    suspend fun getChat(
        chatId: Int,
        fields: String? = null,
    ) = messagesApi.getChat(MessagesGetChatRequest(chatId, fields).map)

    suspend fun getConversationMembers(
        peerId: Int,
        offset: Int? = null,
        count: Int? = null,
        extended: Boolean? = null,
        fields: String? = null,
    ) = messagesApi.getConversationMembers(
        MessagesGetConversationMembersRequest(
            peerId,
            offset,
            count,
            extended,
            fields
        ).map
    )

    suspend fun removeChatUser(
        chatId: Int,
        memberId: Int,
    ) = messagesApi.removeChatUser(MessagesRemoveChatUserRequest(chatId, memberId).map)

}
