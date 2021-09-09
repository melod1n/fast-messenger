package com.meloda.fast.api.model.attachments

import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class VKLinkAttachment(
    val url: String,
    val title: String,
    val caption: String,
    val photo: VKPhotoAttachment,
    val target: String,
    @SerializedName("is_favorite")
    val isFavorite: Boolean
) : BaseVKAttachment()