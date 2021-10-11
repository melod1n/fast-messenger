package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.meloda.fast.api.model.attachments.VkGraffiti
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkGraffiti(
    val id: Int,
    val owner_id: Int,
    val url: String,
    val width: Int,
    val height: Int,
    val access_key: String
) : Parcelable {

    fun asVkGraffiti() = VkGraffiti(
        id = id,
        ownerId = owner_id,
        url = url,
        width = width,
        height = height,
        accessKey = access_key
    )

}