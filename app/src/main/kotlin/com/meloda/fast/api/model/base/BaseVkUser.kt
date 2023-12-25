package com.meloda.fast.api.model.base

import android.os.Parcelable
import com.meloda.fast.api.model.VkUser
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkUser(
    val id: Int,
    val first_name: String,
    val last_name: String,
    val can_access_closed: Boolean,
    val is_closed: Boolean,
    val can_invite_to_chats: Boolean,
    val sex: Int?,
    val photo_50: String?,
    val photo_100: String?,
    val photo_200: String?,
    val online: Int?,
    val online_info: OnlineInfo?,
    val screen_name: String,
    val bdate: String?
    //...other fields
) : Parcelable {

    @Parcelize
    data class OnlineInfo(
        val visible: Boolean,
        val status: String,
        val last_seen: Int?,
        val is_online: Boolean?,
        val online_mobile: Boolean?,
        val app_id: Int?
    ) : Parcelable

    fun mapToDomain() = VkUser(
        id = id,
        firstName = first_name,
        lastName = last_name,
        online = online == 1,
        photo50 = photo_50,
        photo100 = photo_100,
        photo200 = photo_200,
        lastSeen = online_info?.last_seen,
        lastSeenStatus = online_info?.status,
        birthday = bdate
    )
}
