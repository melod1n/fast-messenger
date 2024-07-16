package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType

data object VkUnknownAttachment : VkAttachment {
    override val type: AttachmentType = AttachmentType.UNKNOWN
}
