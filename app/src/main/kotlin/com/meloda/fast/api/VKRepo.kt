package com.meloda.fast.api

import com.meloda.fast.api.model.response.GetConversationsResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface VKRepo {

    @FormUrlEncoded
    @POST(VKUrls.getConversations)
    suspend fun getAllChats(
        @Field("user_id") chatId: Int,
        @Field("token") token: String
    ): Answer<GetConversationsResponse>

}