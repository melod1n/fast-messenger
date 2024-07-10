package com.meloda.app.fast.auth.twofa

import com.meloda.app.fast.data.State
import com.meloda.app.fast.data.api.auth.AuthRepository
import com.meloda.app.fast.model.api.responses.SendSmsResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthUseCaseImpl(
    private val authRepository: AuthRepository
) : AuthUseCase {

    // TODO: 05/05/2024, Danil Nikolaev: implement
    override fun sendSms(validationSid: String): Flow<State<SendSmsResponse>> = flow {
//        emit(State.Loading)
//
//        val newState = authRepository.sendSms(validationSid)
//            .fold(
//                onSuccess = { response -> State.Success(response) },
//                onNetworkFailure = { State.Error.ConnectionError },
//                onUnknownFailure = { State.UNKNOWN_ERROR },
//                onHttpFailure = { result -> result.error.toStateApiError() },
//                onApiFailure = { result -> result.error.toStateApiError() }
//            )
//        emit(newState)
    }
}
