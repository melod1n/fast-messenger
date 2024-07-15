package com.meloda.app.fast.data.api.auth

import com.meloda.app.fast.model.api.responses.ValidatePhoneResponse
import com.meloda.app.fast.network.RestApiErrorDomain
import com.slack.eithernet.ApiResult

interface AuthRepository {

    suspend fun validatePhone(
        validationSid: String
    ): ApiResult<ValidatePhoneResponse, RestApiErrorDomain>
}
