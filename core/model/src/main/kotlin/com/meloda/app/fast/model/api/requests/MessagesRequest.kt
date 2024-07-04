package com.meloda.app.fast.model.api.requests

import com.meloda.app.fast.model.api.asInt
import com.meloda.app.fast.model.api.domain.VkAttachment

data class MessagesGetHistoryRequest(
    val count: Int? = null,
    val offset: Int? = null,
    val peerId: Int,
    val extended: Boolean? = null,
    val startMessageId: Int? = null,
    val rev: Boolean? = null,
    val fields: String? = null,
) {

    val map: Map<String, String>
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

data class MessagesSendRequest(
    val peerId: Int,
    val randomId: Int = 0,
    val message: String?,
    val lat: Int? = null,
    val lon: Int? = null,
    val replyTo: Int? = null,
    val stickerId: Int? = null,
    val disableMentions: Boolean? = null,
    val doNotParseLinks: Boolean? = null,
    val silent: Boolean? = null,
    val attachments: List<VkAttachment>? = null
) {

    val map: Map<String, String>
        get() = mutableMapOf(
            "peer_id" to peerId.toString(),
            "random_id" to randomId.toString()
        ).apply {
            message?.let { this["message"] = it }
            lat?.let { this["lat"] = it.toString() }
            lon?.let { this["lon"] = it.toString() }
            replyTo?.let { this["reply_to"] = it.toString() }
            stickerId?.let { this["sticker_id"] = it.toString() }
            disableMentions?.let { this["disable_mentions"] = it.asInt().toString() }
            doNotParseLinks?.let { this["dont_parse_links"] = it.asInt().toString() }
            silent?.let { this["silent"] = it.toString() }

            // TODO: 05/05/2024, Danil Nikolaev: add attachments
//            attachments?.let {
//                this["attachment"] = it.joinToString() { attachment ->
//                    attachment.asString(true)
//                }
//            }
        }
}

data class MessagesMarkAsReadRequest(
    val peerId: Int,
    val messageIds: List<Int>?,
    val startMessageId: Int?
) {

    val map: Map<String, String>
        get() = mutableMapOf(
            "peerId" to peerId.toString(),
        )
}

data class MessagesMarkAsImportantRequest(
    val messagesIds: List<Int>,
    val important: Boolean
) {

    val map: Map<String, String>
        get() = mapOf(
            "message_ids" to messagesIds.joinToString(),
            "important" to important.asInt().toString()
        )

}

data class MessagesGetLongPollServerRequest(
    val needPts: Boolean,
    val version: Int
) {

    val map: Map<String, String>
        get() = mapOf(
            "need_pts" to needPts.asInt().toString(),
            "version" to version.toString()
        )
}


data class MessagesPinMessageRequest(
    val peerId: Int,
    val messageId: Int? = null,
    val conversationMessageId: Int? = null
) {

    val map: Map<String, String>
        get() = mutableMapOf(
            "peer_id" to peerId.toString()
        ).apply {
            messageId?.let { this["message_id"] = it.toString() }
            conversationMessageId?.let { this["conversation_message_id"] = it.toString() }
        }

}

data class MessagesUnPinMessageRequest(val peerId: Int) {
    val map: Map<String, String>
        get() = mapOf("peer_id" to peerId.toString())
}

data class MessagesDeleteRequest(
    val peerId: Int,
    val messagesIds: List<Int>? = null,
    val conversationsMessagesIds: List<Int>? = null,
    val isSpam: Boolean? = null,
    val deleteForAll: Boolean? = null
) {

    val map: Map<String, String>
        get() = mutableMapOf(
            "peer_id" to peerId.toString()
        ).apply {
            isSpam?.let { this["spam"] = it.asInt().toString() }
            deleteForAll?.let { this["delete_for_all"] = it.asInt().toString() }
            messagesIds?.let { this["message_ids"] = it.joinToString() }

            conversationsMessagesIds?.let {
                this["conversation_message_ids"] = it.joinToString()
            }
        }
}

data class MessagesEditRequest(
    val peerId: Int,
    val messageId: Int,
    val message: String? = null,
    val lat: Float? = null,
    val long: Float? = null,
    val attachments: List<VkAttachment>? = null,
    val notParseLinks: Boolean = false,
    val keepSnippets: Boolean = true,
    val keepForwardedMessages: Boolean = true
) {

    val map: Map<String, String>
        get() = mutableMapOf(
            "peer_id" to peerId.toString(),
            "message_id" to messageId.toString(),
            "dont_parse_links" to notParseLinks.asInt().toString(),
            "keep_snippets" to keepSnippets.asInt().toString(),
            "keep_forward_messages" to keepForwardedMessages.asInt().toString()
        ).apply {
            message?.let { this["message"] = it }
            lat?.let { this["lat"] = it.toString() }
            long?.let { this["long"] = it.toString() }

            // TODO: 05/05/2024, Danil Nikolaev: add attachments
//            attachments?.let {
//                val attachments =
//                    if (it.isEmpty()) ""
//                    else it.joinToString() { it.asString() }
//                this["attachment"] = attachments
//            }
        }

}


data class MessagesGetByIdRequest(
    val messagesIds: List<Int>,
    val extended: Boolean? = null,
    val fields: String? = null
) {

    val map: Map<String, String>
        get() = mutableMapOf(
            "message_ids" to messagesIds.joinToString(),
        ).apply {
            extended?.let { this["extended"] = it.asInt().toString() }
            fields?.let { this["fields"] = it }
        }
}


data class MessagesGetChatRequest(
    val chatId: Int,
    val fields: String? = null
) {

    val map: Map<String, String>
        get() = mutableMapOf(
            "chat_id" to chatId.toString()
        ).apply {
            fields?.let { this["fields"] = it }
        }
}


data class MessagesGetConversationMembersRequest(
    val peerId: Int,
    val offset: Int? = null,
    val count: Int? = null,
    val extended: Boolean? = null,
    val fields: String? = null
) {

    val map: Map<String, String>
        get() = mutableMapOf(
            "peer_id" to peerId.toString()
        ).apply {
            offset?.let { this["offset"] = it.toString() }
            count?.let { this["count"] = it.toString() }
            extended?.let { this["extended"] = it.toString() }
            fields?.let { this["fields"] = it }
        }

}


data class MessagesRemoveChatUserRequest(
    val chatId: Int,
    val memberId: Int
) {
    val map: Map<String, String>
        get() = mapOf(
            "chat_id" to chatId.toString(),
            "member_id" to memberId.toString()
        )
}
