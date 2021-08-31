package com.meloda.fast.api.network.repo

import com.meloda.fast.api.network.Answer
import com.meloda.fast.api.network.VKUrls
import com.meloda.fast.api.network.response.GetConversationsResponse
import retrofit2.http.*

interface ConversationsRepo {

    @FormUrlEncoded
    @POST(VKUrls.Conversations.get)
    suspend fun getAllChats(
        @Field("user_id") chatId: Int,
        @Field("token") token: String
    ): Answer<GetConversationsResponse>


}