package com.meloda.fast.api.model.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkGroupDomain(
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
