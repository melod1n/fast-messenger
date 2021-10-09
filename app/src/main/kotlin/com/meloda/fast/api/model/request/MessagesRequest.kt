package com.meloda.fast.api.model.request

import android.os.Parcelable
import com.meloda.fast.api.ApiExtensions.intString
import kotlinx.parcelize.Parcelize

@Parcelize
data class MessagesGetHistoryRequest(
    val count: Int? = null,
    val offset: Int? = null,
    val peerId: Int,
    val extended: Boolean? = null,
    val startMessageId: Int? = null,
    val rev: Boolean? = null,
    val fields: String? = null,
) : Parcelable {

    val map
        get() = mutableMapOf(
            "peer_id" to peerId.toString()
        ).apply {
            count?.let { this["count"] = it.toString() }
            offset?.let { this["offset"] = it.toString() }
            extended?.let { this["extended"] = it.intString }
            startMessageId?.let { this["start_message_id"] = it.toString() }
            rev?.let { this["rev"] = it.intString }
            fields?.let { this["fields"] = it }
        }

}

@Parcelize
data class MessagesSendRequest(
    val peerId: Int,
    val randomId: Int = 0,
    val message: String? = null,
    val lat: Int? = null,
    val lon: Int? = null,
    val replyTo: Int? = null,
    val stickerId: Int? = null,
    val disableMentions: Boolean? = null,
    val dontParseLinks: Boolean? = null
) : Parcelable {

    val map
        get() = mutableMapOf(
            "peer_id" to peerId.toString(),
            "random_id" to randomId.toString()
        ).apply {
            message?.let { this["message"] = it }
            lat?.let { this["lat"] = it.toString() }
            lon?.let { this["lon"] = it.toString() }
            replyTo?.let { this["reply_to"] = it.toString() }
            stickerId?.let { this["sticker_id"] = it.toString() }
            disableMentions?.let { this["disable_mentions"] = it.intString }
            dontParseLinks?.let { this["dont_parse_links"] = it.intString }
        }
}

@Parcelize
data class MessagesMarkAsImportantRequest(
    val messagesIds: List<Int>,
    val important: Boolean
) : Parcelable {

    val map
        get() = mutableMapOf(
            "message_ids" to messagesIds.joinToString { it.toString() },
            "important" to important.intString
        )

}

@Parcelize
data class MessagesGetLongPollServerRequest(
    val needPts: Boolean,
    val version: Int
) : Parcelable {

    val map
        get() = mutableMapOf(
            "need_pts" to needPts.intString,
            "version" to version.toString()
        )
}


@Parcelize
data class MessagesPinMessageRequest(
    val peerId: Int,
    val messageId: Int? = null,
    val conversationMessageId: Int? = null
) : Parcelable {

    val map
        get() = mutableMapOf(
            "peer_id" to peerId.toString()
        ).apply {
            messageId?.let { this["message_id"] = it.toString() }
            conversationMessageId?.let { this["conversation_message_id"] = it.toString() }
        }

}

@Parcelize
data class MessagesUnPinMessageRequest(val peerId: Int) : Parcelable {
    val map get() = mutableMapOf("peer_id" to peerId.toString())
}

@Parcelize
data class MessagesDeleteRequest(
    val peerId: Int,
    val messagesIds: List<Int>? = null,
    val conversationsMessagesIds: List<Int>? = null,
    val isSpam: Boolean? = null,
    val deleteForAll: Boolean? = null
) : Parcelable {

    val map
        get() = mutableMapOf(
            "peer_id" to peerId.toString()
        ).apply {
            isSpam?.let { this["spam"] = it.intString }
            deleteForAll?.let { this["delete_for_all"] = it.intString }
            messagesIds?.let {
                this["message_ids"] = it.joinToString { id -> id.toString() }
            }

            conversationsMessagesIds?.let {
                this["conversation_message_ids"] = it.joinToString { id -> id.toString() }
            }
        }

}