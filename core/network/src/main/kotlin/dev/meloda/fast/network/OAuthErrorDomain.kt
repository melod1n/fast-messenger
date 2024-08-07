package dev.meloda.fast.network

sealed class OAuthErrorDomain {

    data object UnknownError : OAuthErrorDomain()

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
    data object WrongValidationCode : OAuthErrorDomain()
    data object WrongValidationCodeFormat : OAuthErrorDomain()
    data object TooManyTriesError:  OAuthErrorDomain()
}
