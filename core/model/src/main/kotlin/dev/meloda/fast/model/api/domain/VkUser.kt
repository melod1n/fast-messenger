package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.database.VkUserEntity

data class VkUser(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val onlineStatus: OnlineStatus,
    val photo50: String?,
    val photo100: String?,
    val photo200: String?,
    val photo400Orig: String?,
    val lastSeen: Int?,
    val lastSeenStatus: String?,
    val birthday: String?,
) {
    override fun toString() = fullName

    val fullName get() = "$firstName $lastName".trim()
}

sealed class OnlineStatus(open val appId: Long?) {
    data class Online(override val appId: Long? = null) : OnlineStatus(appId)
    data class OnlineMobile(override val appId: Long? = null) : OnlineStatus(appId)
    data object Recently : OnlineStatus(null)
    data object LastWeek : OnlineStatus(null)
    data object LastMonth : OnlineStatus(null)
    data object Offline : OnlineStatus(null)

    fun isOnline(): Boolean = this is Online || this is OnlineMobile
    fun isMobile(): Boolean = this is OnlineMobile
}

fun VkUser.asEntity(): VkUserEntity = VkUserEntity(
    id = id,
    firstName = firstName,
    lastName = lastName,
    isOnline = onlineStatus.isOnline(),
    isOnlineMobile = onlineStatus.isMobile(),
    onlineAppId = onlineStatus.appId,
    lastSeen = lastSeen,
    lastSeenStatus = lastSeenStatus,
    birthday = birthday,
    photo50 = photo50,
    photo100 = photo100,
    photo200 = photo200,
    photo400Orig = photo400Orig
)
