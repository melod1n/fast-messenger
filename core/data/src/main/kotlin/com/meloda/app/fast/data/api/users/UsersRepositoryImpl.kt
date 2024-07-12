package com.meloda.app.fast.data.api.users

import com.meloda.app.fast.data.VkMemoryCache
import com.meloda.app.fast.database.dao.UsersDao
import com.meloda.app.fast.model.api.data.VkUserData
import com.meloda.app.fast.model.api.domain.VkUser
import com.meloda.app.fast.model.api.domain.asEntity
import com.meloda.app.fast.model.api.requests.UsersGetRequest
import com.meloda.app.fast.model.database.VkUserEntity
import com.meloda.app.fast.model.database.asExternalModel
import com.meloda.app.fast.network.RestApiErrorDomain
import com.meloda.app.fast.network.mapApiResult
import com.meloda.app.fast.network.service.users.UsersService
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UsersRepositoryImpl(
    private val service: UsersService,
    private val dao: UsersDao
) : UsersRepository {

    override suspend fun get(
        userIds: List<Int>?,
        fields: String?,
        nomCase: String?
    ): ApiResult<List<VkUser>, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = UsersGetRequest(
            userIds = userIds,
            fields = fields,
            nomCase = nomCase
        )

        service.get(requestModel.map).mapApiResult(
            successMapper = { apiResponse ->
                val response = apiResponse.requireResponse()

                val users = response.map(VkUserData::mapToDomain)

                launch { storeUsers(users) }

                VkMemoryCache.appendUsers(users)

                users
            },
            errorMapper = { error ->
                error?.toDomain()
            }
        )
    }

    override suspend fun getLocalUsers(
        userIds: List<Int>
    ): List<VkUser> = withContext(Dispatchers.IO) {
        dao.getAllByIds(userIds).map(VkUserEntity::asExternalModel)
    }

    override suspend fun storeUsers(users: List<VkUser>) {
        dao.insertAll(users.map(VkUser::asEntity))
    }
}
