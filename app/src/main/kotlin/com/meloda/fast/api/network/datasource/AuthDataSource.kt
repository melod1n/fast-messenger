package com.meloda.fast.api.network.datasource

import com.meloda.fast.api.network.repo.AuthRepo
import javax.inject.Inject

class AuthDataSource @Inject constructor(
    private val repo: AuthRepo
) : AuthRepo {
    override suspend fun auth(param: Map<String, String?>) = repo.auth(param)
}