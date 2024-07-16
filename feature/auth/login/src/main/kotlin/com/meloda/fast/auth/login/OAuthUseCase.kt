package com.meloda.fast.auth.login

import dev.meloda.fast.data.State
import com.meloda.fast.auth.login.model.AuthInfo
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
