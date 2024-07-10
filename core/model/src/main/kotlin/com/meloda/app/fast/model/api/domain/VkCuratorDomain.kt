package com.meloda.app.fast.model.api.domain

import com.meloda.app.fast.model.api.data.AttachmentType

data class VkCuratorDomain(
    val id: Int,
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.CURATOR
}
