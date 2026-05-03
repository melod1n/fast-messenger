package dev.meloda.fast.auth.login.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class CaptchaArguments(
    val redirectUri: String?
) : Parcelable
