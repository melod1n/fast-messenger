package com.meloda.fast.auth.login.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class LoginValidationArguments(
    val validationSid: String,
    val redirectUri: String,
    val phoneMask: String,
    val validationType: String,
    val canResendSms: Boolean,
    val wrongCodeError: String?,
) : Parcelable
