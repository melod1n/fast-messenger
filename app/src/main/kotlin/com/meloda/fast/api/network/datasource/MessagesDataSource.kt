package com.meloda.fast.api.network.datasource

import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.request.MessagesGetHistoryRequest
import com.meloda.fast.api.model.request.MessagesGetLongPollServerRequest
import com.meloda.fast.api.model.request.MessagesMarkAsImportantRequest
import com.meloda.fast.api.model.request.MessagesSendRequest
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

    suspend fun storeMessages(messages: List<VkMessage>) = dao.insert(messages)

    suspend fun getCachedMessages(peerId: Int) = dao.getByPeerId(peerId)

}