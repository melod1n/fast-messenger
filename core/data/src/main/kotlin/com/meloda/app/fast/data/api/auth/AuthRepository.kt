package com.meloda.app.fast.data.api.auth

import com.meloda.app.fast.model.api.requests.AuthDirectRequest
import com.meloda.app.fast.model.api.responses.AuthDirectResponse
import com.meloda.app.fast.model.api.responses.SendSmsResponse
import com.meloda.app.fast.network.OAuthErrorDomain
import com.slack.eithernet.ApiResult

interface AuthRepository {

//    suspend fun auth(
//        params: AuthDirectRequest
//    ): ApiResult<AuthDirectResponse, OAuthErrorDomain>

    suspend fun sendSms(
        validationSid: String
    ): SendSmsResponse
}
