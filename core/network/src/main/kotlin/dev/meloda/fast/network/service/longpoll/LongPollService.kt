package dev.meloda.fast.network.service.longpoll

import dev.meloda.fast.model.api.data.LongPollUpdates
import dev.meloda.fast.network.RestApiError
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
