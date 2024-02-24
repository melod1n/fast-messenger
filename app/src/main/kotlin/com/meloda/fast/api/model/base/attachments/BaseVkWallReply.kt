package com.meloda.fast.api.model.base.attachments

import com.meloda.fast.api.model.attachments.VkWallReply
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BaseVkWallReply(
    val id: Int,
    val from_id: Int,
    val date: Int,
    val text: String,
    val post_id: Int,
    val owner_id: Int,
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

    fun asVkWallReply() = VkWallReply(id = id)

}
