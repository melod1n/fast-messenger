package com.meloda.fast.api.network.auth

import com.meloda.fast.api.network.VkUrls

object AuthUrls {

    const val DirectAuth = "${VkUrls.OAUTH}/token"
    const val SendSms = "${VkUrls.API}/auth.validatePhone"
}
