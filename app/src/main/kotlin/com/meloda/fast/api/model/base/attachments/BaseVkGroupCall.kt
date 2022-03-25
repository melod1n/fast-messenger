package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.meloda.fast.api.model.attachments.VkGroupCall
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkGroupCall(
    val initiator_id: Int,
    val join_link: String,
    val participants: Participants
) : Parcelable {

    @Parcelize
    data class Participants(
        val list: List<Int>,
        val count: Int
    ) : Parcelable

    fun asVkGroupCall() = VkGroupCall(initiatorId = initiator_id)

}
