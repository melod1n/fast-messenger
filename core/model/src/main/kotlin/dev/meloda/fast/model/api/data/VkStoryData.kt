package dev.meloda.fast.model.api.data

import dev.meloda.fast.model.api.domain.VkStoryDomain
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkStoryData(
    val id: Long,
    val owner_id: Long,
    val access_key: String?,
    val can_comment: Int?,
    val can_reply: Int?,
    val can_like: Boolean?,
    val can_share: Int?,
    val can_hide: Int?,
    val date: Int,
    val expires_at: Int,
    val is_ads: Boolean?,
    val photo: VkPhotoData?,
    val replies: Replies?,
    val is_one_time: Boolean?,
    val track_code: String?,
    val type: String?,
    val views: Int?,
    val likes_count: Int?,
    val is_restricted: Boolean?,
) : VkAttachmentData {

    @JsonClass(generateAdapter = true)
    data class Replies(
        val count: Int,
        val new: Int?
    )

    fun toDomain() = VkStoryDomain(
        id = id,
        ownerId = owner_id,
        date = date,
        photo = photo?.toDomain()
    )
}
