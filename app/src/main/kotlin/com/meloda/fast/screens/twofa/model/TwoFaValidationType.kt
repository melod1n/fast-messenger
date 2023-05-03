package com.meloda.fast.screens.twofa.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class TwoFaValidationType : Parcelable {
    object Sms : TwoFaValidationType()
    object TwoFaApp : TwoFaValidationType()
    data class Another(val type: String) : TwoFaValidationType()

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
