package com.meloda.app.fast.network.service.longpoll

import com.meloda.app.fast.model.api.data.LongPollUpdates
import com.meloda.app.fast.network.RestApiError
import com.slack.eithernet.ApiResult
import retrofit2.http.GET
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface LongPollService {

    @GET
    suspend fun getResponse(
        @Url serverUrl: String,
        @QueryMap params: Map<String, String>
    ): ApiResult<LongPollUpdates, RestApiError>
}
