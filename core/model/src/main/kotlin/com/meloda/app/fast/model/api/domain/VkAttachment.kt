package com.meloda.app.fast.model.api.domain

import com.meloda.app.fast.model.api.data.AttachmentType

interface VkAttachment {
    val type: AttachmentType get() = AttachmentType.UNKNOWN
}
