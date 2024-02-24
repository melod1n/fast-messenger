package com.meloda.fast.api.model.base.attachments

import com.meloda.fast.api.model.attachments.VkGroupCall
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BaseVkGroupCall(
    val initiator_id: Int,
    val join_link: String,
    val participants: Participants
) {

    @JsonClass(generateAdapter = true)
    data class Participants(
        val list: List<Int>,
        val count: Int
    )

    fun asVkGroupCall() = VkGroupCall(initiatorId = initiator_id)

}
