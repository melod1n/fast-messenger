package com.meloda.app.fast.data.api.friends

import com.meloda.app.fast.common.VkConstants
import com.meloda.app.fast.data.VkMemoryCache
import com.meloda.app.fast.database.dao.UsersDao
import com.meloda.app.fast.model.FriendsInfo
import com.meloda.app.fast.model.api.data.VkUserData
import com.meloda.app.fast.model.api.domain.VkUser
import com.meloda.app.fast.model.api.domain.asEntity
import com.meloda.app.fast.model.api.requests.GetFriendsRequest
import com.meloda.app.fast.model.api.requests.GetOnlineFriendsRequest
import com.meloda.app.fast.network.RestApiErrorDomain
import com.meloda.app.fast.network.mapApiDefault
import com.meloda.app.fast.network.mapApiResult
import com.meloda.app.fast.network.service.friends.FriendsService
import com.slack.eithernet.ApiResult
import com.slack.eithernet.successOrElse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class FriendsRepositoryImpl(
    private val service: FriendsService,
    private val dao: UsersDao
) : FriendsRepository {

    override suspend fun getAllFriends(
        count: Int?,
        offset: Int?
    ): ApiResult<FriendsInfo, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val friends = async { getFriends(count, offset) }.await()
            .successOrElse { failure ->
                return@withContext failure
            }

        val onlineFriends = async { getOnlineFriends(count, offset) }.await()
            .successOrElse { failure ->
                return@withContext failure
            }.mapNotNull { userId -> friends.find { it.id == userId } }

        ApiResult.success(FriendsInfo(friends, onlineFriends))
    }

    override suspend fun getFriends(
        count: Int?,
        offset: Int?
    ): ApiResult<List<VkUser>, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = GetFriendsRequest(
            order = "hints",
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
    ): ApiResult<List<Int>, RestApiErrorDomain> = withContext(Dispatchers.IO) {
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
