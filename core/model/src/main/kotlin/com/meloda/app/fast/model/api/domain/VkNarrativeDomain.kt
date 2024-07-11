package com.meloda.app.fast.model.api.domain

import com.meloda.app.fast.model.api.data.AttachmentType

data class VkNarrativeDomain(
    val id: Int,
    val title: String?
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.NARRATIVE
}
