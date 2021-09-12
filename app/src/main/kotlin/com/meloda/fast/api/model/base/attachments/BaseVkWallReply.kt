package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkWallReply(
    val id: Int,
    @SerializedName("from_id")
    val fromId: Int,
    val date: Int,
    val text: String,
    @SerializedName("post_id")
    val postId: Int,
    @SerializedName("owner_id")
    val ownerId: Int,
    @SerializedName("parents_stack")
    val parentsStack: List<Int>,
    val likes: Likes,
    @SerializedName("reply_to_user")
    val replyToUser: Int?,
    @SerializedName("reply_to_comment")
    val replyToComment: Int?
) : Parcelable {


    @Parcelize
    data class Likes(
        val count: Int,
        @SerializedName("can_like")
        val canLike: Int,
        @SerializedName("user_likes")
        val userLikes: Int,
        @SerializedName("can_publish")
        val canPublish: Int
    ) : Parcelable

}