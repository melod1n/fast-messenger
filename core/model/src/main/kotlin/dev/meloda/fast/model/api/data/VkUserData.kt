package dev.meloda.fast.model.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.domain.OnlineStatus
import dev.meloda.fast.model.api.domain.VkUser

@JsonClass(generateAdapter = true)
data class VkUserData(
    @Json(name = "id") val id: Long,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String,
    @Json(name = "can_access_closed") val canAccessClosed: Boolean,
    @Json(name = "is_closed") val isClosed: Boolean,
    @Json(name = "can_invite_to_chats") val canInviteToChats: Boolean = false,
    @Json(name = "sex") val sex: Int?,
    @Json(name = "photo_50") val photo50: String?,
    @Json(name = "photo_100") val photo100: String?,
    @Json(name = "photo_200") val photo200: String?,
    @Json(name = "photo_400_orig") val photo400Orig: String?,
    @Json(name = "online_info") val onlineInfo: OnlineInfo?,
    @Json(name = "last_seen") val lastSeen: LastSeen?,
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
        @Json(name = "online_mobile") val isOnlineMobile: Boolean?,
        @Json(name = "app_id") val appId: Long?
    )

    @JsonClass(generateAdapter = true)
    data class LastSeen(
        @Json(name = "platform") val platform: Int?,
        @Json(name = "time") val time: Int
    )

    fun mapToDomain() = VkUser(
        id = id,
        firstName = firstName,
        lastName = lastName,
        onlineStatus = parseUserOnlineState(
            isOnline = onlineInfo?.isOnline,
            isOnlineMobile = onlineInfo?.isOnlineMobile,
            status = onlineInfo?.status,
            appId = onlineInfo?.appId
        ),
        photo50 = photo50,
        photo100 = photo100,
        photo200 = photo200,
        photo400Orig = photo400Orig,
        lastSeen = onlineInfo?.lastSeen,
        lastSeenStatus = onlineInfo?.status,
        birthday = birthday
    )
}

fun parseUserOnlineState(
    isOnline: Boolean?,
    isOnlineMobile: Boolean?,
    status: String?,
    appId: Long?
): OnlineStatus {
    return when {
        isOnlineMobile == true -> OnlineStatus.OnlineMobile(appId)
        isOnline == true -> OnlineStatus.Online(appId)

        status != null -> {
            when (status) {
                "last_week" -> OnlineStatus.LastWeek
                "last_month" -> OnlineStatus.LastMonth

                else -> OnlineStatus.Recently
            }
        }

        else -> OnlineStatus.Offline
    }
}
