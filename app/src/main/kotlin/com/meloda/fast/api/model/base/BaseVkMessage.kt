package com.meloda.fast.api.model.base

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.base.attachments.BaseVkAttachmentItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkMessage(
    val date: Int,
    @SerializedName("from_id")
    val fromId: Int,
    val id: Int,
    val out: Int,
    @SerializedName("peer_id")
    val peerId: Int,
    val text: String,
    @SerializedName("conversation_message_id")
    val conversationMessageId: Int,
    @SerializedName("fwd_messages")
    val fwdMessages: List<BaseVkMessage>? = listOf(),
    val important: Boolean,
    @SerializedName("random_id")
    val randomId: Int,
    val attachments: List<BaseVkAttachmentItem> = listOf(),
    @SerializedName("is_hidden")
    val isHidden: Boolean,
    val payload: String,
    val geo: Geo?,
    val action: Action?,
    val ttl: Int
) : Parcelable {

    fun asVkMessage() = VkMessage(
        id = id,
        text = if (text.isBlank()) null else text,
        isOut = out == 1,
        peerId = peerId,
        fromId = fromId,
        date = date,
        action = action?.type,
        actionMemberId = action?.memberId,
        actionText = action?.text,
        actionConversationMessageId = action?.conversationMessageId,
        actionMessage = action?.message,
        geoType = geo?.type
    ).also {
        it.attachments = VkUtils.parseAttachments(attachments)
        it.forwards = VkUtils.parseForwards(fwdMessages)
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
        @SerializedName("member_id")
        val memberId: Int?,
        val text: String?,
        @SerializedName("conversation_message_id")
        val conversationMessageId: Int?,
        val message: String?
    ) : Parcelable

}
