package dev.meloda.fast.data.api.oauth

import dev.meloda.fast.model.api.responses.AuthDirectResponse

interface OAuthRepository {

    suspend fun auth(
        login: String,
        password: String,
        forceSms: Boolean,
        validationCode: String?,
        captchaSid: String?,
        captchaKey: String?
    ): AuthDirectResponse
}
