package com.meloda.app.fast.model.api.data

import com.meloda.app.fast.model.api.domain.OnlineStatus
import com.meloda.app.fast.model.api.domain.VkUser
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkUserData(
    @Json(name = "id") val id: Int,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String,
    @Json(name = "can_access_closed") val canAccessClosed: Boolean,
    @Json(name = "is_closed") val isClosed: Boolean,
    @Json(name = "can_invite_to_chats") val canInviteToChats: Boolean = false,
    @Json(name = "sex") val sex: Int?,
    @Json(name = "photo_50") val photo50: String?,
    @Json(name = "photo_100") val photo100: String?,
    @Json(name = "photo_200") val photo200: String?,
    @Json(name = "online") val online: Int?,
    @Json(name = "online_info") val onlineInfo: OnlineInfo?,
    @Json(name = "screen_name") val screenName: String,
    @Json(name = "bdate") val birthday: String?
    //...other fields
) {

    @JsonClass(generateAdapter = true)
    data class OnlineInfo(
        @Json(name = "visible") val visible: Boolean,
        @Json(name = "status") val status: String?,
        @Json(name = "last_seen") val lastSeen: Int?,
        @Json(name = "is_online") val isOnline: Boolean?,
        @Json(name = "online_mobile") val onlineMobile: Boolean?,
        @Json(name = "app_id") val appId: Int?
    )

    fun mapToDomain() = VkUser(
        id = id,
        firstName = firstName,
        lastName = lastName,
        // TODO: 05/05/2024, Danil Nikolaev: improve
        onlineStatus = when {
            online != 1 -> OnlineStatus.Offline
            onlineInfo?.onlineMobile == true -> {
                OnlineStatus.OnlineMobile(appId = onlineInfo.appId)
            }

            else -> {
                OnlineStatus.Online(appId = onlineInfo?.appId)
            }
        },
        photo50 = photo50,
        photo100 = photo100,
        photo200 = photo200,
        lastSeen = onlineInfo?.lastSeen,
        lastSeenStatus = onlineInfo?.status,
        birthday = birthday
    )
}
