package com.meloda.app.fast.network.service.auth

import com.meloda.app.fast.model.api.responses.SendSmsResponse
import com.meloda.app.fast.network.ApiResponse
import com.meloda.app.fast.network.RestApiError
import com.slack.eithernet.ApiResult
import retrofit2.http.GET
import retrofit2.http.Query

interface AuthService {

    @GET(AuthUrls.SEND_SMS)
    suspend fun sendSms(@Query("sid") validationSid: String): ApiResult<ApiResponse<SendSmsResponse>, RestApiError>
}
