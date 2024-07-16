package dev.meloda.fast.auth.validation.model

enum class ValidationType(val value: String) {
    SMS("sms"), APP("2fa_app");

    companion object {
        fun parse(value: String): ValidationType = entries.firstOrNull { it.value == value }
            ?: throw IllegalArgumentException("Unknown validation type with value: $value")
    }
}
