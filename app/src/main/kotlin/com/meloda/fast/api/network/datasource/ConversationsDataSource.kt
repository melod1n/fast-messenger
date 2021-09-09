package com.meloda.fast.api.network.datasource

import com.meloda.fast.api.network.repo.ConversationsRepo
import com.meloda.fast.api.network.request.ConversationsGetRequest
import javax.inject.Inject

class ConversationsDataSource @Inject constructor(
    private val repo: ConversationsRepo
) : ConversationsRepo {

    override suspend fun getAllChats(param: ConversationsGetRequest) = repo.getAllChats(param)

}