package com.meloda.fast.data.account

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.base.RestApiError
import com.meloda.fast.api.network.account.AccountUrls
import com.slack.eithernet.ApiResult
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface AccountApi {

    @GET(AccountUrls.SetOnline)
    suspend fun setOnline(@QueryMap params: Map<String, String>): ApiResult<ApiResponse<Any>, RestApiError>

    @POST(AccountUrls.SetOffline)
    suspend fun setOffline(@QueryMap params: Map<String, String>): ApiResult<ApiResponse<Any>, RestApiError>

}
