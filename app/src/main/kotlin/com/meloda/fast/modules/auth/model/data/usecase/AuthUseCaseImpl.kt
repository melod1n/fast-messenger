package com.meloda.fast.modules.auth.model.data.usecase

import com.meloda.fast.api.network.auth.SendSmsResponse
import com.meloda.fast.base.State
import com.meloda.fast.base.toStateApiError
import com.meloda.fast.modules.auth.model.domain.repository.AuthRepository
import com.meloda.fast.modules.auth.model.domain.usecase.AuthUseCase
import com.slack.eithernet.fold
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthUseCaseImpl(
    private val authRepository: AuthRepository
) : AuthUseCase {

    override fun sendSms(validationSid: String): Flow<State<SendSmsResponse>> = flow {
        emit(State.Loading)

        val newState = authRepository.sendSms(validationSid)
            .fold(
                onSuccess = { response -> State.Success(response) },
                onNetworkFailure = { State.Error.ConnectionError },
                onUnknownFailure = { State.UNKNOWN_ERROR },
                onHttpFailure = { result -> result.error.toStateApiError() },
                onApiFailure = { result -> result.error.toStateApiError() }
            )
        emit(newState)
    }
}
