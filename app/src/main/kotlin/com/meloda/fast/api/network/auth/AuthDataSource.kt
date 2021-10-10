package com.meloda.fast.api.network.auth

import javax.inject.Inject

class AuthDataSource @Inject constructor(
    private val repo: AuthRepo
) {

    suspend fun auth(params: RequestAuthDirect) = repo.auth(params.map)

    suspend fun sendSms(validationSid: String) = repo.sendSms(validationSid)

}