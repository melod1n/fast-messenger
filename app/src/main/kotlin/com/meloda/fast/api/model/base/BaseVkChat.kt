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
}