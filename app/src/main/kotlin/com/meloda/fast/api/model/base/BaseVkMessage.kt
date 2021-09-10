package com.meloda.fast.api.model.base

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meloda.fast.api.model.VkMessage
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

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
    val fwdMessages: List<BaseVkMessage> = listOf(),
    val important: Boolean,
    @SerializedName("random_id")
    val randomId: Int,
    val attachments: @RawValue List<Any> = listOf(),
    @SerializedName("is_hidden")
    val isHidden: Boolean,
    val payload: String,
    val geo: Geo?
) : Parcelable {

    fun asVkMessage() = VkMessage(
        id = id,
        text = text,
        isOut = out == 1,
        peerId = peerId,
        fromId = fromId,
        date = date
    )

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

}
