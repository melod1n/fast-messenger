package com.meloda.fast.api.model.attachments

import com.meloda.fast.api.model.base.attachments.BaseVkVideo
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkVideo(
    val id: Int,
    val images: List<BaseVkVideo.Image>,
    val firstFrames: List<BaseVkVideo.FirstFrame>
) : VkAttachment() {

    @IgnoredOnParcel
    val className: String = this::class.java.name

    fun imageForWidth(width: Int): BaseVkVideo.Image? {
        return images.find { it.width == width }
    }

}