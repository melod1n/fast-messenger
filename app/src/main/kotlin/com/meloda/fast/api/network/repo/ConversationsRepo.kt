package com.meloda.fast.api.network.repo

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.network.Answer
import com.meloda.fast.api.network.VKUrls
import com.meloda.fast.api.network.response.ConversationsGetResponse
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ConversationsRepo {

    @FormUrlEncoded
    @POST(VKUrls.Conversations.get)
    suspend fun getAllChats(@FieldMap params: Map<String, String>): Answer<ApiResponse<ConversationsGetResponse>>

}