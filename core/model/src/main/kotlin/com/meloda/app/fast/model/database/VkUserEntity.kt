package com.meloda.app.fast.model.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class VkUserEntity(
    @PrimaryKey val id: Int,
    val firstName: String,
    val lastName: String,
    val isOnline: Boolean,
    val isOnlineMobile: Boolean,
    val onlineAppId: Int?,
    val lastSeen: Int?,
    val lastSeenStatus: String?,
    val birthday: String?,
    val photo50: String?,
    val photo100: String?,
    val photo200: String?
)
