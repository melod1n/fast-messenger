package dev.meloda.fast.network.service.auth

import dev.meloda.fast.common.AppConstants

object AuthUrls {
    private const val URL = AppConstants.URL_API

    const val VALIDATE_PHONE = "$URL/auth.validatePhone"
    const val VALIDATE_LOGIN = "$URL/auth.validateLogin"

    const val GET_ANONYM_TOKEN = "$URL/auth.getAnonymToken"
    const val EXCHANGE_SILENT_TOKEN = "$URL/auth.exchangeSilentAuthToken"
    const val GET_EXCHANGE_TOKEN = "$URL/auth.getExchangeToken"
}
