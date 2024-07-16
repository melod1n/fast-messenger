package dev.meloda.fast.data.api.account

import dev.meloda.fast.data.State
import dev.meloda.fast.data.mapToState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AccountUseCaseImpl(
    private val repository: AccountRepository
) : AccountUseCase {

    override suspend fun setOnline(
        voip: Boolean,
        accessToken: String
    ): Flow<State<Int>> = flow {
        emit(State.Loading)

        val newState = repository.setOnline(voip = voip).mapToState()
        emit(newState)
    }

    override suspend fun setOffline(
        accessToken: String
    ): Flow<State<Int>> = flow {
        emit(State.Loading)

        val newState = repository.setOffline().mapToState()
        emit(newState)
    }
}
