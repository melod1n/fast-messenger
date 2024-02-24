package com.meloda.fast.data.users

import com.meloda.fast.api.model.VkUserDomain
import com.meloda.fast.api.network.users.UsersGetRequest

class UsersRepository(
    private val usersApi: UsersApi,
) {

    suspend fun getById(params: UsersGetRequest) = usersApi.getById(params.map)

    suspend fun storeUsers(users: List<VkUserDomain>) {
        // TODO: 17/12/2023, Danil Nikolaev: implement
    }
}
