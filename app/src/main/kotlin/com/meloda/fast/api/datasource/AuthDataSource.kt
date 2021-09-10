package com.meloda.fast.api.datasource

import com.meloda.fast.api.network.repo.AuthRepo
import com.meloda.fast.api.network.request.RequestAuthDirect
import javax.inject.Inject

class AuthDataSource @Inject constructor(
    private val repo: AuthRepo
) {

    suspend fun auth(params: RequestAuthDirect) = repo.auth(params.map)

    suspend fun sendSms(validationSid: String) = repo.sendSms(validationSid)

}