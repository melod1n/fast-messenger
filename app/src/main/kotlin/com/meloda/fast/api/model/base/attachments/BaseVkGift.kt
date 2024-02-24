package com.meloda.fast.api.model.base.attachments

import com.meloda.fast.api.model.attachments.VkGift
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BaseVkGift(
    val id: Int,
    val thumb_256: String?,
    val thumb_96: String?,
    val thumb_48: String
) {

    fun asVkGift() = VkGift(
        id = id,
        thumb256 = thumb_256,
        thumb96 = thumb_96,
        thumb48 = thumb_48
    )

}
