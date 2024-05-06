package com.meloda.app.fast.auth

import com.meloda.app.fast.data.State
import com.meloda.app.fast.data.api.oauth.OAuthRepository
import com.meloda.app.fast.model.api.responses.AuthDirectResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OAuthUseCaseImpl(
    private val oAuthRepository: OAuthRepository
) : OAuthUseCase {

    // TODO: 05/05/2024, Danil Nikolaev: implement

    override fun auth(
        login: String,
        password: String,
        forceSms: Boolean,
        twoFaCode: String?,
        captchaSid: String?,
        captchaKey: String?
    ): Flow<State<AuthDirectResponse>> = flow {
//        emit(State.Loading)
//
//        val newState = oAuthRepository.auth(
//            AuthDirectRequest(
//                grantType = VkConstants.Auth.GrantType.PASSWORD,
//                clientId = VkConstants.VK_APP_ID,
//                clientSecret = VkConstants.VK_SECRET,
//                username = login,
//                password = password,
//                scope = VkConstants.Auth.SCOPE,
//                twoFaForceSms = forceSms,
//                twoFaCode = twoFaCode,
//                captchaSid = captchaSid,
//                captchaKey = captchaKey,
//            )
//        ).fold(
//            onSuccess = { response ->
//                State.Success(response)
//            },
//            onFailure = BaseOAuthError::toStateError
//        )
//        emit(newState)
    }
}
