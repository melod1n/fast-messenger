package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.meloda.fast.api.model.attachments.VkGift
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkGift(
    val id: Int,
    val thumb_256: String?,
    val thumb_96: String?,
    val thumb_48: String
) : Parcelable {

    fun asVkGift() = VkGift(
        id = id,
        thumb256 = thumb_256,
        thumb96 = thumb_96,
        thumb48 = thumb_48
    )

}