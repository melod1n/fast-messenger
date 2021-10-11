package com.meloda.fast.api.model.attachments

import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.base.attachments.BaseVkVideo
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkVideo(
    val id: Int,
    val ownerId: Int,
    val images: List<BaseVkVideo.Image>,
    val firstFrames: List<BaseVkVideo.FirstFrame>?,
    val accessKey: String?
) : VkAttachment() {

    @IgnoredOnParcel
    val className: String = this::class.java.name

    fun imageForWidth(width: Int): BaseVkVideo.Image? {
        return images.find { it.width == width }
    }

    override fun asString(withAccessKey: Boolean) = VkUtils.attachmentToString(
        attachmentClass = this::class.java,
        id = id,
        ownerId = ownerId,
        withAccessKey = withAccessKey,
        accessKey = accessKey
    )

}