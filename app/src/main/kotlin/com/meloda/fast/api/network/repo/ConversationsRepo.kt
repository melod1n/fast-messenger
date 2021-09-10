package com.meloda.fast.api.network.repo

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.network.Answer
import com.meloda.fast.api.network.VKUrls
import com.meloda.fast.api.network.request.ConversationsGetRequest
import com.meloda.fast.api.network.response.ConversationsGetResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ConversationsRepo {

    @POST(VKUrls.Conversations.get)
    suspend fun getAllChats(@Body params: ConversationsGetRequest): Answer<ApiResponse<ConversationsGetResponse>>

}