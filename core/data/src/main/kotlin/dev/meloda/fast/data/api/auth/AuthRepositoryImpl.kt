package dev.meloda.fast.data.api.auth

import dev.meloda.fast.model.api.responses.ValidatePhoneResponse
import dev.meloda.fast.network.RestApiErrorDomain
import dev.meloda.fast.network.mapApiDefault
import dev.meloda.fast.network.service.auth.AuthService
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
