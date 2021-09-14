package com.meloda.fast.api.network.repo

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.model.response.MessagesGetHistoryResponse
import com.meloda.fast.api.network.Answer
import com.meloda.fast.api.network.VKUrls
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface MessagesRepo {

    @FormUrlEncoded
    @POST(VKUrls.Messages.GetHistory)
    suspend fun getHistory(@FieldMap params: Map<String, String>): Answer<ApiResponse<MessagesGetHistoryResponse>>

    @FormUrlEncoded
    @POST(VKUrls.Messages.Send)
    suspend fun send(@FieldMap params: Map<String, String>): Answer<ApiResponse<Int>>

    @FormUrlEncoded
    @POST(VKUrls.Messages.MarkAsImportant)
    suspend fun markAsImportant(@FieldMap params: Map<String, String>): Answer<ApiResponse<List<Int>>>

}