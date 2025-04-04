package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.auth.AuthRepository
import dev.meloda.fast.data.mapToState
import dev.meloda.fast.model.api.responses.ExchangeSilentTokenResponse
import dev.meloda.fast.model.api.responses.GetAnonymTokenResponse
import dev.meloda.fast.model.api.responses.GetExchangeTokenResponse
import dev.meloda.fast.model.api.responses.ValidatePhoneResponse
import kotlinx.coroutines.flow.Flow

class AuthUseCaseImpl(private val repository: AuthRepository) : AuthUseCase {

    override fun logout(): Flow<State<Int>> = flowNewState { repository.logout().mapToState() }

    override fun validatePhone(validationSid: String): Flow<State<ValidatePhoneResponse>> =
        flowNewState { repository.validatePhone(validationSid = validationSid).mapToState() }

    override suspend fun getAnonymToken(
        clientId: String,
        clientSecret: String
    ): Flow<State<GetAnonymTokenResponse>> = flowNewState {
        repository.getAnonymToken(
            clientId = clientId,
            clientSecret = clientSecret
        ).mapToState()
    }

    override suspend fun exchangeSilentToken(
        anonymToken: String,
        silentToken: String,
        silentUuid: String
    ): Flow<State<ExchangeSilentTokenResponse>> = flowNewState {
        repository.exchangeSilentToken(
            anonymToken = anonymToken,
            silentToken = silentToken,
            silentUuid = silentUuid
        ).mapToState()
    }

    override suspend fun getExchangeToken(
        accessToken: String
    ): Flow<State<GetExchangeTokenResponse>> = flowNewState {
        repository.getExchangeToken(accessToken = accessToken).mapToState()
    }
}
