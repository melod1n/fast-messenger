package com.meloda.fast.data.account.data.repository

import com.meloda.fast.api.network.account.AccountSetOfflineRequest
import com.meloda.fast.api.network.account.AccountSetOnlineRequest
import com.meloda.fast.base.RestApiErrorDomain
import com.meloda.fast.base.mapResult
import com.meloda.fast.data.account.data.service.AccountService
import com.meloda.fast.data.account.domain.repository.AccountRepository
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AccountRepositoryImpl(
    private val accountService: AccountService
) : AccountRepository {

    override suspend fun setOnline(
        params: AccountSetOnlineRequest
    ): ApiResult<Unit, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        accountService.setOnline(params.map).mapResult(
            successMapper = {},
            errorMapper = { error -> error?.toDomain() }
        )
    }

    override suspend fun setOffline(
        params: AccountSetOfflineRequest
    ): ApiResult<Unit, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        accountService.setOffline(params.map).mapResult(
            successMapper = {},
            errorMapper = { error -> error?.toDomain() }
        )
    }
}
