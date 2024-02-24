package com.meloda.fast.data.longpoll

import com.meloda.fast.api.base.RestApiError
import com.slack.eithernet.ApiResult
import retrofit2.http.GET
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface LongPollApi {

    @GET
    suspend fun getResponse(
        @Url serverUrl: String,
        @QueryMap params: Map<String, String>
    ): ApiResult<LongPollUpdates, RestApiError>
}
