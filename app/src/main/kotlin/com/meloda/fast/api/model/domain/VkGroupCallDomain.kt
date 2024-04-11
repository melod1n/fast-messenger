package com.meloda.fast.api.model.domain

import com.meloda.fast.R
import com.meloda.fast.api.model.data.AttachmentType
import com.meloda.fast.model.base.UiText

data class VkGroupCallDomain(
    val initiatorId: Int
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.GROUP_CALL_IN_PROGRESS

    val className: String = this::class.java.name

    override fun getUiText(): UiText =
        UiText.Resource(R.string.message_attachments_call_in_progress)
}
