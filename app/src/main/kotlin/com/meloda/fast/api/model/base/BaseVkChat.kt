package com.meloda.fast.api.model.base

import android.os.Parcelable
import com.meloda.fast.api.model.VkChat
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkChat(
    val type: String,
    val title: String,
    val admin_id: Int,
    val members_count: Int,
    val id: Int,
    val users: List<BaseChatMember> = emptyList(),
    val photo_50: String,
    val photo_100: String,
    val photo_200: String,
    val is_default_photo: Boolean,
    val push_settings: PushSettings
) : Parcelable {

    fun asVkChat() = VkChat(
        type = type,
        title = title,
        adminId = admin_id,
        membersCount = members_count,
        id = id,
        members = users.map { it.asChatMember() },
        photo50 = photo_50,
        photo100 = photo_100,
        photo200 = photo_200,
        isDefaultPhoto = is_default_photo
    )

    @Parcelize
    data class PushSettings(
        val sound: Int,
        val disabled_until: Int
    ) : Parcelable


    @Parcelize
    data class BaseChatMember(
        val id: Int,
        val online: Int?,
        val name: String?,
        val first_name: String?,
        val last_name: String?,
        val is_closed: Boolean?,
        val can_access_closed: Boolean?,
        val type: String,
        val invited_by: Int
    ) : Parcelable {

        fun asChatMember() = VkChat.ChatMember(
            id = id,
            type = VkChat.ChatMember.ChatMemberType.parse(type),
            isOnline = if (online == null) null else online == 1,
            name = name,
            firstName = first_name,
            lastName = last_name,
            invitedBy = invited_by
        )
    }

}