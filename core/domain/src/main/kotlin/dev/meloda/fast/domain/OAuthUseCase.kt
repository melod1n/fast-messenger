package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.model.AuthInfo
import kotlinx.coroutines.flow.Flow

interface OAuthUseCase {

    fun auth(
        login: String,
        password: String,
        forceSms: Boolean,
        validationCode: String?,
        captchaSid: String?,
        captchaKey: String?
    ): Flow<State<AuthInfo>>
}
