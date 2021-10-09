package com.meloda.fast.api.network.datasource

import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.request.*
import com.meloda.fast.api.network.repo.MessagesRepo
import com.meloda.fast.database.dao.MessagesDao
import javax.inject.Inject

class MessagesDataSource @Inject constructor(
    private val repo: MessagesRepo,
    private val dao: MessagesDao
) {

    suspend fun getHistory(params: MessagesGetHistoryRequest) =
        repo.getHistory(params.map)

    suspend fun send(params: MessagesSendRequest) =
        repo.send(params.map)

    suspend fun markAsImportant(params: MessagesMarkAsImportantRequest) =
        repo.markAsImportant(params.map)

    suspend fun getLongPollServer(params: MessagesGetLongPollServerRequest) =
        repo.getLongPollServer(params.map)

    suspend fun pin(params: MessagesPinMessageRequest) =
        repo.pin(params.map)

    suspend fun unpin(params: MessagesUnPinMessageRequest) =
        repo.unpin(params.map)

    suspend fun delete(params: MessagesDeleteRequest) =
        repo.delete(params.map)

    suspend fun store(messages: List<VkMessage>) = dao.insert(messages)

    suspend fun getCached(peerId: Int) = dao.getByPeerId(peerId)

}