package dev.meloda.fast.auth.login.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class LoginUserBannedArguments(
    val name: String,
    val message: String,
    val restoreUrl: String,
    val accessToken: String
) : Parcelable
