package com.meloda.app.fast.network

sealed class OAuthErrorDomain {

    data class ValidationRequiredError(
        val description: String,
        val validationType: ValidationType,
        val validationSid: String,
        val phoneMask: String,
        val redirectUri: String,
        val validationResend: String?,
        val restoreIfCannotGetCode: Boolean?
    ) : OAuthErrorDomain()

    data class CaptchaRequiredError(
        val captchaSid: String,
        val captchaImageUrl: String
    ) : OAuthErrorDomain()

    data class UserBannedError(
        val memberName: String,
        val message: String,
        val accessToken: String,
        val restoreUrl: String
    ) : OAuthErrorDomain()

    data object InvalidCredentialsError : OAuthErrorDomain()
    data object WrongTwoFaCode : OAuthErrorDomain()
    data object WrongTwoFaCodeFormat : OAuthErrorDomain()
    data object TooManyTriesError:  OAuthErrorDomain()

    data object UnknownError : OAuthErrorDomain()
}
