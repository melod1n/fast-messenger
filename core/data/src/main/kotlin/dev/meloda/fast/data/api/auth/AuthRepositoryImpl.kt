package dev.meloda.fast.data.api.auth

import com.slack.eithernet.ApiResult
import dev.meloda.fast.model.api.requests.ExchangeSilentTokenRequest
import dev.meloda.fast.model.api.requests.GetAnonymTokenRequest
import dev.meloda.fast.model.api.requests.GetExchangeTokenRequest
import dev.meloda.fast.model.api.responses.ExchangeSilentTokenResponse
import dev.meloda.fast.model.api.responses.GetAnonymTokenResponse
import dev.meloda.fast.model.api.responses.GetExchangeTokenResponse
import dev.meloda.fast.model.api.responses.ValidatePhoneResponse
import dev.meloda.fast.network.RestApiErrorDomain
import dev.meloda.fast.network.mapApiDefault
import dev.meloda.fast.network.service.auth.AuthService
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

    override suspend fun getAnonymToken(
        clientId: String,
        clientSecret: String
    ): ApiResult<GetAnonymTokenResponse, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = GetAnonymTokenRequest(
            clientId = clientId,
            clientSecret = clientSecret
        )

        service.getAnonymToken(requestModel.map).mapApiDefault()
    }

    override suspend fun exchangeSilentToken(
        anonymToken: String,
        silentToken: String,
        silentUuid: String
    ): ApiResult<ExchangeSilentTokenResponse, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = ExchangeSilentTokenRequest(
            anonymToken = anonymToken,
            silentToken = silentToken,
            silentUuid = silentUuid
        )

        service.exchangeSilentToken(requestModel.map).mapApiDefault()
    }

    override suspend fun getExchangeToken(
        accessToken: String
    ): ApiResult<GetExchangeTokenResponse, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = GetExchangeTokenRequest(accessToken = accessToken)
        service.getExchangeToken(requestModel.map).mapApiDefault()
    }
}
