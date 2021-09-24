package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkGraffiti(
    val id: Int,
    val owner_id: Int,
    val url: String,
    val width: Int,
    val height: Int,
    val access_key: String
) : Parcelable