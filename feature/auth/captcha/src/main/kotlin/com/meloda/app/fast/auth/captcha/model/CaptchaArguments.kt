package com.meloda.app.fast.auth.captcha.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class CaptchaArguments(
    val captchaSid: String,
    val captchaImage: String
) : Parcelable
