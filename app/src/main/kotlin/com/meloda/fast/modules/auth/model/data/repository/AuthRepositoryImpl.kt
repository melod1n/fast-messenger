package com.meloda.fast.modules.auth.model.data.repository

import com.meloda.fast.api.network.auth.SendSmsResponse
import com.meloda.fast.base.RestApiErrorDomain
import com.meloda.fast.base.mapResult
import com.meloda.fast.modules.auth.model.data.service.AuthService
import com.meloda.fast.modules.auth.model.domain.repository.AuthRepository
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val authService: AuthService
) : AuthRepository {

    override suspend fun sendSms(
        validationSid: String
    ): ApiResult<SendSmsResponse, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        authService.sendSms(validationSid).mapResult(
            successMapper = { response -> response.requireResponse() },
            errorMapper = { error -> error?.toDomain() }
        )
    }
}
