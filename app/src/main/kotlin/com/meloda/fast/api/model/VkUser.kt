package com.meloda.fast.api.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkUser(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val online: Boolean,
    val photo200: String?,
    val lastSeen: Int?,
    val lastSeenStatus: String?,
    val birthday: String?
) : Parcelable {

    override fun toString() = fullName

    val fullName get() = "$firstName $lastName".trim()
}
