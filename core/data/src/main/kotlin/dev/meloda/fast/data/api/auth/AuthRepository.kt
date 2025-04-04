package dev.meloda.fast.data.api.auth

import com.slack.eithernet.ApiResult
import dev.meloda.fast.model.api.responses.ExchangeSilentTokenResponse
import dev.meloda.fast.model.api.responses.GetAnonymTokenResponse
import dev.meloda.fast.model.api.responses.GetExchangeTokenResponse
import dev.meloda.fast.model.api.responses.ValidatePhoneResponse
import dev.meloda.fast.network.RestApiErrorDomain

interface AuthRepository {

    suspend fun logout(): ApiResult<Int, RestApiErrorDomain>

    suspend fun validatePhone(
        validationSid: String
    ): ApiResult<ValidatePhoneResponse, RestApiErrorDomain>

    suspend fun getAnonymToken(
        clientId: String,
        clientSecret: String
    ): ApiResult<GetAnonymTokenResponse, RestApiErrorDomain>

    suspend fun exchangeSilentToken(
        anonymToken: String,
        silentToken: String,
        silentUuid: String
    ): ApiResult<ExchangeSilentTokenResponse, RestApiErrorDomain>

    suspend fun getExchangeToken(
        accessToken: String
    ): ApiResult<GetExchangeTokenResponse, RestApiErrorDomain>
}
