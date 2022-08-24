package com.meloda.fast.data.users

import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.network.users.UsersGetRequest

class UsersRepository(
    private val usersApi: UsersApi,
    private val usersDao: UsersDao
) {

    suspend fun getById(params: UsersGetRequest) = usersApi.getById(params.map)

    suspend fun storeUsers(users: List<VkUser>) {
        usersDao.insert(users)
    }

}