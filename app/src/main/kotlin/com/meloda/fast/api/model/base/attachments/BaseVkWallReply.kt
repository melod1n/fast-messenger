package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.meloda.fast.api.model.attachments.VkWallReply
import kotlinx.parcelize.Parcelize

@Parcelize
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
) : Parcelable {

    @Parcelize
    data class Likes(
        val count: Int,
        val can_like: Int,
        val user_likes: Int,
        val can_publish: Int
    ) : Parcelable

    fun asVkWallReply() = VkWallReply(id = id)

}