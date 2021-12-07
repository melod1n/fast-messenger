package com.meloda.fast.api.network.account

import com.meloda.fast.api.ApiExtensions.intString

data class AccountSetOnlineRequest(
    val voip: Boolean,
    val accessToken: String
) {

    val map
        get() = mutableMapOf(
            "voip" to voip.intString,
            "access_token" to accessToken
        )

}

data class AccountSetOfflineRequest(val accessToken: String) {
    val map get() = mutableMapOf("access_token" to accessToken)
}
