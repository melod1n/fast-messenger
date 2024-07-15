package com.meloda.app.fast.network.service.oauth

import com.meloda.app.fast.common.AppConstants

object OAuthUrls {
    private const val URL = AppConstants.URL_OAUTH

    const val DIRECT_AUTH = "$URL/token"
    const val GET_ANONYMOUS_TOKEN = "$URL/get_anonym_token"
}
