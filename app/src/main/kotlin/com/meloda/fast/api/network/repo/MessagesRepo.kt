package com.meloda.fast.api.network.repo

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.model.base.BaseVkLongPoll
import com.meloda.fast.api.model.base.BaseVkMessage
import com.meloda.fast.api.model.response.MessagesGetHistoryResponse
import com.meloda.fast.api.network.Answer
import com.meloda.fast.api.network.VkUrls
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface MessagesRepo {

    @FormUrlEncoded
    @POST(VkUrls.Messages.GetHistory)
    suspend fun getHistory(@FieldMap params: Map<String, String>): Answer<ApiResponse<MessagesGetHistoryResponse>>

    @FormUrlEncoded
    @POST(VkUrls.Messages.Send)
    suspend fun send(@FieldMap params: Map<String, String>): Answer<ApiResponse<Int>>

    @FormUrlEncoded
    @POST(VkUrls.Messages.MarkAsImportant)
    suspend fun markAsImportant(@FieldMap params: Map<String, String>): Answer<ApiResponse<List<Int>>>

    @FormUrlEncoded
    @POST(VkUrls.Messages.GetLongPollServer)
    suspend fun getLongPollServer(@FieldMap params: Map<String, String>): Answer<ApiResponse<BaseVkLongPoll>>

    @FormUrlEncoded
    @POST(VkUrls.Messages.Pin)
    suspend fun pin(@FieldMap params: Map<String, String>): Answer<ApiResponse<BaseVkMessage>>

    @FormUrlEncoded
    @POST(VkUrls.Messages.Unpin)
    suspend fun unpin(@FieldMap params: Map<String, String>): Answer<ApiResponse<Any>>

    @FormUrlEncoded
    @POST(VkUrls.Messages.Delete)
    suspend fun delete(@FieldMap params: Map<String, String>): Answer<ApiResponse<Any>>

}