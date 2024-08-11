package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.users.UsersRepository
import dev.meloda.fast.data.mapToState
import dev.meloda.fast.model.api.domain.VkUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UsersUseCaseImpl(
    private val repository: UsersRepository,
) : UsersUseCase {

    override fun get(
        userIds: List<Int>?,
        fields: String?,
        nomCase: String?
    ): Flow<State<List<VkUser>>> = flow {
        emit(State.Loading)

        val newState = repository.get(userIds, fields, nomCase).mapToState()
        emit(newState)
    }

    override fun getLocalUser(userId: Int): Flow<State<VkUser?>> = flow {
        emit(State.Loading)

        val newState = kotlin.runCatching {
            repository.getLocalUsers(listOf(userId)).singleOrNull()
        }.fold(
            onSuccess = { user -> State.Success(user) },
            onFailure = { State.Error.InternalError }
        )

        emit(newState)
    }

    override suspend fun storeUser(user: VkUser) = repository.storeUsers(listOf(user))
    override suspend fun storeUsers(users: List<VkUser>) = repository.storeUsers(users)
}
