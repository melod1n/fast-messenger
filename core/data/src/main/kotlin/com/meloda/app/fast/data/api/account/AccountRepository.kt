package com.meloda.app.fast.data.api.account

import com.meloda.app.fast.model.api.requests.AccountSetOfflineRequest
import com.meloda.app.fast.model.api.requests.AccountSetOnlineRequest

interface AccountRepository {

    suspend fun setOnline(
        params: AccountSetOnlineRequest
    ): Boolean

    suspend fun setOffline(
        params: AccountSetOfflineRequest
    ): Boolean
}
