package com.meloda.fast.data.account

import com.meloda.fast.api.network.account.AccountSetOfflineRequest
import com.meloda.fast.api.network.account.AccountSetOnlineRequest

class AccountsRepository(
    private val accountApi: AccountApi,
    private val accountsDao: AccountsDao
) {

    suspend fun setOnline(params: AccountSetOnlineRequest) = accountApi.setOnline(params.map)

    suspend fun setOffline(params: AccountSetOfflineRequest) = accountApi.setOffline(params.map)



}