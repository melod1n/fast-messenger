package com.meloda.fast.data.account.data.usecase

import com.meloda.fast.api.network.account.AccountSetOfflineRequest
import com.meloda.fast.api.network.account.AccountSetOnlineRequest
import com.meloda.fast.base.State
import com.meloda.fast.base.toStateApiError
import com.meloda.fast.data.account.domain.repository.AccountRepository
import com.meloda.fast.data.account.domain.usecase.AccountUseCase
import com.meloda.fast.database.dao.AccountsDao
import com.meloda.fast.model.AppAccount
import com.slack.eithernet.fold
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AccountUseCaseImpl(
    private val accountRepository: AccountRepository,
    private val accountsDao: AccountsDao
) : AccountUseCase {

    override suspend fun setOnline(
        voip: Boolean,
        accessToken: String
    ): Flow<State<Unit>> = flow {
        emit(State.Loading)

        val newState = accountRepository.setOnline(
            params = AccountSetOnlineRequest(
                voip = voip,
                accessToken = accessToken
            )
        ).fold(
            onSuccess = { response -> State.Success(response) },
            onNetworkFailure = { State.Error.ConnectionError },
            onUnknownFailure = { State.UNKNOWN_ERROR },
            onHttpFailure = { result -> result.error.toStateApiError() },
            onApiFailure = { result -> result.error.toStateApiError() }
        )
        emit(newState)
    }

    override suspend fun setOffline(
        accessToken: String
    ): Flow<State<Unit>> = flow {
        emit(State.Loading)

        val newState = accountRepository.setOffline(
            params = AccountSetOfflineRequest(accessToken = accessToken)
        ).fold(
            onSuccess = { response -> State.Success(response) },
            onNetworkFailure = { State.Error.ConnectionError },
            onUnknownFailure = { State.UNKNOWN_ERROR },
            onHttpFailure = { result -> result.error.toStateApiError() },
            onApiFailure = { result -> result.error.toStateApiError() }
        )
        emit(newState)
    }

    override suspend fun getAllAccounts(): Flow<List<AppAccount>> = flow {
        emit(accountsDao.getAll())
    }
}
