package com.meloda.fast.data.conversations

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.network.ApiAnswer
import com.meloda.fast.api.network.conversations.ConversationsGetResponse
import com.meloda.fast.api.network.conversations.ConversationsUrls
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ConversationsApi {

    @FormUrlEncoded
    @POST(ConversationsUrls.Get)
    suspend fun get(@FieldMap params: Map<String, String>): ApiAnswer<ApiResponse<ConversationsGetResponse>>

    @FormUrlEncoded
    @POST(ConversationsUrls.Delete)
    suspend fun delete(@FieldMap params: Map<String, String>): ApiAnswer<ApiResponse<Any>>

    @FormUrlEncoded
    @POST(ConversationsUrls.Pin)
    suspend fun pin(@FieldMap params: Map<String, String>): ApiAnswer<ApiResponse<Any>>

    @FormUrlEncoded
    @POST(ConversationsUrls.Unpin)
    suspend fun unpin(@FieldMap params: Map<String, String>): ApiAnswer<ApiResponse<Any>>

    @FormUrlEncoded
    @POST(ConversationsUrls.ReorderPinned)
    suspend fun reorderPinned(@FieldMap params: Map<String, String>): ApiAnswer<ApiResponse<Any>>

}