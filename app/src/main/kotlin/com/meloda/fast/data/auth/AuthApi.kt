package com.meloda.fast.data.auth

import com.meloda.fast.api.base.RestApiError
import com.meloda.fast.api.network.auth.AuthDirectResponse
import com.meloda.fast.api.network.auth.AuthUrls
import com.meloda.fast.api.network.auth.SendSmsResponse
import com.slack.eithernet.ApiResult
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface AuthApi {

    @GET(AuthUrls.DirectAuth)
    suspend fun auth(@QueryMap param: Map<String, String?>): ApiResult<AuthDirectResponse, RestApiError>

    @GET(AuthUrls.SendSms)
    suspend fun sendSms(@Query("sid") validationSid: String): ApiResult<SendSmsResponse, RestApiError>
}
