package com.meloda.fast.api.model.base

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meloda.fast.api.model.VkUser
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkUser(
    val id: Int,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("can_access_closed")
    val canAccessClosed: Boolean,
    @SerializedName("is_closed")
    val isClosed: Boolean,
    @SerializedName("can_invite_to_chats")
    val canInviteToChats: Boolean,
    val sex: Int?,
    @SerializedName("photo_50")
    val photo50: String?,
    @SerializedName("photo_100")
    val photo100: String?,
    @SerializedName("photo_200")
    val photo200: String?,
    val online: Int?,
    @SerializedName("online_info")
    val onlineInfo: OnlineInfo?,
    @SerializedName("screen_name")
    val screenName: String
    //...other fields
) : Parcelable {

    @Parcelize
    data class OnlineInfo(
        val visible: Boolean,
        val status: String,
        @SerializedName("last_seen")
        val lastSeen: Int?,
        @SerializedName("is_online")
        val isOnline: Boolean?,
        @SerializedName("online_mobile")
        val isOnlineMobile: Boolean?,
        @SerializedName("app_id")
        val appId: Int?
    ) : Parcelable

    fun asVkUser() = VkUser(
        id = id,
        firstName = firstName,
        lastName = lastName
    )

}
