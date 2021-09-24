package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkCall(
    val initiator_id: Int,
    val receiver_id: Int,
    val state: String,
    val time: Int,
    val duration: Int,
    val video: Boolean
) : Parcelable