package com.meloda.fast.api.datasource

import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.network.repo.UsersRepo
import com.meloda.fast.api.network.request.UsersGetRequest
import com.meloda.fast.database.dao.UsersDao
import javax.inject.Inject

class UsersDataSource @Inject constructor(
    private val repo: UsersRepo,
    private val dao: UsersDao
) {

    suspend fun getById(params: UsersGetRequest) = repo.getById(params.map)

    suspend fun storeUsers(users: List<VkUser>) = dao.insert(users)

}