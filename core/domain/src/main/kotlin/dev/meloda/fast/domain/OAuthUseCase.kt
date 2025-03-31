package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.model.AuthInfo
import dev.meloda.fast.model.api.responses.GetSilentTokenResponse
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

    fun getSilentToken(
        login: String,
        password: String,
        forceSms: Boolean,
        validationCode: String?,
        captchaSid: String?,
        captchaKey: String?
    ): Flow<State<GetSilentTokenResponse>>
}
