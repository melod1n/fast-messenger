package com.meloda.fast.api.model.request

import android.os.Parcelable
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
            extended?.let { this["extended"] = (if (it) 1 else 0).toString() }
            startMessageId?.let { this["start_message_id"] = it.toString() }
            rev?.let { this["rev"] = (if (it) 1 else 0).toString() }
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
            disableMentions?.let { this["disable_mentions"] = (if (it) 1 else 0).toString() }
            dontParseLinks?.let { this["dont_parse_links"] = (if (it) 1 else 0).toString() }
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
            "important" to (if (important) 1 else 0).toString()
        )

}

@Parcelize
data class MessagesGetLongPollServerRequest(
    val needPts: Boolean,
    val version: Int
) : Parcelable {

    val map
        get() = mutableMapOf(
            "need_pts" to (if (needPts) 1 else 0).toString(),
            "version" to version.toString()
        )
}