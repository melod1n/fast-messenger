package com.meloda.fast.api.model.base.attachments

import com.meloda.fast.api.model.attachments.VkCall
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BaseVkCall(
    val initiator_id: Int,
    val receiver_id: Int,
    val state: String,
    val time: Int,
    val duration: Int,
    val video: Boolean
) {

    fun asVkCall() = VkCall(
        initiatorId = initiator_id,
        receiverId = receiver_id,
        state = state,
        time = time,
        duration = duration,
        isVideo = video
    )
}
