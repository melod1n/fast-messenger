package dev.meloda.fast.data.api.oauth

import com.slack.eithernet.ApiResult
import dev.meloda.fast.model.api.responses.AuthDirectResponse
import dev.meloda.fast.model.api.responses.GetSilentTokenResponse
import dev.meloda.fast.network.OAuthErrorDomain

interface OAuthRepository {

    suspend fun auth(
        login: String,
        password: String,
        forceSms: Boolean,
        validationCode: String?,
        captchaSid: String?,
        captchaKey: String?
    ): ApiResult<AuthDirectResponse, OAuthErrorDomain>

    suspend fun getSilentToken(
        login: String,
        password: String,
        forceSms: Boolean,
        validationCode: String?,
        captchaSid: String?,
        captchaKey: String?,
    ): ApiResult<GetSilentTokenResponse, OAuthErrorDomain>
}
