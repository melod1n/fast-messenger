package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkGroupCall(
    @SerializedName("initiator_id")
    val initiatorId: Int,
    @SerializedName("join_link")
    val joinLink: String,
    val participants: Participants
) : Parcelable {

    @Parcelize
    data class Participants(
        val list: List<Int>,
        val count: Int
    ) : Parcelable

}
