package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkGift(
    val id: Int,
    val thumb_256: String?,
    val thumb_96: String?,
    val thumb_48: String
) : Parcelable