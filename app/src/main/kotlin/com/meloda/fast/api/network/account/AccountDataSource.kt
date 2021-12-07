package com.meloda.fast.api.network.account

import javax.inject.Inject

class AccountDataSource @Inject constructor(
    private val repo: AccountRepo
) {


    suspend fun setOnline(params: AccountSetOnlineRequest) = repo.setOnline(params.map)

    suspend fun setOffline(params: AccountSetOfflineRequest) = repo.setOffline(params.map)


}