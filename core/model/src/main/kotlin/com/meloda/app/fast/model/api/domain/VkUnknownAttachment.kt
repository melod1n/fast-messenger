package com.meloda.app.fast.model.api.domain

import com.meloda.app.fast.model.api.data.AttachmentType

data object VkUnknownAttachment : VkAttachment {
    override val type: AttachmentType = AttachmentType.UNKNOWN
}
