package com.meloda.fast.api.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkGroup(
    val id: Int,
    val name: String,
    val screenName: String,
    val photo200: String?,
    val membersCount: Int?
) : Parcelable {

    override fun toString() = name.trim()
}
