package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkGraffiti(
    val id: Int,
    @SerializedName("owner_id")
    val ownerId: Int,
    val url: String,
    val width: Int,
    val height: Int,
    @SerializedName("access_key")
    val accessKey: String
) : Parcelable