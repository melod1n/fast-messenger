package dev.meloda.fast.data.api.friends

import dev.meloda.fast.model.FriendsInfo
import dev.meloda.fast.model.api.domain.VkUser
import dev.meloda.fast.network.RestApiErrorDomain
import com.slack.eithernet.ApiResult

interface FriendsRepository {

    suspend fun getAllFriends(
        order: String,
        count: Int?,
        offset: Int?
    ): ApiResult<FriendsInfo, RestApiErrorDomain>

    suspend fun getFriends(
        order: String,
        count: Int?,
        offset: Int?
    ): ApiResult<List<VkUser>, RestApiErrorDomain>

    suspend fun getOnlineFriends(
        count: Int?,
        offset: Int?
    ): ApiResult<List<Long>, RestApiErrorDomain>

    suspend fun storeUsers(users: List<VkUser>)
}
