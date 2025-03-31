package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.oauth.OAuthRepository
import dev.meloda.fast.data.asState
import dev.meloda.fast.model.AuthInfo
import dev.meloda.fast.model.api.responses.GetSilentTokenResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OAuthUseCaseImpl(
    private val oAuthRepository: OAuthRepository
) : OAuthUseCase {

    override fun auth(
        login: String,
        password: String,
        forceSms: Boolean,
        validationCode: String?,
        captchaSid: String?,
        captchaKey: String?
    ): Flow<State<AuthInfo>> = flow {
        emit(State.Loading)

        val newState = oAuthRepository.auth(
            login = login,
            password = password,
            forceSms = forceSms,
            validationCode = validationCode,
            captchaSid = captchaSid,
            captchaKey = captchaKey
        ).asState(
            successMapper = {
                AuthInfo(
                    userId = it.userId!!,
                    accessToken = it.accessToken!!,
                    validationHash = it.validationHash!!
                )
            }
        )

        emit(newState)
    }

    override fun getSilentToken(
        login: String,
        password: String,
        forceSms: Boolean,
        validationCode: String?,
        captchaSid: String?,
        captchaKey: String?
    ): Flow<State<GetSilentTokenResponse>> = flow {
        emit(State.Loading)

        val newState = oAuthRepository.getSilentToken(
            login = login,
            password = password,
            forceSms = forceSms,
            validationCode = validationCode,
            captchaSid = captchaSid,
            captchaKey = captchaKey
        ).asState()

        emit(newState)
    }
}
