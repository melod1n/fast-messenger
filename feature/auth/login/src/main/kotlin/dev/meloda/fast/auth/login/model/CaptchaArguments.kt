package com.meloda.fast.auth.login.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class CaptchaArguments(
    val captchaSid: String,
    val captchaImageUrl: String
) : Parcelable
