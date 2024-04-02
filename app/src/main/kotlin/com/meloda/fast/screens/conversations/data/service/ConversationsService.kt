package com.meloda.fast.screens.conversations.data.service

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.base.RestApiError
import com.meloda.fast.api.network.conversations.ConversationsGetResponse
import com.meloda.fast.api.network.conversations.ConversationsUrls
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
