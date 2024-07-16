package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType

data class VkMiniAppDomain(
    val link: String
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.MINI_APP
}
