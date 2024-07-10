package com.meloda.app.fast.data.api.account

import com.meloda.app.fast.model.api.requests.AccountSetOfflineRequest
import com.meloda.app.fast.model.api.requests.AccountSetOnlineRequest
import com.meloda.app.fast.network.service.account.AccountService

// TODO: 05/05/2024, Danil Nikolaev: implement
class AccountRepositoryImpl(
    private val accountService: AccountService
) : com.meloda.app.fast.data.api.account.AccountRepository {

    override suspend fun setOnline(params: AccountSetOnlineRequest): Boolean {
        return false
    }

    override suspend fun setOffline(params: AccountSetOfflineRequest): Boolean {
        return false
    }
}
