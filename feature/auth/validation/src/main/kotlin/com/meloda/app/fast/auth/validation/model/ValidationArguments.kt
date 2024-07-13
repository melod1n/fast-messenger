package com.meloda.app.fast.auth.validation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class ValidationArguments(
    val validationSid: String,
    val redirectUri: String,
    val phoneMask: String,
    val validationType: String,
    val canResendSms: Boolean,
    val wrongCodeError: String?,
) : Parcelable
