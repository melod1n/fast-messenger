package com.meloda.app.fast.data.api.users

import com.meloda.app.fast.data.State
import com.meloda.app.fast.model.api.domain.VkUserDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


// TODO: 05/05/2024, Danil Nikolaev: implement
class UsersUseCaseImpl(
    private val usersRepository: UsersRepository,
) : UsersUseCase {

    override fun getUserById(
        userId: Int,
        fields: String?,
        nomCase: String?
    ): Flow<State<VkUserDomain?>> = flow {
//        emit(State.Loading)
//
//        val newState = usersRepository.getById(
//            UsersGetRequest(
//                userIds = listOf(userId),
//                fields = fields,
//                nomCase = nomCase
//            )
//        ).fold(
//            onSuccess = { response -> State.Success(response.singleOrNull()?.mapToDomain()) },
//            onNetworkFailure = { State.Error.ConnectionError },
//            onUnknownFailure = { State.UNKNOWN_ERROR },
//            onHttpFailure = { result -> result.error.toStateApiError() },
//            onApiFailure = { result -> result.error.toStateApiError() }
//        )
//        emit(newState)
    }

    override fun getUsersByIds(
        userIds: List<Int>,
        fields: String?,
        nomCase: String?
    ): Flow<State<List<VkUserDomain>>> = flow {
//        emit(State.Loading)
//
//        val newState = usersRepository.getById(
//            UsersGetRequest(
//                userIds = userIds,
//                fields = fields,
//                nomCase = nomCase
//            )
//        ).fold(
//            onSuccess = { response -> State.Success(response.map(VkUserData::mapToDomain)) },
//            onNetworkFailure = { State.Error.ConnectionError },
//            onUnknownFailure = { State.UNKNOWN_ERROR },
//            onHttpFailure = { result -> result.error.toStateApiError() },
//            onApiFailure = { result -> result.error.toStateApiError() }
//        )
//        emit(newState)
    }

    override suspend fun storeUser(user: VkUserDomain) {

    }

    override suspend fun storeUsers(users: List<VkUserDomain>) {

    }
}
