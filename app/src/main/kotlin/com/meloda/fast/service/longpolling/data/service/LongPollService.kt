package com.meloda.fast.service.longpolling.data.service

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.base.RestApiError
import com.meloda.fast.api.model.data.VkLongPollData
import com.meloda.fast.api.network.messages.MessagesUrls
import com.meloda.fast.service.longpolling.data.LongPollUpdates
import com.slack.eithernet.ApiResult
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface LongPollService {

    @FormUrlEncoded
    @POST(MessagesUrls.GetLongPollServer)
    suspend fun getLongPollServer(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<VkLongPollData>, RestApiError>

    @GET
    suspend fun getResponse(
        @Url serverUrl: String,
        @QueryMap params: Map<String, String>
    ): ApiResult<LongPollUpdates, RestApiError>
}
