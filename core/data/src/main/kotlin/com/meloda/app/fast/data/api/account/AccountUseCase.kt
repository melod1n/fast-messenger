package com.meloda.app.fast.data.api.account

import com.meloda.app.fast.data.State
import kotlinx.coroutines.flow.Flow

interface AccountUseCase {

    suspend fun setOnline(
        voip: Boolean,
        accessToken: String
    ): Flow<State<Unit>>

    suspend fun setOffline(
        accessToken: String
    ): Flow<State<Unit>>
}
