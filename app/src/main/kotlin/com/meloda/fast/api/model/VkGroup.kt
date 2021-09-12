package com.meloda.fast.api.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "groups")
@Parcelize
data class VkGroup(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val name: String,
    val screenName: String,
    val photo200: String?
): Parcelable {

    override fun toString() = name.trim()

}
