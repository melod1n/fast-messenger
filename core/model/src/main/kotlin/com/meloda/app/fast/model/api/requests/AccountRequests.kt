package com.meloda.app.fast.model.api.requests

import com.meloda.app.fast.model.api.asInt

data class AccountSetOnlineRequest(
    val voip: Boolean,
    val accessToken: String
) {

    val map
        get() = mutableMapOf(
            "voip" to voip.asInt().toString(),
            "access_token" to accessToken
        )

}

data class AccountSetOfflineRequest(val accessToken: String) {
    val map get() = mutableMapOf("access_token" to accessToken)
}
