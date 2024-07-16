package dev.meloda.fast.network

enum class VkOAuthError(val value: String) {
    UNKNOWN("unknown_error"),

    NEED_VALIDATION("need_validation"),
    NEED_CAPTCHA("need_captcha"),
    INVALID_CLIENT("invalid_client"),
    INVALID_REQUEST("invalid_request"),
    FLOOD_CONTROL("9;Flood control");

    companion object {
        fun parse(value: String): VkOAuthError = entries.firstOrNull { it.value == value }
            ?: throw IllegalArgumentException("Unknown error with value: $value")
    }
}
