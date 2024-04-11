package com.meloda.fast.modules.auth.model.domain.usecase

import com.meloda.fast.api.network.oauth.AuthDirectResponse
import com.meloda.fast.base.State
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
