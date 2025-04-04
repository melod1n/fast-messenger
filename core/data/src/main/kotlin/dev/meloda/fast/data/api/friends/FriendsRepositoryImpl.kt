package dev.meloda.fast.data.api.friends

import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.data.VkMemoryCache
import dev.meloda.fast.database.dao.UserDao
import dev.meloda.fast.model.FriendsInfo
import dev.meloda.fast.model.api.data.VkUserData
import dev.meloda.fast.model.api.domain.VkUser
import dev.meloda.fast.model.api.domain.asEntity
import dev.meloda.fast.model.api.requests.GetFriendsRequest
import dev.meloda.fast.model.api.requests.GetOnlineFriendsRequest
import dev.meloda.fast.network.RestApiErrorDomain
import dev.meloda.fast.network.mapApiDefault
import dev.meloda.fast.network.mapApiResult
import dev.meloda.fast.network.service.friends.FriendsService
import com.slack.eithernet.ApiResult
import com.slack.eithernet.successOrElse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class FriendsRepositoryImpl(
    private val service: FriendsService,
    private val dao: UserDao
) : FriendsRepository {

    override suspend fun getAllFriends(
        order: String,
        count: Int?,
        offset: Int?
    ): ApiResult<FriendsInfo, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val friends = async { getFriends(order, count, offset) }.await()
            .successOrElse { failure ->
                return@withContext failure
            }

        val onlineFriends = async { getOnlineFriends(count, offset) }.await()
            .successOrElse { failure ->
                return@withContext failure
            }

        ApiResult.success(FriendsInfo(friends, onlineFriends))
    }

    override suspend fun getFriends(
        order: String,
        count: Int?,
        offset: Int?
    ): ApiResult<List<VkUser>, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = GetFriendsRequest(
            order = order,
            count = count,
            offset = offset,
            fields = VkConstants.USER_FIELDS
        )
        service.getFriends(requestModel.map).mapApiResult(
            successMapper = { apiResponse ->
                val response = apiResponse.requireResponse()
                val users = response.items.map(VkUserData::mapToDomain)

                VkMemoryCache.appendUsers(users)

                users
            },
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun getOnlineFriends(
        count: Int?,
        offset: Int?
    ): ApiResult<List<Long>, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = GetOnlineFriendsRequest(
            order = "hints",
            count = count,
            offset = offset,
        )

        service.getOnlineFriends(requestModel.map).mapApiDefault()
    }

    override suspend fun storeUsers(users: List<VkUser>) = withContext(Dispatchers.IO) {
        dao.insertAll(users.map(VkUser::asEntity))
    }
}
