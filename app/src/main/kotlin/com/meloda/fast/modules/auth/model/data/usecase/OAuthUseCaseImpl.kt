package com.meloda.fast.modules.auth.model.data.usecase

import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.fold
import com.meloda.fast.api.network.BaseOAuthError
import com.meloda.fast.api.network.oauth.AuthDirectRequest
import com.meloda.fast.api.network.oauth.AuthDirectResponse
import com.meloda.fast.base.State
import com.meloda.fast.base.toStateError
import com.meloda.fast.modules.auth.model.domain.repository.OAuthRepository
import com.meloda.fast.modules.auth.model.domain.usecase.OAuthUseCase
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

        val newState = oAuthRepository.auth(
            AuthDirectRequest(
                grantType = VKConstants.Auth.GrantType.PASSWORD,
                clientId = VKConstants.VK_APP_ID,
                clientSecret = VKConstants.VK_SECRET,
                username = login,
                password = password,
                scope = VKConstants.Auth.SCOPE,
                twoFaForceSms = forceSms,
                twoFaCode = twoFaCode,
                captchaSid = captchaSid,
                captchaKey = captchaKey,
            )
        ).fold(
            onSuccess = { response ->
                State.Success(response)
            },
            onFailure = BaseOAuthError::toStateError
        )
        emit(newState)
    }
}
