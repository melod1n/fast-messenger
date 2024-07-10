package com.meloda.app.fast.network.service.account

import com.meloda.app.fast.network.ApiResponse
import com.meloda.app.fast.network.RestApiError
import com.slack.eithernet.ApiResult
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface AccountService {

    @GET(AccountUrls.SET_ONLINE)
    suspend fun setOnline(
        @QueryMap params: Map<String, String>
    ): ApiResult<ApiResponse<Any>, RestApiError>

    @POST(AccountUrls.SET_OFFLINE)
    suspend fun setOffline(
        @QueryMap params: Map<String, String>
    ): ApiResult<ApiResponse<Any>, RestApiError>
}
