package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.model.api.responses.ExchangeSilentTokenResponse
import dev.meloda.fast.model.api.responses.GetAnonymTokenResponse
import dev.meloda.fast.model.api.responses.GetExchangeTokenResponse
import dev.meloda.fast.model.api.responses.ValidatePhoneResponse
import kotlinx.coroutines.flow.Flow

interface AuthUseCase {

    fun validatePhone(
        validationSid: String
    ): Flow<State<ValidatePhoneResponse>>

    suspend fun getAnonymToken(
        clientId: String,
        clientSecret: String
    ): Flow<State<GetAnonymTokenResponse>>

    suspend fun exchangeSilentToken(
        anonymToken: String,
        silentToken: String,
        silentUuid: String
    ): Flow<State<ExchangeSilentTokenResponse>>

    suspend fun getExchangeToken(
        accessToken: String
    ): Flow<State<GetExchangeTokenResponse>>
}
