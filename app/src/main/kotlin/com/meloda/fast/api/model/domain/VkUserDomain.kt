package com.meloda.fast.api.model.domain

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
    }
}
