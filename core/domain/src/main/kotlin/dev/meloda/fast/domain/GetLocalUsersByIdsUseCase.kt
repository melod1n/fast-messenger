package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.users.UsersRepository
import dev.meloda.fast.model.api.domain.VkUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetLocalUsersByIdsUseCase(private val repository: UsersRepository) {

    operator fun invoke(userIds: List<Long>): Flow<State<List<VkUser>>> = flow {
        emit(State.Loading)

        val newState = kotlin.runCatching {
            repository.getLocalUsers(userIds = userIds)
        }.fold(
            onSuccess = { user -> State.Success(user) },
            onFailure = { State.Error.InternalError }
        )

        emit(newState)
    }
}
