package com.meloda.fast.api.network

object ErrorCodes {
    const val UNKNOWN_ERROR = 1
    const val APP_DISABLED = 2
    const val UNKNOWN_METHOD = 3
    const val INVALID_SIGNATURE = 4
    const val USER_AUTHORIZATION_FAILED = 5
    const val TOO_MANY_REQUESTS = 6
    const val NO_RIGHTS = 7
    const val BAD_REQUEST = 8
    const val TOO_MANY_SIMILAR_ACTIONS = 9
    const val INTERNAL_SERVER_ERROR = 10
    const val IN_TEST_MODE = 11
    const val EXECUTE_CODE_COMPILE_ERROR = 12
    const val EXECUTE_CODE_RUNTIME_ERROR = 13
    const val CAPTCHA_NEEDED = 14
    const val ACCESS_DENIED = 15
    const val REQUIRES_REQUESTS_OVER_HTTPS = 16
    const val VALIDATION_REQUIRED = 17
    const val USER_BANNED_OR_DELETED = 18
    const val ACTION_PROHIBITED = 20
    const val ACTION_ALLOWED_ONLY_FOR_STANDALONE = 21
    const val METHOD_OFF = 23
    const val CONFIRMATION_REQUIRED = 24
    const val PARAMETER_IS_NOT_SPECIFIED = 100
    const val INCORRECT_APP_ID = 101
    const val OUT_OF_LIMITS = 103
    const val INCORRECT_USER_ID = 113
    const val INCORRECT_TIMESTAMP = 150
    const val ACCESS_TO_ALBUM_DENIED = 200
    const val ACCESS_TO_AUDIO_DENIED = 201
    const val ACCESS_TO_GROUP_DENIED = 203
    const val ALBUM_IS_FULL = 300
    const val ACTION_DENIED = 500
    const val PERMISSION_DENIED = 600
    const val CANNOT_SEND_MESSAGE_BLACK_LIST = 900
    const val CANNOT_SEND_MESSAGE_GROUP = 901
    const val INVALID_DOC_ID = 1150
    const val INVALID_DOC_TITLE = 1152
    const val ACCESS_TO_DOC_DENIED = 1153
}

object VKErrors {
    const val UNKNOWN = "unknown_error"

    const val NEED_VALIDATION = "need_validation"
    const val NEED_CAPTCHA = "need_captcha"
    const val INVALID_REQUEST = "invalid_request"

}