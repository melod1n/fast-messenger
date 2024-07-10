package com.meloda.app.fast.data.api.friends

import com.meloda.app.fast.model.FriendsInfo
import com.meloda.app.fast.model.api.domain.VkUser
import com.meloda.app.fast.network.RestApiErrorDomain
import com.slack.eithernet.ApiResult

interface FriendsRepository {

    suspend fun getAllFriends(
        count: Int?,
        offset: Int?
    ): ApiResult<FriendsInfo, RestApiErrorDomain>

    suspend fun getFriends(
        count: Int?,
        offset: Int?
    ): ApiResult<List<VkUser>, RestApiErrorDomain>

    suspend fun getOnlineFriends(
        count: Int?,
        offset: Int?
    ): ApiResult<List<Int>, RestApiErrorDomain>

    suspend fun storeUsers(users: List<VkUser>)
}
