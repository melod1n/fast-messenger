package dev.meloda.fast.model.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.meloda.fast.model.api.data.parseUserOnlineState
import dev.meloda.fast.model.api.domain.VkUser

@Entity(tableName = "users")
data class VkUserEntity(
    @PrimaryKey val id: Long,
    val firstName: String,
    val lastName: String,
    val isOnline: Boolean,
    val isOnlineMobile: Boolean,
    val onlineAppId: Long?,
    val lastSeen: Int?,
    val lastSeenStatus: String?,
    val birthday: String?,
    val photo50: String?,
    val photo100: String?,
    val photo200: String?,
    val photo400Orig: String?
)

fun VkUserEntity.asExternalModel(): VkUser = VkUser(
    id = id,
    firstName = firstName,
    lastName = lastName,
    onlineStatus = parseUserOnlineState(
        isOnline = isOnline,
        isOnlineMobile = isOnlineMobile,
        status = lastSeenStatus,
        appId = onlineAppId
    ),
    photo50 = photo50,
    photo100 = photo100,
    photo200 = photo200,
    photo400Orig = photo400Orig,
    lastSeen = lastSeen,
    lastSeenStatus = lastSeenStatus,
    birthday = birthday
)
