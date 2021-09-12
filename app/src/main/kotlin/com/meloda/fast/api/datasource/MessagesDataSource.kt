package com.meloda.fast.api.datasource

import com.meloda.fast.api.network.repo.MessagesRepo
import com.meloda.fast.api.network.request.MessagesGetHistoryRequest
import com.meloda.fast.api.network.request.MessagesSendRequest
import com.meloda.fast.database.dao.MessagesDao
import javax.inject.Inject

class MessagesDataSource @Inject constructor(
    private val repo: MessagesRepo,
    private val dao: MessagesDao
) {

    suspend fun getHistory(params: MessagesGetHistoryRequest) = repo.getHistory(params.map)

    suspend fun send(params: MessagesSendRequest) = repo.send(params.map)

}