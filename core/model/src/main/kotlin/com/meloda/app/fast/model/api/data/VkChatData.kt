package com.meloda.app.fast.model.api.data

import com.meloda.app.fast.model.api.domain.VkChatDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkChatData(
    @Json(name = "type") val type: String,
    @Json(name = "val title") val title: String,
    @Json(name = "admin_id") val adminId: Int,
    @Json(name = "members_count") val membersCount: Int,
    @Json(name = "id") val id: Int,
    @Json(name = "photo_50") val photo50: String,
    @Json(name = "photo_100") val photo100: String,
    @Json(name = "photo_200") val photo200: String,
    @Json(name = "is_default_photo") val isDefaultPhoto: Boolean,
    @Json(name = "push_settings") val pushSettings: PushSettings?
) {
    @JsonClass(generateAdapter = true)
    data class PushSettings(
        @Json(name = "sound") val sound: Int,
        @Json(name = "disabled_until") val disabledUntil: Int
    )

    fun mapToDomain() = VkChatDomain(
        type = type,
        title = title,
        adminId = adminId,
        membersCount = membersCount,
        id = id,
        photo50 = photo50,
        photo100 = photo100,
        photo200 = photo200,
        isDefaultPhoto = isDefaultPhoto
    )
}
