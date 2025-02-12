package dev.meloda.fast.network

enum class ValidationType(val value: String) {
    APP("2fa_app"),
    SMS("2fa_sms");

    companion object {
        fun parse(value: String): ValidationType =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown validation type $value")
    }
}
