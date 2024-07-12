package com.meloda.app.fast.data.api.oauth

import com.meloda.app.fast.common.VkConstants
import com.meloda.app.fast.model.api.requests.AuthDirectRequest
import com.meloda.app.fast.model.api.responses.AuthDirectResponse
import com.meloda.app.fast.network.service.oauth.OAuthService
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OAuthRepositoryImpl(
    private val oAuthService: OAuthService,
) : OAuthRepository {

    override suspend fun auth(
        login: String,
        password: String,
        forceSms: Boolean,
        twoFaCode: String?,
        captchaSid: String?,
        captchaKey: String?
    ): AuthDirectResponse = withContext(Dispatchers.IO) {
        val requestModel = AuthDirectRequest(
            grantType = VkConstants.Auth.GrantType.PASSWORD,
            clientId = VkConstants.VK_APP_ID,
            clientSecret = VkConstants.VK_SECRET,
            username = login,
            password = password,
            scope = VkConstants.Auth.SCOPE,
            twoFaForceSms = forceSms,
            twoFaCode = twoFaCode,
            captchaSid = captchaSid,
            captchaKey = captchaKey,
        )

        when (val result = oAuthService.auth(requestModel.map)) {
            is ApiResult.Success -> result.value

            is ApiResult.Failure.HttpFailure -> {
                requireNotNull(result.error)
            }

            is ApiResult.Failure.ApiFailure -> TODO()

            is ApiResult.Failure.NetworkFailure -> {
                // TODO: 13/07/2024, Danil Nikolaev: implement showing network error
                TODO()
            }
            is ApiResult.Failure.UnknownFailure -> TODO()

            else -> throw IllegalStateException("Unknown result")
        }
    }
}
