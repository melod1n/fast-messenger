package com.meloda.app.fast.auth.twofa.model

sealed class TwoFaValidationType(val value: String) {

    data object Sms : TwoFaValidationType(TYPE_SMS)

    data object TwoFaApp : TwoFaValidationType(TYPE_TWO_FA_APP)

    data class Another(val type: String) : TwoFaValidationType(type)

    companion object {
        private const val TYPE_SMS = "sms"
        private const val TYPE_TWO_FA_APP = "2fa_app"

        fun parse(validationType: String): TwoFaValidationType {
            return when (validationType) {
                TYPE_SMS -> Sms
                TYPE_TWO_FA_APP -> TwoFaApp
                else -> Another(validationType)
            }
        }
    }
}
