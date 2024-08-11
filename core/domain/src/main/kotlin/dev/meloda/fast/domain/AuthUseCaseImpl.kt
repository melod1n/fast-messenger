package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.auth.AuthRepository
import dev.meloda.fast.data.mapToState
import dev.meloda.fast.model.api.responses.ValidatePhoneResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthUseCaseImpl(private val repository: AuthRepository) : AuthUseCase {

    override fun validatePhone(validationSid: String): Flow<State<ValidatePhoneResponse>> = flow {
        emit(State.Loading)

        val newState = repository.validatePhone(validationSid).mapToState()
        emit(newState)
    }
}
