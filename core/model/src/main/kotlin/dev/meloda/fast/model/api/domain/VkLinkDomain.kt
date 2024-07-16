package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType

data class VkLinkDomain(
    val url: String,
    val title: String?,
    val caption: String?,
    val photo: VkPhotoDomain?,
    val target: String?,
    val isFavorite: Boolean
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.LINK
}
