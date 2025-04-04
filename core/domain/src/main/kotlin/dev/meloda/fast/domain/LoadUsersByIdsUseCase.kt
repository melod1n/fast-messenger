package dev.meloda.fast.domain

import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.users.UsersRepository
import dev.meloda.fast.data.mapToState
import dev.meloda.fast.model.api.domain.VkUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LoadUsersByIdsUseCase(private val repository: UsersRepository) {

    operator fun invoke(
        userIds: List<Long>?,
        fields: String = VkConstants.USER_FIELDS,
        nomCase: String? = null
    ): Flow<State<List<VkUser>>> = flow {
        emit(State.Loading)

        val newState = repository.get(
            userIds = userIds,
            fields = fields,
            nomCase = nomCase
        ).mapToState()

        emit(newState)
    }
}
