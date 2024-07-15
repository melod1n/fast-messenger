package com.meloda.app.fast.data.api.account

import com.meloda.app.fast.network.RestApiErrorDomain
import com.slack.eithernet.ApiResult

interface AccountRepository {

    suspend fun setOnline(
        accessToken: String? = null,
        voip: Boolean = false
    ): ApiResult<Int, RestApiErrorDomain>

    suspend fun setOffline(
        accessToken: String? = null
    ): ApiResult<Int, RestApiErrorDomain>

    suspend fun registerDevice(
        token: String,
        deviceId: String
    ): ApiResult<Int, RestApiErrorDomain>
}
