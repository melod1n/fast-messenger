package com.meloda.app.fast.auth

import com.meloda.app.fast.data.State
import com.meloda.app.fast.model.api.responses.AuthDirectResponse
import kotlinx.coroutines.flow.Flow

interface OAuthUseCase {

    fun auth(
        login: String,
        password: String,
        forceSms: Boolean,
        twoFaCode: String?,
        captchaSid: String?,
        captchaKey: String?
    ): Flow<State<AuthDirectResponse>>
}
