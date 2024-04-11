package com.meloda.fast.modules.auth.model.domain.repository

import com.meloda.fast.api.network.auth.SendSmsResponse
import com.meloda.fast.base.RestApiErrorDomain
import com.slack.eithernet.ApiResult

interface AuthRepository {

    suspend fun sendSms(
        validationSid: String
    ): ApiResult<SendSmsResponse, RestApiErrorDomain>
}
