package com.meloda.fast.screens.messages.data.service

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.base.RestApiError
import com.meloda.fast.api.model.data.VkChatData
import com.meloda.fast.api.model.data.VkMessageData
import com.meloda.fast.api.network.messages.MessagesGetByIdResponse
import com.meloda.fast.api.network.messages.MessagesGetConversationMembersResponse
import com.meloda.fast.api.network.messages.MessagesGetHistoryResponse
import com.meloda.fast.api.network.messages.MessagesUrls
import com.slack.eithernet.ApiResult
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface MessagesService {

    @FormUrlEncoded
    @POST(MessagesUrls.GetHistory)
    suspend fun getHistory(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<MessagesGetHistoryResponse>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.Send)
    suspend fun send(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<Int>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.MarkAsImportant)
    suspend fun markAsImportant(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<List<Int>>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.Pin)
    suspend fun pin(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<VkMessageData>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.Unpin)
    suspend fun unpin(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<Unit>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.Delete)
    suspend fun delete(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<Unit>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.Edit)
    suspend fun edit(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<Int>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.GetById)
    suspend fun getById(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<MessagesGetByIdResponse>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.MarkAsRead)
    suspend fun markAsRead(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<Int>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.GetChat)
    suspend fun getChat(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<VkChatData>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.GetConversationMembers)
    suspend fun getConversationMembers(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<MessagesGetConversationMembersResponse>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.RemoveChatUser)
    suspend fun removeChatUser(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<Int>, RestApiError>
}
