package com.meloda.fast.data.account.domain.repository

import com.meloda.fast.api.network.account.AccountSetOfflineRequest
import com.meloda.fast.api.network.account.AccountSetOnlineRequest
import com.meloda.fast.base.RestApiErrorDomain
import com.slack.eithernet.ApiResult

interface AccountRepository {

    suspend fun setOnline(
        params: AccountSetOnlineRequest
    ): ApiResult<Unit, RestApiErrorDomain>

    suspend fun setOffline(
        params: AccountSetOfflineRequest
    ): ApiResult<Unit, RestApiErrorDomain>
}
