package com.meloda.fast.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class VkGroupDB(
    @PrimaryKey val id: Int,
    val name: String,
    val screenName: String,
    val photo50: String?,
    val photo100: String?,
    val photo200: String?,
    val membersCount: Int?
)
