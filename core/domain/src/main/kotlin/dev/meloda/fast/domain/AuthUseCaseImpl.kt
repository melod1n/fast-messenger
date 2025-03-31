package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.auth.AuthRepository
import dev.meloda.fast.data.mapToState
import dev.meloda.fast.model.api.responses.ExchangeSilentTokenResponse
import dev.meloda.fast.model.api.responses.GetAnonymTokenResponse
import dev.meloda.fast.model.api.responses.GetExchangeTokenResponse
import dev.meloda.fast.model.api.responses.ValidatePhoneResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthUseCaseImpl(private val repository: AuthRepository) : AuthUseCase {

    override fun validatePhone(validationSid: String): Flow<State<ValidatePhoneResponse>> = flow {
        emit(State.Loading)
        val newState = repository.validatePhone(validationSid).mapToState()
        emit(newState)
    }

    override suspend fun getAnonymToken(
        clientId: String,
        clientSecret: String
    ): Flow<State<GetAnonymTokenResponse>> = flow {
        emit(State.Loading)

        val newState = repository.getAnonymToken(
            clientId = clientId,
            clientSecret = clientSecret
        ).mapToState()

        emit(newState)
    }

    override suspend fun exchangeSilentToken(
        anonymToken: String,
        silentToken: String,
        silentUuid: String
    ): Flow<State<ExchangeSilentTokenResponse>> = flow {
        emit(State.Loading)

        val newState = repository.exchangeSilentToken(
            anonymToken = anonymToken,
            silentToken = silentToken,
            silentUuid = silentUuid
        ).mapToState()

        emit(newState)
    }

    override suspend fun getExchangeToken(
        accessToken: String
    ): Flow<State<GetExchangeTokenResponse>> = flow {
        emit(State.Loading)

        val newState = repository.getExchangeToken(
            accessToken = accessToken
        ).mapToState()

        emit(newState)
    }
}
