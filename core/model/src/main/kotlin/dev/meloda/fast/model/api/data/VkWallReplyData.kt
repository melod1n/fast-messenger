package dev.meloda.fast.model.api.data

import dev.meloda.fast.model.api.domain.VkWallReplyDomain
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkWallReplyData(
    val id: Long,
    val from_id: Long,
    val date: Int,
    val text: String,
    val post_id: Long,
    val owner_id: Long,
    val parents_stack: List<Int>,
    val likes: Likes,
    val reply_to_user: Int?,
    val reply_to_comment: Int?
) {

    @JsonClass(generateAdapter = true)
    data class Likes(
        val count: Int,
        val can_like: Int,
        val user_likes: Int,
        val can_publish: Int
    )

    fun toDomain() = VkWallReplyDomain(id = id)
}
