package dev.meloda.fast.data.api.users

import com.slack.eithernet.ApiResult
import dev.meloda.fast.model.api.domain.VkUser
import dev.meloda.fast.network.RestApiErrorDomain

interface UsersRepository {

    suspend fun get(
        userIds: List<Long>?,
        fields: String?,
        nomCase: String?
    ): ApiResult<List<VkUser>, RestApiErrorDomain>

    suspend fun getLocalUsers(userIds: List<Long>): List<VkUser>

    suspend fun storeUsers(users: List<VkUser>)
}
