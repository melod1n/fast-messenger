package com.meloda.fast.data.auth

import com.meloda.fast.api.network.auth.AuthDirectRequest

class AuthRepository(
    private val authApi: AuthApi
) {

    suspend fun auth(params: AuthDirectRequest) = authApi.auth(params.map)

    suspend fun sendSms(validationSid: String) = authApi.sendSms(validationSid)
}
