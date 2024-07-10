package com.meloda.app.fast.data.api.oauth

import com.meloda.app.fast.model.api.responses.AuthDirectResponse

interface OAuthRepository {

    suspend fun auth(
        login: String,
        password: String,
        forceSms: Boolean,
        twoFaCode: String?,
        captchaSid: String?,
        captchaKey: String?
    ): AuthDirectResponse
}
