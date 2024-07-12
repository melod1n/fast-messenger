package com.meloda.app.fast.data.api.users

import com.meloda.app.fast.model.api.domain.VkUser
import com.meloda.app.fast.network.RestApiErrorDomain
import com.slack.eithernet.ApiResult

interface UsersRepository {

    suspend fun get(
        userIds: List<Int>?,
        fields: String?,
        nomCase: String?
    ): ApiResult<List<VkUser>, RestApiErrorDomain>

    suspend fun getLocalUsers(userIds: List<Int>): List<VkUser>

    suspend fun storeUsers(users: List<VkUser>)
}
