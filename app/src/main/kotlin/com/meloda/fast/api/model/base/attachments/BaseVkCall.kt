package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkCall(
    @SerializedName("initiator_id")
    val initiatorId: Int,
    @SerializedName("receiver_id")
    val receiverId: Int,
    val state: String,
    val time: Int,
    val duration: Int,
    val video: Boolean
) : Parcelable