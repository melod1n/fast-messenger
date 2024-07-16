package dev.meloda.fast.model.api.data

import dev.meloda.fast.model.api.domain.VkGroupDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlin.math.abs

@JsonClass(generateAdapter = true)
data class VkGroupData(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "screen_name") val screenName: String,
    @Json(name = "is_closed") val isClosed: Int?,
    @Json(name = "type") val type: String,
    @Json(name = "is_admin") val isAdmin: Int?,
    @Json(name = "is_member") val isMember: Int?,
    @Json(name = "is_advertiser") val isAdvertiser: Int?,
    @Json(name = "photo_50") val photo50: String?,
    @Json(name = "photo_100") val photo100: String?,
    @Json(name = "photo_200") val photo200: String?,
    @Json(name = "members_count") val membersCount: Int?
) {

    fun mapToDomain() = VkGroupDomain(
        id = abs(id),
        name = name,
        screenName = screenName,
        photo50 = photo50,
        photo100 = photo100,
        photo200 = photo200,
        membersCount = membersCount
    )
}
