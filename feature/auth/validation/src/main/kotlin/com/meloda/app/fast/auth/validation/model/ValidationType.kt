package com.meloda.app.fast.auth.validation.model

sealed class ValidationType(val value: String) {

    data object Sms : ValidationType(TYPE_SMS)

    data object App : ValidationType(TYPE_TWO_FA_APP)

    data class Other(val type: String) : ValidationType(type)

    companion object {
        private const val TYPE_SMS = "sms"
        private const val TYPE_TWO_FA_APP = "2fa_app"

        fun parse(validationType: String): ValidationType {
            return when (validationType) {
                TYPE_SMS -> Sms
                TYPE_TWO_FA_APP -> App
                else -> Other(validationType)
            }
        }
    }
}
