package com.meloda.fast.data.users.data

import com.meloda.fast.api.model.data.VkUserData
import com.meloda.fast.api.network.users.UsersGetRequest
import com.meloda.fast.base.RestApiErrorDomain
import com.meloda.fast.base.mapResult
import com.meloda.fast.data.users.UsersService
import com.meloda.fast.data.users.domain.UsersRepository
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsersRepositoryImpl(
    private val usersService: UsersService
) : UsersRepository {

    override suspend fun getById(
        params: UsersGetRequest
    ): ApiResult<List<VkUserData>, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        usersService.getById(params.map).mapResult(
            successMapper = { response -> response.requireResponse() },
            errorMapper = { error -> error?.toDomain() }
        )
    }
}
