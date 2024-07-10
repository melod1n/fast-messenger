package com.meloda.app.fast.network

@Suppress("unused")
object VkErrorCodes {
    const val UnknownError = 1
    const val AppDisabled = 2
    const val UnknownMethod = 3
    const val InvalidSignature = 4
    const val UserAuthorizationFailed = 5
    const val TooManyRequests = 6
    const val NoRights = 7
    const val BadRequest = 8
    const val TooManySimilarActions = 9
    const val InternalServerError = 10
    const val InTestMode = 11
    const val ExecuteCodeCompileError = 12
    const val ExecuteCodeRuntimeError = 13
    const val CaptchaNeeded = 14
    const val AccessDenied = 15
    const val RequiresRequestsOverHttps = 16
    const val ValidationRequired = 17
    const val UserBannedOrDeleted = 18
    const val ActionProhibited = 20
    const val ActionAllowedOnlyForStandalone = 21
    const val MethodOff = 23
    const val ConfirmationRequired = 24
    const val ParameterIsNotSpecified = 100
    const val IncorrectAppId = 101
    const val OutOfLimits = 103
    const val IncorrectUserId = 113
    const val IncorrectTimestamp = 150
    const val AccessToAlbumDenied = 200
    const val AccessToAudioDenied = 201
    const val AccessToGroupDenied = 203
    const val AlbumIsFull = 300
    const val ActionDenied = 500
    const val PermissionDenied = 600
    const val CannotSendMessageBlackList = 900
    const val CannotSendMessageGroup = 901
    const val InvalidDocId = 1150
    const val InvalidDocTitle = 1152
    const val AccessToDocDenied = 1153

    const val AccessTokenExpired = 1117
}

object VkOAuthErrors {
    const val UNKNOWN = "unknown_error"

    const val NEED_VALIDATION = "need_validation"
    const val NEED_CAPTCHA = "need_captcha"
    const val INVALID_CLIENT = "invalid_client"
    const val INVALID_REQUEST = "invalid_request"

}

object VkErrorTypes {
    const val WRONG_OTP_FORMAT = "otp_format_is_incorrect"
    const val WRONG_OTP = "wrong_otp"
}
