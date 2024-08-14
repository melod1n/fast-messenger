package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.users.UsersRepository
import dev.meloda.fast.model.api.domain.VkUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class StoreUsersUseCase(private val repository: UsersRepository) {

    operator fun invoke(users: List<VkUser>): Flow<State<Unit>> = flow {
        emit(State.Loading)

        val newState = kotlin.runCatching {
            repository.storeUsers(users)
        }.fold(
            onSuccess = {
                State.Success(Unit)
            },
            onFailure = { error ->
                error.printStackTrace()
                State.Error.InternalError
            }
        )

        emit(newState)
    }
}
