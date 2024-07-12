package com.meloda.app.fast.data.api.users

import com.meloda.app.fast.data.State
import com.meloda.app.fast.data.mapToState
import com.meloda.app.fast.model.api.domain.VkUser
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
