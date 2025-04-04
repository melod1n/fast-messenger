package dev.meloda.fast.data.api.oauth

import com.slack.eithernet.ApiResult
import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.model.api.requests.AuthDirectRequest
import dev.meloda.fast.model.api.responses.AuthDirectResponse
import dev.meloda.fast.model.api.responses.GetSilentTokenResponse
import dev.meloda.fast.network.OAuthErrorDomain
import dev.meloda.fast.network.ValidationType
import dev.meloda.fast.network.VkOAuthError
import dev.meloda.fast.network.VkOAuthErrorType
import dev.meloda.fast.network.mapResult
import dev.meloda.fast.network.service.oauth.OAuthService
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
        captchaKey: String?,
    ): ApiResult<AuthDirectResponse, OAuthErrorDomain> = withContext(Dispatchers.IO) {
        val requestModel = AuthDirectRequest(
            grantType = VkConstants.Auth.GrantType.PASSWORD,
            clientId = VkConstants.MESSENGER_APP_ID.toString(),
            clientSecret = VkConstants.MESSENGER_APP_SECRET,
            username = login,
            password = password,
            scope = VkConstants.MESSENGER_APP_SCOPE.toString(),
            validationForceSms = forceSms,
            validationCode = validationCode,
            captchaSid = captchaSid,
            captchaKey = captchaKey,
        )

        oAuthService.auth(requestModel.map).mapResult(
            successMapper = {
                it
            },
            errorMapper = { response ->
                val error = response?.error?.let(VkOAuthError::parse)
                val errorType = response?.errorType?.let(VkOAuthErrorType::parse)

                when (error) {
                    null -> OAuthErrorDomain.UnknownError

                    VkOAuthError.FLOOD_CONTROL -> OAuthErrorDomain.TooManyTriesError

                    VkOAuthError.NEED_VALIDATION -> {
                        if (response.banInfo != null) {
                            val info = requireNotNull(response.banInfo)

                            OAuthErrorDomain.UserBannedError(
                                memberName = info.memberName,
                                message = info.message,
                                accessToken = info.accessToken,
                                restoreUrl = info.restoreUrl
                            )
                        } else {
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
                        }
                    }

                    VkOAuthError.NEED_CAPTCHA -> {
                        OAuthErrorDomain.CaptchaRequiredError(
                            captchaSid = response.captchaSid.orEmpty(),
                            captchaImageUrl = response.captchaImage.orEmpty()
                        )
                    }

                    VkOAuthError.INVALID_CLIENT -> {
                        OAuthErrorDomain.InvalidCredentialsError
                    }

                    VkOAuthError.INVALID_REQUEST -> {
                        when (errorType) {
                            null -> OAuthErrorDomain.UnknownError

                            VkOAuthErrorType.WRONG_OTP -> {
                                OAuthErrorDomain.WrongValidationCode
                            }

                            VkOAuthErrorType.WRONG_OTP_FORMAT -> {
                                OAuthErrorDomain.WrongValidationCodeFormat
                            }

                            VkOAuthErrorType.PASSWORD_BRUTEFORCE_ATTEMPT -> {
                                OAuthErrorDomain.TooManyTriesError
                            }

                            VkOAuthErrorType.USERNAME_OR_PASSWORD_IS_INCORRECT -> {
                                OAuthErrorDomain.InvalidCredentialsError
                            }
                        }
                    }

                    VkOAuthError.UNKNOWN -> OAuthErrorDomain.UnknownError
                }
            }
        )
    }

    override suspend fun getSilentToken(
        login: String,
        password: String,
        forceSms: Boolean,
        validationCode: String?,
        captchaSid: String?,
        captchaKey: String?,
    ): ApiResult<GetSilentTokenResponse, OAuthErrorDomain> =
        withContext(Dispatchers.IO) {
            val requestModel = AuthDirectRequest(
                grantType = VkConstants.Auth.GrantType.PASSWORD,
                clientId = VkConstants.MESSENGER_APP_ID.toString(),
                clientSecret = VkConstants.MESSENGER_APP_SECRET,
                username = login,
                password = password,
                scope = VkConstants.MESSENGER_APP_SCOPE.toString(),
                validationForceSms = forceSms,
                validationCode = validationCode,
                captchaSid = captchaSid,
                captchaKey = captchaKey,
            )

            oAuthService.getSilentToken(requestModel.map).mapResult(
                successMapper = { it },
                errorMapper = { response ->
                    val error = response?.error?.let(VkOAuthError::parse)
                    val errorType = response?.errorType?.let(VkOAuthErrorType::parse)

                    when (error) {
                        null -> OAuthErrorDomain.UnknownError

                        VkOAuthError.FLOOD_CONTROL -> OAuthErrorDomain.TooManyTriesError

                        VkOAuthError.NEED_VALIDATION -> {
                            if (response.banInfo != null) {
                                val info = requireNotNull(response.banInfo)

                                OAuthErrorDomain.UserBannedError(
                                    memberName = info.memberName,
                                    message = info.message,
                                    accessToken = info.accessToken,
                                    restoreUrl = info.restoreUrl
                                )
                            } else {
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
                            }
                        }

                        VkOAuthError.NEED_CAPTCHA -> {
                            OAuthErrorDomain.CaptchaRequiredError(
                                captchaSid = response.captchaSid.orEmpty(),
                                captchaImageUrl = response.captchaImage.orEmpty()
                            )
                        }

                        VkOAuthError.INVALID_CLIENT -> {
                            OAuthErrorDomain.InvalidCredentialsError
                        }

                        VkOAuthError.INVALID_REQUEST -> {
                            when (errorType) {
                                null -> OAuthErrorDomain.UnknownError

                                VkOAuthErrorType.WRONG_OTP -> {
                                    OAuthErrorDomain.WrongValidationCode
                                }

                                VkOAuthErrorType.WRONG_OTP_FORMAT -> {
                                    OAuthErrorDomain.WrongValidationCodeFormat
                                }

                                VkOAuthErrorType.PASSWORD_BRUTEFORCE_ATTEMPT -> {
                                    OAuthErrorDomain.TooManyTriesError
                                }

                                VkOAuthErrorType.USERNAME_OR_PASSWORD_IS_INCORRECT -> {
                                    OAuthErrorDomain.InvalidCredentialsError
                                }
                            }
                        }

                        VkOAuthError.UNKNOWN -> OAuthErrorDomain.UnknownError
                    }
                }
            )
        }
}
