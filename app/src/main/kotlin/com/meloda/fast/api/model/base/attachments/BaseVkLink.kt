package com.meloda.fast.api.model.base.attachments

import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkLink(
    val url: String,
    val title: String,
    val caption: String,
    val photo: BaseVkPhoto,
    val target: String,
    @SerializedName("is_favorite")
    val isFavorite: Boolean
) : BaseVkAttachment()