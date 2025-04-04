package dev.meloda.fast.domain

import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.users.UsersRepository
import dev.meloda.fast.data.mapToState
import dev.meloda.fast.model.api.domain.VkUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LoadUserByIdUseCase(private val repository: UsersRepository) {

    operator fun invoke(
        userId: Long?,
        fields: String = VkConstants.USER_FIELDS,
        nomCase: String? = null
    ): Flow<State<VkUser?>> = flow {
        emit(State.Loading)

        val newState = repository.get(
            userIds = userId?.let(::listOf),
            fields = fields,
            nomCase = nomCase
        ).mapToState(List<VkUser>::singleOrNull)

        emit(newState)
    }
}
