package com.meloda.app.fast.network.service.conversations

import com.meloda.app.fast.model.api.responses.ConversationsGetResponse
import com.meloda.app.fast.network.ApiResponse
import com.meloda.app.fast.network.RestApiError
import com.slack.eithernet.ApiResult
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
    @POST(ConversationsUrls.DELETE)
    suspend fun delete(
        @FieldMap params: Map<String, String>
    ): ApiResult<Unit, RestApiError>

    @FormUrlEncoded
    @POST(ConversationsUrls.PIN)
    suspend fun pin(@FieldMap params: Map<String, String>): ApiResult<Unit, RestApiError>

    @FormUrlEncoded
    @POST(ConversationsUrls.UNPIN)
    suspend fun unpin(@FieldMap params: Map<String, String>): ApiResult<Unit, RestApiError>

    @FormUrlEncoded
    @POST(ConversationsUrls.REORDER_PINNED)
    suspend fun reorderPinned(@FieldMap params: Map<String, String>): ApiResult<Unit, RestApiError>
}