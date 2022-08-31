package com.meloda.fast.data.messages

import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.network.longpoll.LongPollGetUpdatesRequest
import com.meloda.fast.api.network.messages.*
import com.meloda.fast.data.longpoll.LongPollApi

class MessagesRepository(
    private val messagesApi: MessagesApi,
    private val messagesDao: MessagesDao,
    private val longPollApi: LongPollApi
) {

    suspend fun store(messages: List<VkMessage>) = messagesDao.insert(messages)

    suspend fun getCached(peerId: Int) = messagesDao.getByPeerId(peerId)

    suspend fun getHistory(params: MessagesGetHistoryRequest) =
        messagesApi.getHistory(params.map)

    suspend fun send(params: MessagesSendRequest) =
        messagesApi.send(params.map)

    suspend fun markAsImportant(params: MessagesMarkAsImportantRequest) =
        messagesApi.markAsImportant(params.map)

    suspend fun getLongPollServer(params: MessagesGetLongPollServerRequest) =
        messagesApi.getLongPollServer(params.map)

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
        params: LongPollGetUpdatesRequest
    ) = longPollApi.getResponse(serverUrl, params.map)

    suspend fun getById(params: MessagesGetByIdRequest) =
        messagesApi.getById(params.map)

    suspend fun markAsRead(
        peerId: Int,
        messagesIds: List<Int>? = null,
        startMessageId: Int? = null
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
        fields: String? = null
    ) = messagesApi.getChat(MessagesGetChatRequest(chatId, fields).map)

}