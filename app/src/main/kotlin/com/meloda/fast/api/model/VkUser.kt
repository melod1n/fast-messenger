package com.meloda.fast.api.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "users")
@Parcelize
data class VkUser(
    @PrimaryKey(autoGenerate = false)
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
