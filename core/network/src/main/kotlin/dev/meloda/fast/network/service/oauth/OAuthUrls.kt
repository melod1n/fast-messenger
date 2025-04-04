package dev.meloda.fast.network.service.oauth

import dev.meloda.fast.common.AppConstants

object OAuthUrls {
    private const val URL = AppConstants.URL_OAUTH

    const val GET_SILENT_TOKEN = "$URL/token"
}
