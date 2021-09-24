package com.meloda.fast.api.model.attachments

import com.meloda.fast.api.model.base.attachments.BaseVkPhoto
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkPhoto(
    val albumId: Int,
    val date: Int,
    val id: Int,
    val ownerId: Int,
    val hasTags: Boolean,
    val accessKey: String?,
    val sizes: List<BaseVkPhoto.Size>,
    val text: String,
    val userId: Int?
) : VkAttachment() {

    @IgnoredOnParcel
    val className: String = this::class.java.name

    fun sizeOfType(type: Char): BaseVkPhoto.Size? {
        for (size in sizes) {
            if (size.type == type.toString())
                return size
        }

        return null
    }

}