package com.meloda.app.fast.data.api.auth

import com.meloda.app.fast.model.api.requests.AuthDirectRequest
import com.meloda.app.fast.model.api.responses.AuthDirectResponse
import com.meloda.app.fast.model.api.responses.SendSmsResponse
import com.meloda.app.fast.network.OAuthErrorDomain
import com.meloda.app.fast.network.service.auth.AuthService
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val authService: AuthService
) : AuthRepository {

//    override suspend fun auth(
//        params: AuthDirectRequest
//    ): ApiResult<AuthDirectResponse, OAuthErrorDomain> {
//
//    }

    // TODO: 05/05/2024, Danil Nikolaev: implement
    override suspend fun sendSms(
        validationSid: String
    ): SendSmsResponse = withContext(Dispatchers.IO) {
        SendSmsResponse(
            validationSid = null, delay = null, validationType = null, validationResend = null

        )
//        authService.sendSms(validationSid).mapResult(
//            successMapper = { response -> response.requireResponse() },
//            errorMapper = { error -> error?.toDomain() }
//        )
    }
}
