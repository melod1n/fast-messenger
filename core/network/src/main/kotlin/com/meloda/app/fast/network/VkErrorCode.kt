package dev.meloda.fast.network

enum class VkErrorCode(val code: Int) {
    UNKNOWN_ERROR(1),
    APP_DISABLED(2),
    UNKNOWN_METHOD(3),
    INVALID_SIGNATURE(4),
    USER_AUTHORIZATION_FAILED(5),
    TOO_MANY_REQUESTS(6),
    NO_RIGHTS(7),
    BAD_REQUEST(8),
    TOO_MANY_SIMILAR_ACTIONS(9),
    INTERNAL_SERVER_ERROR(10),
    IN_TEST_MODE(11),
    EXECUTE_CODE_COMPILE_ERROR(12),
    EXECUTE_CODE_RUNTIME_ERROR(13),
    CAPTCHA_NEEDED(14),
    ACCESS_DENIED(15),
    REQUIRES_REQUESTS_OVER_HTTPS(16),
    VALIDATION_REQUIRED(17),
    USER_BANNED_OR_DELETED(18),
    ACTION_PROHIBITED(20),
    ACTION_ALLOWED_ONLY_FOR_STANDALONE(21),
    METHOD_OFF(23),
    CONFIRMATION_REQUIRED(24),
    PARAMETER_IS_NOT_SPECIFIED(100),
    INCORRECT_APP_ID(101),
    OUT_OF_LIMITS(103),
    INCORRECT_USER_ID(113),
    INCORRECT_TIMESTAMP(150),
    ACCESS_TO_ALBUM_DENIED(200),
    ACCESS_TO_AUDIO_DENIED(201),
    ACCESS_TO_GROUP_DENIED(203),
    ALBUM_IS_FULL(300),
    ACTION_DENIED(500),
    PERMISSION_DENIED(600),
    CANNOT_SEND_MESSAGE_BLACK_LIST(900),
    CANNOT_SEND_MESSAGE_GROUP(901),
    INVALID_DOC_ID(1150),
    INVALID_DOC_TITLE(1152),
    ACCESS_TO_DOC_DENIED(1153),

    SOME_AUTH_ERROR(104),
    ACCESS_TOKEN_EXPIRED(1117);

    companion object {
        fun parse(code: Int): VkErrorCode = entries.firstOrNull { it.code == code }
            ?: throw IllegalArgumentException("Unknown error with value: $code")
    }
}


