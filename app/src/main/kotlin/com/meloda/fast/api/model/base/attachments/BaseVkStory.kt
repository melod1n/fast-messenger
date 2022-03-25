package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.meloda.fast.api.model.attachments.VkStory
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkStory(
    val id: Int,
    val owner_id: Int,
    val access_key: String,
    val can_comment: Int,
    val can_reply: Int,
    val can_like: Boolean,
    val can_share: Int,
    val can_hide: Int,
    val date: Int,
    val expires_at: Int,
    val is_ads: Boolean,
    val photo: BaseVkPhoto?,
    val replies: Replies,
    val is_one_time: Boolean,
    val track_code: String,
    val type: String,
    val views: Int,
    val likes_count: Int,
    val reaction_set_id: String,
    val is_restricted: Boolean,
    val no_sound: Boolean,
    val need_mute: Boolean,
    val mute_reply: Boolean,
    val can_ask: Int,
    val can_ask_anonymous: Int,
    val preloading_enabled: Boolean,
    val narratives_count: Int,
    val can_use_in_narrative: Boolean
) : BaseVkAttachment() {

    fun asVkStory() = VkStory(
        id = id,
        ownerId = owner_id,
        date = date,
        photo = photo?.asVkPhoto()
    )

    @Parcelize
    data class Replies(
        val count: Int,
        val new: Int
    ) : Parcelable

}