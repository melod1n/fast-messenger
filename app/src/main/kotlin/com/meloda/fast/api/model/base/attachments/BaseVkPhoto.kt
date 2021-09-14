package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meloda.fast.api.model.attachments.VkPhoto
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkPhoto(
    @SerializedName("album_id")
    val albumId: Int,
    val date: Int,
    val id: Int,
    @SerializedName("owner_id")
    val ownerId: Int,
    @SerializedName("has_tags")
    val hasTags: Boolean,
    @SerializedName("access_key")
    val accessKey: String?,
    val sizes: List<Size>,
    val text: String,
    @SerializedName("user_id")
    val userId: Int?
) : BaseVkAttachment() {

    fun asVkPhoto() = VkPhoto(
        albumId = albumId,
        date = date,
        id = id,
        ownerId = ownerId,
        hasTags = hasTags,
        accessKey = accessKey,
        sizes = sizes,
        text = text,
        userId = userId
    )

}

@Parcelize
data class Size(
    val height: Int,
    val width: Int,
    val type: String,
    @SerializedName("url", alternate = ["src"])
    val url: String,
) : Parcelable