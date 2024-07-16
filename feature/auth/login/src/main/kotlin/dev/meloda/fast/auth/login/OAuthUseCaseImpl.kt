package dev.meloda.fast.auth.login

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.oauth.OAuthRepository
import dev.meloda.fast.network.OAuthErrorDomain
import dev.meloda.fast.network.ValidationType
import dev.meloda.fast.network.VkOAuthError
import dev.meloda.fast.network.VkOAuthErrorType
import dev.meloda.fast.auth.login.model.AuthInfo
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

        val error = response.error?.let(VkOAuthError::parse)
        val errorType = response.errorType?.let(VkOAuthErrorType::parse)

        val newState = when (error) {
            null -> {
                State.Success(
                    AuthInfo(
                        userId = response.userId,
                        accessToken = response.accessToken,
                        validationHash = response.validationHash
                    )
                )
            }

            VkOAuthError.FLOOD_CONTROL -> {
                State.Error.OAuthError(OAuthErrorDomain.TooManyTriesError)
            }

            VkOAuthError.NEED_VALIDATION -> {
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

            VkOAuthError.NEED_CAPTCHA -> {
                State.Error.OAuthError(
                    OAuthErrorDomain.CaptchaRequiredError(
                        captchaSid = response.captchaSid.orEmpty(),
                        captchaImageUrl = response.captchaImage.orEmpty()
                    )
                )
            }

            VkOAuthError.INVALID_CLIENT -> {
                State.Error.OAuthError(OAuthErrorDomain.InvalidCredentialsError)
            }

            VkOAuthError.INVALID_REQUEST -> {
                when (errorType) {
                    VkOAuthErrorType.WRONG_OTP -> {
                        State.Error.OAuthError(OAuthErrorDomain.WrongValidationCode)
                    }

                    VkOAuthErrorType.WRONG_OTP_FORMAT -> {
                        State.Error.OAuthError(OAuthErrorDomain.WrongValidationCodeFormat)
                    }

                    VkOAuthErrorType.PASSWORD_BRUTEFORCE_ATTEMPT -> {
                        State.Error.OAuthError(OAuthErrorDomain.TooManyTriesError)
                    }

                    null -> State.Error.OAuthError(OAuthErrorDomain.UnknownError)
                }
            }

            VkOAuthError.UNKNOWN -> {
                State.Error.OAuthError(OAuthErrorDomain.UnknownError)
            }
        }

        emit(newState)
    }
}
