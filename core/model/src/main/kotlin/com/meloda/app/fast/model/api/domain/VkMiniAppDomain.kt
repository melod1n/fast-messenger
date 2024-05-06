package com.meloda.app.fast.model.api.domain

import com.meloda.app.fast.model.api.data.AttachmentType

data class VkMiniAppDomain(
    val link: String
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.MINI_APP
}
