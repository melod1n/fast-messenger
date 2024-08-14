package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.users.UsersRepository
import dev.meloda.fast.model.api.domain.VkUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetLocalUserByIdUseCase(private val repository: UsersRepository) {

    operator fun invoke(userId: Int): Flow<State<VkUser?>> = flow {
        emit(State.Loading)

        val newState = kotlin.runCatching {
            repository.getLocalUsers(userIds = listOf(userId)).singleOrNull()
        }.fold(
            onSuccess = { user -> State.Success(user) },
            onFailure = { State.Error.InternalError }
        )

        emit(newState)
    }
}
