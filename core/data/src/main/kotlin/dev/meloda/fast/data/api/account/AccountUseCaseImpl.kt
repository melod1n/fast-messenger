package dev.meloda.fast.data.api.account

import dev.meloda.fast.data.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// TODO: 05/05/2024, Danil Nikolaev: implement
class AccountUseCaseImpl(
    private val accountRepository: dev.meloda.fast.data.api.account.AccountRepository
) : dev.meloda.fast.data.api.account.AccountUseCase {

    override suspend fun setOnline(
        voip: Boolean,
        accessToken: String
    ): Flow<State<Unit>> = flow {
//        emit(dev.meloda.fast.data.State.Loading)
//
//        val newState = accountRepository.setOnline(
//            params = AccountSetOnlineRequest(
//                voip = voip,
//                accessToken = accessToken
//            )
//        ).fold(
//            onSuccess = { response -> dev.meloda.fast.data.State.Success(response) },
//            onNetworkFailure = { dev.meloda.fast.data.State.Error.ConnectionError },
//            onUnknownFailure = { dev.meloda.fast.data.State.UNKNOWN_ERROR },
//            onHttpFailure = { result -> result.error.toStateApiError() },
//            onApiFailure = { result -> result.error.toStateApiError() }
//        )
//        emit(newState)
    }

    override suspend fun setOffline(
        accessToken: String
    ): Flow<dev.meloda.fast.data.State<Unit>> = flow {
        emit(dev.meloda.fast.data.State.Loading)

//        val newState = accountRepository.setOffline(
//            params = AccountSetOfflineRequest(accessToken = accessToken)
//        ).fold(
//            onSuccess = { response -> dev.meloda.fast.data.State.Success(response) },
//            onNetworkFailure = { dev.meloda.fast.data.State.Error.ConnectionError },
//            onUnknownFailure = { dev.meloda.fast.data.State.UNKNOWN_ERROR },
//            onHttpFailure = { result -> result.error.toStateApiError() },
//            onApiFailure = { result -> result.error.toStateApiError() }
//        )
//        emit(newState)
    }
}
