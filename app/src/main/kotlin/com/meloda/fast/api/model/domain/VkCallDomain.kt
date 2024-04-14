package com.meloda.fast.api.model.domain

import com.meloda.fast.R
import com.meloda.fast.api.model.data.AttachmentType
import com.meloda.fast.model.base.UiText

data class VkCallDomain(
    val initiatorId: Int,
    val receiverId: Int,
    val state: String,
    val time: Int,
    val duration: Int,
    val isVideo: Boolean
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.CALL

    override fun getUiText(): UiText = UiText.Resource(R.string.message_attachments_call)
}
