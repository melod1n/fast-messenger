package com.meloda.fast.api.model.base

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkLongPoll(
    val server: String,
    val key: String,
    val ts: Int,
    val pts: Int
) : Parcelable