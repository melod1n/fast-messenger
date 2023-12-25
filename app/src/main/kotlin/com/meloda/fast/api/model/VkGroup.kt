package com.meloda.fast.api.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkGroup(
    val id: Int,
    val name: String,
    val screenName: String,
    val photo50: String?,
    val photo100: String?,
    val photo200: String?,
    val membersCount: Int?
) : Parcelable {

    override fun toString() = name.trim()
}
