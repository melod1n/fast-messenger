package com.meloda.app.fast.model.api.domain

import com.meloda.app.fast.model.api.data.AttachmentType

data class VkCallDomain(
    val initiatorId: Int,
    val receiverId: Int,
    val state: String,
    val time: Int,
    val duration: Int,
    val isVideo: Boolean
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.CALL
}
