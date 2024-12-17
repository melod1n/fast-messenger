package dev.meloda.fast.network.service.conversations

import com.slack.eithernet.ApiResult
import dev.meloda.fast.model.api.responses.ConversationsDeleteResponse
import dev.meloda.fast.model.api.responses.ConversationsGetByIdResponse
import dev.meloda.fast.model.api.responses.ConversationsGetResponse
import dev.meloda.fast.network.ApiResponse
import dev.meloda.fast.network.RestApiError
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ConversationsService {

    @FormUrlEncoded
    @POST(ConversationsUrls.GET)
    suspend fun getConversations(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<ConversationsGetResponse>, RestApiError>

    @FormUrlEncoded
    @POST(ConversationsUrls.GET_BY_ID)
    suspend fun getConversationsById(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<ConversationsGetByIdResponse>, RestApiError>

    @FormUrlEncoded
    @POST(ConversationsUrls.DELETE)
    suspend fun delete(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<ConversationsDeleteResponse>, RestApiError>

    @FormUrlEncoded
    @POST(ConversationsUrls.PIN)
    suspend fun pin(@FieldMap params: Map<String, String>): ApiResult<ApiResponse<Int>, RestApiError>

    @FormUrlEncoded
    @POST(ConversationsUrls.UNPIN)
    suspend fun unpin(@FieldMap params: Map<String, String>): ApiResult<ApiResponse<Int>, RestApiError>

    @FormUrlEncoded
    @POST(ConversationsUrls.REORDER_PINNED)
    suspend fun reorderPinned(@FieldMap params: Map<String, String>): ApiResult<Unit, RestApiError>
}
