package dev.meloda.fast.data.api.oauth

import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.model.api.requests.AuthDirectRequest
import dev.meloda.fast.model.api.responses.AuthDirectResponse
import dev.meloda.fast.network.service.oauth.OAuthService
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
        validationCode: String?,
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
            validationForceSms = forceSms,
            validationCode = validationCode,
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
