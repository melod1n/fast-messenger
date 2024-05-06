package com.meloda.app.fast.model.api.domain

import com.meloda.app.fast.model.database.VkUserEntity

data class VkUserDomain(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val onlineStatus: OnlineStatus,
    val photo50: String?,
    val photo100: String?,
    val photo200: String?,
    val lastSeen: Int?,
    val lastSeenStatus: String?,
    val birthday: String?,
) {

    override fun toString() = fullName

    val fullName get() = "$firstName $lastName".trim()

    sealed class OnlineStatus(open val appId: Int?) {
        data class Online(override val appId: Int?) : OnlineStatus(appId)
        data class OnlineMobile(override val appId: Int?) : OnlineStatus(appId)
        data object Offline : OnlineStatus(null)

        fun isOnline(): Boolean = this is Online || this is OnlineMobile
        fun isMobile(): Boolean = this is OnlineMobile
    }

    fun mapToDB(): VkUserEntity = VkUserEntity(
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
        photo200 = photo200
    )
}
