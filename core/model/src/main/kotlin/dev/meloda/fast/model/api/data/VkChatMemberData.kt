package dev.meloda.fast.model.api.data

import dev.meloda.fast.model.api.domain.VkChatMemberDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkChatMemberData(
    @Json(name = "member_id") val memberId: Long,
    @Json(name = "invited_by") val invitedBy: Int,
    @Json(name = "join_date") val joinDate: Int,
    @Json(name = "is_admin") val isAdmin: Boolean?,
    @Json(name = "is_owner") val isOwner: Boolean?,
    @Json(name = "can_kick") val canKick: Boolean?
) {

    fun mapToDomain() = VkChatMemberDomain(
        memberId = memberId,
        invitedBy = invitedBy,
        joinDate = joinDate,
        isAdmin = isAdmin == true,
        isOwner = isOwner == true,
        canKick = canKick == true
    )
}
