package com.meloda.fast.api.network.messages

import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.network.longpoll.LongPollGetUpdatesRequest
import com.meloda.fast.api.network.longpoll.LongPollRepo
import com.meloda.fast.database.dao.MessagesDao
import javax.inject.Inject

class MessagesDataSource @Inject constructor(
    private val messagesRepo: MessagesRepo,
    private val messagesDao: MessagesDao,
    private val longPollRepo: LongPollRepo
) {

    suspend fun store(messages: List<VkMessage>) = messagesDao.insert(messages)

    suspend fun getCached(peerId: Int) = messagesDao.getByPeerId(peerId)

    suspend fun getHistory(params: MessagesGetHistoryRequest) =
        messagesRepo.getHistory(params.map)

    suspend fun send(params: MessagesSendRequest) =
        messagesRepo.send(params.map)

    suspend fun markAsImportant(params: MessagesMarkAsImportantRequest) =
        messagesRepo.markAsImportant(params.map)

    suspend fun getLongPollServer(params: MessagesGetLongPollServerRequest) =
        messagesRepo.getLongPollServer(params.map)

    suspend fun pin(params: MessagesPinMessageRequest) =
        messagesRepo.pin(params.map)

    suspend fun unpin(params: MessagesUnPinMessageRequest) =
        messagesRepo.unpin(params.map)

    suspend fun delete(params: MessagesDeleteRequest) =
        messagesRepo.delete(params.map)

    suspend fun edit(params: MessagesEditRequest) =
        messagesRepo.edit(params.map)

    suspend fun getLongPollUpdates(
        serverUrl: String,
        params: LongPollGetUpdatesRequest
    ) = longPollRepo.getResponse(serverUrl, params.map)

    suspend fun getById(params: MessagesGetByIdRequest) =
        messagesRepo.getById(params.map)
}