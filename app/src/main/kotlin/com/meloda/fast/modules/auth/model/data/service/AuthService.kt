package com.meloda.fast.modules.auth.model.data.service

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.base.RestApiError
import com.meloda.fast.api.network.auth.AuthUrls
import com.meloda.fast.api.network.auth.SendSmsResponse
import com.slack.eithernet.ApiResult
import retrofit2.http.GET
import retrofit2.http.Query

interface AuthService {

    @GET(AuthUrls.SEND_SMS)
    suspend fun sendSms(@Query("sid") validationSid: String): ApiResult<ApiResponse<SendSmsResponse>, RestApiError>
}
