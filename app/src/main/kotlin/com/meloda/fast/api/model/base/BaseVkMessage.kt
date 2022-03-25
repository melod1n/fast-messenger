package com.meloda.fast.api.model.base

import android.os.Parcelable
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.base.attachments.BaseVkAttachmentItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkMessage(
    val id: Int,
    val peer_id: Int,
    val date: Int,
    val from_id: Int,
    val out: Int,
    val text: String,
    val conversation_message_id: Int,
    val fwd_messages: List<BaseVkMessage>? = emptyList(),
    val important: Boolean,
    val random_id: Int,
    val attachments: List<BaseVkAttachmentItem> = emptyList(),
    val is_hidden: Boolean,
    val payload: String,
    val geo: Geo?,
    val action: Action?,
    val ttl: Int,
    val reply_message: BaseVkMessage?
) : Parcelable {

    fun asVkMessage() = VkMessage(
        id = id,
        text = text.ifBlank { null },
        isOut = out == 1,
        peerId = peer_id,
        fromId = from_id,
        date = date,
        randomId = random_id,
        action = action?.type,
        actionMemberId = action?.member_id,
        actionText = action?.text,
        actionConversationMessageId = action?.conversation_message_id,
        actionMessage = action?.message,
        geoType = geo?.type,
        important = important
    ).also {
        it.attachments = VkUtils.parseAttachments(attachments)
        it.forwards = VkUtils.parseForwards(fwd_messages)
        it.replyMessage = VkUtils.parseReplyMessage(reply_message)
    }

    @Parcelize
    data class Geo(
        val type: String,
        val coordinates: Coordinates,
        val place: Place
    ) : Parcelable {


        @Parcelize
        data class Coordinates(val latitude: Float, val longitude: Float) : Parcelable

        @Parcelize
        data class Place(val country: String, val city: String, val title: String) : Parcelable
    }

    @Parcelize
    data class Action(
        val type: String,
        val member_id: Int?,
        val text: String?,
        val conversation_message_id: Int?,
        val message: String?
    ) : Parcelable

}
