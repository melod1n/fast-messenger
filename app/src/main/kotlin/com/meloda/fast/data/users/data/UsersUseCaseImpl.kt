package com.meloda.fast.data.users.data

import com.meloda.fast.api.model.data.VkUserData
import com.meloda.fast.api.model.domain.VkUserDomain
import com.meloda.fast.api.network.users.UsersGetRequest
import com.meloda.fast.base.State
import com.meloda.fast.base.toStateApiError
import com.meloda.fast.data.users.domain.UsersRepository
import com.meloda.fast.data.users.domain.UsersUseCase
import com.meloda.fast.database.dao.UsersDao
import com.slack.eithernet.fold
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UsersUseCaseImpl(
    private val usersRepository: UsersRepository,
    private val usersDao: UsersDao
) : UsersUseCase {

    override fun getUserById(
        userId: Int,
        fields: String?,
        nomCase: String?
    ): Flow<State<VkUserDomain?>> = flow {
        emit(State.Loading)

        val newState = usersRepository.getById(
            UsersGetRequest(
                userIds = listOf(userId),
                fields = fields,
                nomCase = nomCase
            )
        ).fold(
            onSuccess = { response -> State.Success(response.singleOrNull()?.mapToDomain()) },
            onNetworkFailure = { State.Error.ConnectionError },
            onUnknownFailure = { State.UNKNOWN_ERROR },
            onHttpFailure = { result -> result.error.toStateApiError() },
            onApiFailure = { result -> result.error.toStateApiError() }
        )
        emit(newState)
    }

    override fun getUsersByIds(
        userIds: List<Int>,
        fields: String?,
        nomCase: String?
    ): Flow<State<List<VkUserDomain>>> = flow {
        emit(State.Loading)

        val newState = usersRepository.getById(
            UsersGetRequest(
                userIds = userIds,
                fields = fields,
                nomCase = nomCase
            )
        ).fold(
            onSuccess = { response -> State.Success(response.map(VkUserData::mapToDomain)) },
            onNetworkFailure = { State.Error.ConnectionError },
            onUnknownFailure = { State.UNKNOWN_ERROR },
            onHttpFailure = { result -> result.error.toStateApiError() },
            onApiFailure = { result -> result.error.toStateApiError() }
        )
        emit(newState)
    }

    override suspend fun storeUser(user: VkUserDomain) = usersDao.insert(user.mapToDB())

    override suspend fun storeUsers(users: List<VkUserDomain>) =
        usersDao.insertAll(users.map(VkUserDomain::mapToDB))
}
