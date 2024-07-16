package dev.meloda.fast.network.service.auth

import dev.meloda.fast.common.AppConstants

object AuthUrls {
    private const val URL = AppConstants.URL_API

    const val VALIDATE_PHONE = "$URL/auth.validatePhone"
    const val VALIDATE_LOGIN = "$URL/auth.validateLogin"
}
