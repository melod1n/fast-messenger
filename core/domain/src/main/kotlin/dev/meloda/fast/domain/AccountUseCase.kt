package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import kotlinx.coroutines.flow.Flow

interface AccountUseCase {

    suspend fun setOnline(
        voip: Boolean,
        accessToken: String
    ): Flow<State<Int>>

    suspend fun setOffline(
        accessToken: String
    ): Flow<State<Int>>
}
