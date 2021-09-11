package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkGift(
    val id: Int,
    @SerializedName("thumb_256")
    val thumb256: String?,
    @SerializedName("thumb_96")
    val thumb96: String?,
    @SerializedName("thumb_48")
    val thumb48: String
) : Parcelable