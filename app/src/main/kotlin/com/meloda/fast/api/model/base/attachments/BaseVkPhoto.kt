package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.meloda.fast.api.model.attachments.VkPhoto
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkPhoto(
    val album_id: Int,
    val date: Int,
    val id: Int,
    val owner_id: Int,
    val has_tags: Boolean,
    val access_key: String?,
    val sizes: List<Size>,
    val text: String?,
    val user_id: Int?,
    val lat: Double?,
    val long: Double?,
    val post_id: Int?
) : BaseVkAttachment() {

    fun asVkPhoto() = VkPhoto(
        albumId = album_id,
        date = date,
        id = id,
        ownerId = owner_id,
        hasTags = has_tags,
        accessKey = access_key,
        sizes = sizes,
        text = text,
        userId = user_id
    )

    @Parcelize
    data class Size(
        val height: Int,
        val width: Int,
        val type: String,
        val url: String
    ) : Parcelable

}