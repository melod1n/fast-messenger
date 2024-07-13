package com.meloda.app.fast.userbanned.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class UserBannedArguments(
    val userName: String,
    val message: String,
    val restoreUrl: String,
    val accessToken: String
) : Parcelable
