package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
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
    val accessKey: String,
    val sizes: List<Size>,
    val text: String,
    @SerializedName("user_id")
    val userId: Int?
) : BaseVkAttachment()

@Parcelize
data class Size(
    val height: Int,
    val width: Int,
    val type: String,
    @SerializedName("url", alternate = ["src"])
    val url: String,
) : Parcelable