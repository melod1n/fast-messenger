package com.meloda.app.fast.data.api.users

import com.meloda.app.fast.model.api.data.VkUserData
import com.meloda.app.fast.model.api.requests.UsersGetRequest

interface UsersRepository {
    suspend fun getById(params: UsersGetRequest): List<VkUserData>
}
