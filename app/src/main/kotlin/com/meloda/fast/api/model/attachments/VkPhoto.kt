package com.meloda.fast.api.model.attachments

import com.meloda.fast.api.model.base.attachments.Size
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkPhoto(
    val albumId: Int,
    val date: Int,
    val id: Int,
    val ownerId: Int,
    val hasTags: Boolean,
    val accessKey: String?,
    val sizes: List<Size>,
    val text: String,
    val userId: Int?
) : VkAttachment() {

    fun sizeOfType(type: Char): Size? {
        for (size in sizes) {
            if (size.type == type.toString())
                return size
        }

        return null
    }

}