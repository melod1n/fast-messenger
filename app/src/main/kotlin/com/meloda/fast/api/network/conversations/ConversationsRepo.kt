package com.meloda.fast.api.network.conversations

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.network.Answer
import com.meloda.fast.api.network.VkUrls
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ConversationsRepo {

    @FormUrlEncoded
    @POST(VkUrls.Conversations.Get)
    suspend fun get(@FieldMap params: Map<String, String>): Answer<ApiResponse<ConversationsGetResponse>>

    @FormUrlEncoded
    @POST(VkUrls.Conversations.Delete)
    suspend fun delete(@FieldMap params: Map<String, String>): Answer<ApiResponse<Any>>

    @FormUrlEncoded
    @POST(VkUrls.Conversations.Pin)
    suspend fun pin(@FieldMap params: Map<String, String>): Answer<ApiResponse<Any>>

    @FormUrlEncoded
    @POST(VkUrls.Conversations.Unpin)
    suspend fun unpin(@FieldMap params: Map<String, String>): Answer<ApiResponse<Any>>

    @FormUrlEncoded
    @POST(VkUrls.Conversations.ReorderPinned)
    suspend fun reorderPinned(@FieldMap params: Map<String, String>): Answer<ApiResponse<Any>>

}