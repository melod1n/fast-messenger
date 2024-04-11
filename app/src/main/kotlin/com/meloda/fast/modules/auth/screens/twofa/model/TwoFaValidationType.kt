package com.meloda.fast.modules.auth.screens.twofa.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class TwoFaValidationType(val value: String) : Parcelable {

    @Parcelize
    data object Sms : TwoFaValidationType(TYPE_SMS)

    @Parcelize
    data object TwoFaApp : TwoFaValidationType(TYPE_TWO_FA_APP)

    @Parcelize
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
