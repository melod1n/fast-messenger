package com.meloda.app.fast.model.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.meloda.app.fast.model.api.domain.OnlineStatus
import com.meloda.app.fast.model.api.domain.VkUser

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

fun VkUserEntity.asExternalModel(): VkUser = VkUser(
    id = id,
    firstName = firstName,
    lastName = lastName,
    onlineStatus = when {
        !isOnline -> OnlineStatus.Offline
        !isOnlineMobile -> OnlineStatus.Online(onlineAppId)
        else -> OnlineStatus.OnlineMobile(onlineAppId)
    },
    photo50 = photo50,
    photo100 = photo100,
    photo200 = photo200,
    lastSeen = lastSeen,
    lastSeenStatus = lastSeenStatus,
    birthday = birthday
)
