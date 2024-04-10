package com.meloda.fast.data.account.domain.usecase

import com.meloda.fast.base.State
import com.meloda.fast.model.AppAccount
import kotlinx.coroutines.flow.Flow

interface AccountUseCase {

    suspend fun setOnline(
        voip: Boolean,
        accessToken: String
    ): Flow<State<Unit>>

    suspend fun setOffline(
        accessToken: String
    ): Flow<State<Unit>>

    suspend fun getAllAccounts(): Flow<List<AppAccount>>
}
