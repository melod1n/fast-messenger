package dev.meloda.fast.network

enum class VkOAuthErrorType(val value: String) {
    WRONG_OTP_FORMAT("otp_format_is_incorrect"),
    WRONG_OTP("wrong_otp"),
    PASSWORD_BRUTEFORCE_ATTEMPT("password_bruteforce_attempt");

    companion object {
        fun parse(value: String): VkOAuthErrorType = entries.firstOrNull { it.value == value }
            ?: throw IllegalArgumentException("Unknown error type with value: $value")
    }
}
