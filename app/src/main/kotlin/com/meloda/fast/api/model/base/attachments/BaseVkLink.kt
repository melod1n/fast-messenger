package com.meloda.fast.api.model.base.attachments

import com.meloda.fast.api.model.attachments.VkLink
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkLink(
    val url: String,
    val title: String?,
    val caption: String?,
    val photo: BaseVkPhoto?,
    val target: String?,
    val is_favorite: Boolean
) : BaseVkAttachment() {

    fun asVkLink() = VkLink(
        url = url,
        title = title,
        caption = caption,
        photo = photo?.asVkPhoto(),
        target = target,
        isFavorite = is_favorite
    )

}