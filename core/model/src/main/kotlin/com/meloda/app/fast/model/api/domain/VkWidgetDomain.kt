package com.meloda.app.fast.model.api.domain

import com.meloda.app.fast.model.api.data.AttachmentType

data class VkWidgetDomain(
    val id: Int
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.WIDGET
}