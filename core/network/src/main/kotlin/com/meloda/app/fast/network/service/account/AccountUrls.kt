package dev.meloda.fast.network.service.account

import dev.meloda.fast.common.AppConstants

object AccountUrls {
    private const val URL = AppConstants.URL_API

    const val SET_ONLINE = "$URL/account.setOnline"
    const val SET_OFFLINE = "$URL/account.setOffline"
    const val REGISTER_DEVICE = "$URL/account.registerDevice"
}
