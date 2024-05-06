package com.meloda.app.fast.model.api.domain

import com.meloda.app.fast.model.api.data.AttachmentType

data class VkGroupCallDomain(
    val initiatorId: Int
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.GROUP_CALL_IN_PROGRESS
}
