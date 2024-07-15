package com.meloda.app.fast.auth.validation

import com.meloda.app.fast.data.State
import com.meloda.app.fast.data.api.auth.AuthRepository
import com.meloda.app.fast.data.mapToState
import com.meloda.app.fast.model.api.responses.ValidatePhoneResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthUseCaseImpl(private val repository: AuthRepository) : AuthUseCase {

    override fun validatePhone(validationSid: String): Flow<State<ValidatePhoneResponse>> = flow {
        emit(State.Loading)

        val newState = repository.validatePhone(validationSid).mapToState()
        emit(newState)
    }
}
