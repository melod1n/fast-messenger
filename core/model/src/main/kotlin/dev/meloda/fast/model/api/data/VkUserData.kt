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
    @Json(name = "can_access_closed") val canAccessClosed: Boolean = true,
    @Json(name = "is_closed") val isClosed: Boolean = false,
    @Json(name = "can_invite_to_chats") val canInviteToChats: Boolean = false,
    @Json(name = "sex") val sex: Int? = null,
    @Json(name = "photo_50") val photo50: String? = null,
    @Json(name = "photo_100") val photo100: String? = null,
    @Json(name = "photo_200") val photo200: String? = null,
    @Json(name = "photo_400_orig") val photo400Orig: String? = null,
    @Json(name = "online_info") val onlineInfo: OnlineInfo? = null,
    @Json(name = "last_seen") val lastSeen: LastSeen? = null,
    @Json(name = "screen_name") val screenName: String? = null,
    @Json(name = "bdate") val birthday: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "verified") val verified: Int? = null,
    @Json(name = "city") val city: TitleObject? = null,
    @Json(name = "country") val country: TitleObject? = null,
    @Json(name = "about") val about: String? = null,
    @Json(name = "site") val site: String? = null,
    @Json(name = "followers_count") val followersCount: Int? = null,
    @Json(name = "can_write_private_message") val canWritePrivateMessage: Int? = null
) {

    @JsonClass(generateAdapter = true)
    data class TitleObject(
        @Json(name = "id") val id: Long? = null,
        @Json(name = "title") val title: String? = null
    )

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
        birthday = birthday,
        status = status,
        screenName = screenName,
        city = city?.title,
        country = country?.title,
        about = about,
        site = site,
        verified = verified == 1,
        followersCount = followersCount,
        canWrite = canWritePrivateMessage != 0,
        sex = sex
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
