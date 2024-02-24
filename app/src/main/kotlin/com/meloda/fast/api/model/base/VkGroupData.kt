package com.meloda.fast.api.model.base

import com.meloda.fast.api.model.VkGroupDomain
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkGroupData(
    val id: Int,
    val name: String,
    val screen_name: String,
    val is_closed: Int,
    val type: String,
    val is_admin: Int,
    val is_member: Int,
    val is_advertiser: Int,
    val photo_50: String?,
    val photo_100: String?,
    val photo_200: String?,
    val members_count: Int?
) {

    fun mapToDomain() = VkGroupDomain(
        id = -id,
        name = name,
        screenName = screen_name,
        photo50 = photo_50,
        photo100 = photo_100,
        photo200 = photo_200,
        membersCount = members_count
    )
}
