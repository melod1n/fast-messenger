package com.meloda.app.fast.data.api.friends

import com.meloda.app.fast.model.api.domain.VkUser
import com.meloda.app.fast.network.RestApiErrorDomain
import com.slack.eithernet.ApiResult

interface FriendsRepository {

    suspend fun getFriends(
        count: Int?,
        offset: Int?
    ): ApiResult<List<VkUser>, RestApiErrorDomain>

    suspend fun storeUsers(users: List<VkUser>)
}
