package com.meloda.fast.data.users.domain

import com.meloda.fast.api.model.data.VkUserData
import com.meloda.fast.api.network.users.UsersGetRequest
import com.meloda.fast.base.RestApiErrorDomain
import com.slack.eithernet.ApiResult

interface UsersRepository {
    suspend fun getById(params: UsersGetRequest): ApiResult<List<VkUserData>, RestApiErrorDomain>
}
