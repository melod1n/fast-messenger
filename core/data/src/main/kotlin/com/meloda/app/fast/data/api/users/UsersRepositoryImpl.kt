package com.meloda.app.fast.data.api.users

import com.meloda.app.fast.model.api.data.VkUserData
import com.meloda.app.fast.model.api.requests.UsersGetRequest
import com.meloda.app.fast.network.service.users.UsersService

class UsersRepositoryImpl(
    private val usersService: UsersService
) : UsersRepository {

    override suspend fun getById(params: UsersGetRequest): List<VkUserData> {
        // TODO: 05/05/2024, Danil Nikolaev: implement

        return emptyList()
    }
}
