package com.meloda.fast.api.service

import com.meloda.fast.api.model.ApiResponse
import com.meloda.fast.api.model.request.RequestMessagesGetConversations
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface MessagesService {

    @GET("messages.getConversations")
    suspend fun getConversations(@QueryMap params: RequestMessagesGetConversations): ApiResponse<Map<String, Any>>

}