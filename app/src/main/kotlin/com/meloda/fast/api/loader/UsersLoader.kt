package com.meloda.fast.api.loader

import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.network.repo.UsersRepo
import com.meloda.fast.api.network.request.UsersGetRequest
import javax.inject.Inject

class UsersLoader : Loader<VkUser>() {

    @Inject
    lateinit var repo: UsersRepo

    suspend fun load(
        usersIds: List<Int>,
        fields: String = ""
    ) = load(
        mutableMapOf(
            "usersIds" to usersIds.joinToString { it.toString() },
            "fields" to fields
        )
    )

    override suspend fun load(params: MutableMap<String, Any>): List<VkUser> {
        val usersIds: String = params["usersIds"] as String
        val fields: String = params["fields"] as String

        val users = repo.getById(
            UsersGetRequest(
                usersIds = usersIds.split(",").map { it.toInt() },
                fields = fields
            )
        )

        return emptyList()
    }

    override suspend fun loadSingle(params: MutableMap<String, Any>): VkUser {
        return load(params)[0]
    }

}