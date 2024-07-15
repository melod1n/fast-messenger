package com.meloda.app.fast.data.api.auth

import com.meloda.app.fast.model.api.responses.ValidatePhoneResponse
import com.meloda.app.fast.network.RestApiErrorDomain
import com.meloda.app.fast.network.mapApiDefault
import com.meloda.app.fast.network.service.auth.AuthService
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val service: AuthService
) : AuthRepository {

    override suspend fun validatePhone(
        validationSid: String
    ): ApiResult<ValidatePhoneResponse, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        service.validatePhone(validationSid).mapApiDefault()
    }
}
