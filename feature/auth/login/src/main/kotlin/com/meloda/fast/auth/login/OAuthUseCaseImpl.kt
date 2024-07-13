package com.meloda.fast.auth.login

import com.meloda.app.fast.data.State
import com.meloda.app.fast.data.api.oauth.OAuthRepository
import com.meloda.app.fast.network.OAuthErrorDomain
import com.meloda.app.fast.network.ValidationType
import com.meloda.app.fast.network.VkErrorTypes
import com.meloda.app.fast.network.VkOAuthErrors
import com.meloda.fast.auth.login.model.AuthInfo
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

        val response = oAuthRepository.auth(
            login = login,
            password = password,
            validationCode = validationCode,
            captchaSid = captchaSid,
            captchaKey = captchaKey,
            forceSms = forceSms
        )

        val newState = when (response.error) {
            null -> {
                State.Success(
                    AuthInfo(
                        userId = response.userId,
                        accessToken = response.accessToken,
                        validationHash = response.validationHash
                    )
                )
            }

            VkOAuthErrors.FLOOD_CONTROL -> {
                State.Error.OAuthError(OAuthErrorDomain.TooManyTriesError)
            }

            VkOAuthErrors.NEED_VALIDATION -> {
                if (response.banInfo != null) {
                    val info = requireNotNull(response.banInfo)

                    State.Error.OAuthError(
                        OAuthErrorDomain.UserBannedError(
                            memberName = info.memberName,
                            message = info.message,
                            accessToken = info.accessToken,
                            restoreUrl = info.restoreUrl
                        )
                    )
                } else {
                    State.Error.OAuthError(
                        OAuthErrorDomain.ValidationRequiredError(
                            description = response.errorDescription.orEmpty(),
                            validationType = response.validationType.orEmpty()
                                .let(ValidationType::parse),
                            validationSid = response.validationSid.orEmpty(),
                            phoneMask = response.phoneMask.orEmpty(),
                            redirectUri = response.redirectUri.orEmpty(),
                            validationResend = response.validationResend,
                            restoreIfCannotGetCode = response.restoreIfCannotGetCode
                        )
                    )
                }
            }

            VkOAuthErrors.NEED_CAPTCHA -> {
                State.Error.OAuthError(
                    OAuthErrorDomain.CaptchaRequiredError(
                        captchaSid = response.captchaSid.orEmpty(),
                        captchaImageUrl = response.captchaImage.orEmpty()
                    )
                )
            }

            VkOAuthErrors.INVALID_CLIENT -> {
                State.Error.OAuthError(OAuthErrorDomain.InvalidCredentialsError)
            }

            VkOAuthErrors.INVALID_REQUEST -> {
                when (response.errorType) {
                    VkErrorTypes.WRONG_OTP -> {
                        State.Error.OAuthError(OAuthErrorDomain.WrongValidationCode)
                    }

                    VkErrorTypes.WRONG_OTP_FORMAT -> {
                        State.Error.OAuthError(OAuthErrorDomain.WrongValidationCodeFormat)
                    }

                    else -> {
                        State.Error.OAuthError(OAuthErrorDomain.UnknownError)
                    }
                }
            }

            else -> {
                State.Error.OAuthError(OAuthErrorDomain.UnknownError)
            }
        }

        emit(newState)
    }
}
