package com.meloda.fast.api.model.attachments

import com.meloda.fast.api.VkUtils
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkFile(
    val id: Int,
    val ownerId: Int,
    val title: String,
    val ext: String,
    val size: Int,
    val url: String,
    val accessKey: String?
) : VkAttachment() {

    @IgnoredOnParcel
    val className: String = this::class.java.name

    override fun asString(withAccessKey: Boolean) = VkUtils.attachmentToString(
        attachmentClass = this::class.java,
        id = id,
        ownerId = ownerId,
        withAccessKey = withAccessKey,
        accessKey = accessKey
    )
}