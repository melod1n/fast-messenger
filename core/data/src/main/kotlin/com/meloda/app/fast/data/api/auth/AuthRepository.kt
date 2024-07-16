package dev.meloda.fast.data.api.auth

import dev.meloda.fast.model.api.responses.ValidatePhoneResponse
import dev.meloda.fast.network.RestApiErrorDomain
import com.slack.eithernet.ApiResult

interface AuthRepository {

    suspend fun validatePhone(
        validationSid: String
    ): ApiResult<ValidatePhoneResponse, RestApiErrorDomain>
}
