package com.meloda.fast.api.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class VkGroup(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val name: String,
    val screenName: String,
    val photo200: String?
) {

    override fun toString() = name.trim()

}
