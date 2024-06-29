package com.meloda.app.fast.auth

import com.meloda.app.fast.common.VkConstants
import com.meloda.app.fast.data.State
import com.meloda.app.fast.data.api.oauth.OAuthRepository
import com.meloda.app.fast.data.toStateApiError
import com.meloda.app.fast.model.api.requests.AuthDirectRequest
import com.meloda.app.fast.model.api.responses.AuthDirectResponse
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OAuthUseCaseImpl(
    private val oAuthRepository: OAuthRepository
) : OAuthUseCase {

    override fun auth(
        login: String,
        password: String,
        forceSms: Boolean,
        twoFaCode: String?,
        captchaSid: String?,
        captchaKey: String?
    ): Flow<State<AuthDirectResponse>> = flow {
        emit(State.Loading)

        val newState = when (
            val result = oAuthRepository.auth(
                AuthDirectRequest(
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
            )
        ) {
            is ApiResult.Success -> State.Success(result.value)

            is ApiResult.Failure.NetworkFailure -> {

                State.Error.ConnectionError
            }
            is ApiResult.Failure.UnknownFailure -> {
                State.UNKNOWN_ERROR
            }
            is ApiResult.Failure.HttpFailure -> {
                result.error.toStateApiError()
            }
            is ApiResult.Failure.ApiFailure -> {
                result.error.toStateApiError()
            }
        }
        emit(newState)
    }
}
