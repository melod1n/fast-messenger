package com.meloda.fast.api.network.messages

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.model.base.BaseVkLongPoll
import com.meloda.fast.api.model.base.BaseVkMessage
import com.meloda.fast.api.network.Answer
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface MessagesRepo {

    @FormUrlEncoded
    @POST(MessagesUrls.GetHistory)
    suspend fun getHistory(@FieldMap params: Map<String, String>): Answer<ApiResponse<MessagesGetHistoryResponse>>

    @FormUrlEncoded
    @POST(MessagesUrls.Send)
    suspend fun send(@FieldMap params: Map<String, String>): Answer<ApiResponse<Int>>

    @FormUrlEncoded
    @POST(MessagesUrls.MarkAsImportant)
    suspend fun markAsImportant(@FieldMap params: Map<String, String>): Answer<ApiResponse<List<Int>>>

    @FormUrlEncoded
    @POST(MessagesUrls.GetLongPollServer)
    suspend fun getLongPollServer(@FieldMap params: Map<String, String>): Answer<ApiResponse<BaseVkLongPoll>>

    @FormUrlEncoded
    @POST(MessagesUrls.Pin)
    suspend fun pin(@FieldMap params: Map<String, String>): Answer<ApiResponse<BaseVkMessage>>

    @FormUrlEncoded
    @POST(MessagesUrls.Unpin)
    suspend fun unpin(@FieldMap params: Map<String, String>): Answer<ApiResponse<Any>>

    @FormUrlEncoded
    @POST(MessagesUrls.Delete)
    suspend fun delete(@FieldMap params: Map<String, String>): Answer<ApiResponse<Any>>

    @FormUrlEncoded
    @POST(MessagesUrls.Edit)
    suspend fun edit(@FieldMap params: Map<String, String>): Answer<ApiResponse<Any>>

    @FormUrlEncoded
    @POST(MessagesUrls.GetById)
    suspend fun getById(@FieldMap params: Map<String, String>): Answer<ApiResponse<MessagesGetByIdResponse>>

}