package dev.meloda.fast.data.api.users

import com.slack.eithernet.ApiResult
import dev.meloda.fast.data.VkMemoryCache
import dev.meloda.fast.database.dao.UsersDao
import dev.meloda.fast.model.api.data.VkUserData
import dev.meloda.fast.model.api.domain.VkUser
import dev.meloda.fast.model.api.domain.asEntity
import dev.meloda.fast.model.api.requests.UsersGetRequest
import dev.meloda.fast.model.database.VkUserEntity
import dev.meloda.fast.model.database.asExternalModel
import dev.meloda.fast.network.RestApiErrorDomain
import dev.meloda.fast.network.mapApiResult
import dev.meloda.fast.network.service.users.UsersService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UsersRepositoryImpl(
    private val service: UsersService,
    private val dao: UsersDao
) : UsersRepository {

    override suspend fun get(
        userIds: List<Long>?,
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
        userIds: List<Long>
    ): List<VkUser> = withContext(Dispatchers.IO) {
        dao.getAllByIds(userIds).map(VkUserEntity::asExternalModel)
    }

    override suspend fun storeUsers(users: List<VkUser>) {
        dao.insertAll(users.map(VkUser::asEntity))
    }
}
