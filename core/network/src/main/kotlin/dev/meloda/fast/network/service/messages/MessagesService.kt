package dev.meloda.fast.network.service.messages

import com.slack.eithernet.ApiResult
import dev.meloda.fast.model.api.data.VkLongPollData
import dev.meloda.fast.model.api.data.VkMessageData
import dev.meloda.fast.model.api.responses.MessagesCreateChatResponse
import dev.meloda.fast.model.api.responses.MessagesGetByIdResponse
import dev.meloda.fast.model.api.responses.MessagesGetHistoryAttachmentsResponse
import dev.meloda.fast.model.api.responses.MessagesGetHistoryResponse
import dev.meloda.fast.network.ApiResponse
import dev.meloda.fast.network.RestApiError
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface MessagesService {

    @FormUrlEncoded
    @POST(MessagesUrls.GET_HISTORY)
    suspend fun getHistory(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<MessagesGetHistoryResponse>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.GET_BY_ID)
    suspend fun getById(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<MessagesGetByIdResponse>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.SEND)
    suspend fun send(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<Int>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.GET_LONG_POLL_SERVER)
    suspend fun getLongPollServer(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<VkLongPollData>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.MARK_AS_READ)
    suspend fun markAsRead(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<Int>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.GET_HISTORY_ATTACHMENTS)
    suspend fun getHistoryAttachments(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<MessagesGetHistoryAttachmentsResponse>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.CREATE_CHAT)
    suspend fun createChat(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<MessagesCreateChatResponse>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.PIN)
    suspend fun pin(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<VkMessageData>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.UNPIN)
    suspend fun unpin(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<Int>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.MARK_AS_IMPORTANT)
    suspend fun markAsImportant(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<List<Int>>, RestApiError>

    @FormUrlEncoded
    @POST(MessagesUrls.DELETE)
    suspend fun delete(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<List<Any>>, RestApiError>
//
//    @FormUrlEncoded
//    @POST(MessagesUrls.Edit)
//    suspend fun edit(
//        @FieldMap params: Map<String, String>
//    ): ApiResult<ApiResponse<Int>, RestApiError>
//
//
//    @FormUrlEncoded
//    @POST(MessagesUrls.GetChat)
//    suspend fun getChat(
//        @FieldMap params: Map<String, String>
//    ): ApiResult<ApiResponse<VkChatData>, RestApiError>
//
//    @FormUrlEncoded
//    @POST(MessagesUrls.GetConversationMembers)
//    suspend fun getConversationMembers(
//        @FieldMap params: Map<String, String>
//    ): ApiResult<ApiResponse<MessagesGetConversationMembersResponse>, RestApiError>
//
//    @FormUrlEncoded
//    @POST(MessagesUrls.RemoveChatUser)
//    suspend fun removeChatUser(
//        @FieldMap params: Map<String, String>
//    ): ApiResult<ApiResponse<Int>, RestApiError>
}
