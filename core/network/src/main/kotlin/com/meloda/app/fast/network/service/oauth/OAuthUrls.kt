package dev.meloda.fast.network.service.oauth

import dev.meloda.fast.common.AppConstants

object OAuthUrls {
    private const val URL = AppConstants.URL_OAUTH

    const val DIRECT_AUTH = "$URL/token"
    const val GET_ANONYMOUS_TOKEN = "$URL/get_anonym_token"
}
