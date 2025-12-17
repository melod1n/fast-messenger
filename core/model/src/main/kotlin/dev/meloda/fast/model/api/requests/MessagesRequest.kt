package dev.meloda.fast.model.api.requests

import dev.meloda.fast.model.api.asInt
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkMessage

data class MessagesGetHistoryRequest(
    val count: Int? = null,
    val offset: Int? = null,
    val peerId: Long,
    val extended: Boolean? = null,
    val startMessageId: Long? = null,
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
    val peerId: Long,
    val randomId: Long = 0,
    val message: String?,
    val lat: Int? = null,
    val lon: Int? = null,
    val forward: String? = null,
    val stickerId: Long? = null,
    val disableMentions: Boolean? = null,
    val doNotParseLinks: Boolean? = null,
    val silent: Boolean? = null,
    val attachments: List<VkAttachment>? = null,
    val formatData: VkMessage.FormatData? = null
) {

    val map: Map<String, String>
        get() = mutableMapOf(
            "peer_id" to peerId.toString(),
            "random_id" to randomId.toString()
        ).apply {
            message?.let { this["message"] = it }
            lat?.let { this["lat"] = it.toString() }
            lon?.let { this["lon"] = it.toString() }
            forward?.let { this["forward"] = it }
            stickerId?.let { this["sticker_id"] = it.toString() }
            disableMentions?.let { this["disable_mentions"] = it.asInt().toString() }
            doNotParseLinks?.let { this["dont_parse_links"] = it.asInt().toString() }
            silent?.let { this["silent"] = it.toString() }
            formatData?.let {
                this["format_data"] = "{\"version\":\"${formatData.version}\",\"items\":[" +
                        formatData.items.joinToString(separator = ", ") { item ->
                            "{\"type\":\"${item.type}\",\"offset\":${item.offset},\"length\":${item.length}}"
                        } +
                        "]}"
            }

            // TODO: 05/05/2024, Danil Nikolaev: add attachments
//            attachments?.let {
//                this["attachment"] = it.joinToString() { attachment ->
//                    attachment.asString(true)
//                }
//            }
        }
}

data class MessagesMarkAsReadRequest(
    val peerId: Long,
    val startMessageId: Long?
) {

    val map: Map<String, String>
        get() = mutableMapOf(
            "peer_id" to peerId.toString(),
        ).apply {
            startMessageId?.let { this["start_message_id"] = it.toString() }
        }
}

data class MessagesMarkAsImportantRequest(
    val messagesIds: List<Long>,
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
    val peerId: Long,
    val messageId: Long? = null,
    val cmId: Long? = null
) {

    val map: Map<String, String>
        get() = mutableMapOf(
            "peer_id" to peerId.toString()
        ).apply {
            messageId?.let { this["message_id"] = it.toString() }
            cmId?.let { this["conversation_message_id"] = it.toString() }
        }

}

data class MessagesUnpinMessageRequest(val peerId: Long) {
    val map: Map<String, String>
        get() = mapOf("peer_id" to peerId.toString())
}

data class MessagesDeleteRequest(
    val peerId: Long,
    val messagesIds: List<Long>? = null,
    val cmIds: List<Long>? = null,
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

            cmIds?.let {
                this["conversation_message_ids"] = it.joinToString()
            }
        }
}

data class MessagesEditRequest(
    val peerId: Long,
    val cmId: Long?,
    val messageId: Long?,
    val message: String?,
    val lat: Float?,
    val long: Float?,
    val attachments: List<VkAttachment>?,
    val notParseLinks: Boolean,
    val keepSnippets: Boolean,
    val keepForwardedMessages: Boolean
) {

    val map: Map<String, String>
        get() = mutableMapOf(
            "peer_id" to peerId.toString(),
            "dont_parse_links" to notParseLinks.asInt().toString(),
            "keep_snippets" to keepSnippets.asInt().toString(),
            "keep_forward_messages" to keepForwardedMessages.asInt().toString()
        ).apply {
            messageId?.let { this["message_id"] = it.toString() }
            cmId?.let { this["cmid"] = it.toString() }
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
    val peerCmIds: List<Long>?,
    val peerId: Long?,
    val messagesIds: List<Long>?,
    val cmIds: List<Long>?,
    val extended: Boolean? = null,
    val fields: String? = null
) {

    val map: Map<String, String>
        get() = mutableMapOf<String, String>().apply {
            peerCmIds?.let { this["peer_cmids"] = it.joinToString() }
            peerId?.let { this["peer_id"] = it.toString() }
            messagesIds?.let { this["message_ids"] = it.joinToString() }
            cmIds?.let { this["cmids"] = it.joinToString() }
            extended?.let { this["extended"] = it.asInt().toString() }
            fields?.let { this["fields"] = it }
        }
}


data class MessagesGetChatRequest(
    val chatId: Long,
    val fields: String? = null
) {

    val map: Map<String, String>
        get() = mutableMapOf(
            "chat_id" to chatId.toString()
        ).apply {
            fields?.let { this["fields"] = it }
        }
}


data class MessagesGetConvoMembersRequest(
    val peerId: Long,
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
    val chatId: Long,
    val memberId: Long
) {
    val map: Map<String, String>
        get() = mapOf(
            "chat_id" to chatId.toString(),
            "member_id" to memberId.toString()
        )
}

data class MessagesGetHistoryAttachmentsRequest(
    val peerId: Long,
    val extended: Boolean?,
    val count: Int?,
    val offset: Int?,
    val preserveOrder: Boolean?,
    val attachmentTypes: List<String>,
    val cmId: Long,
    val fields: String?
) {

    val map = mutableMapOf(
        "peer_id" to peerId.toString(),
        "attachment_types" to attachmentTypes.joinToString(","),
        "cmid" to cmId.toString()
    ).apply {
        extended?.let { this["extended"] = it.toString() }
        count?.let { this["count"] = it.toString() }
        offset?.let { this["offset"] = it.toString() }
        preserveOrder?.let { this["preserve_order"] = it.toString() }
        fields?.let { this["fields"] = it }
    }
}

data class MessagesCreateChatRequest(
    val userIds: List<Long>?,
    val title: String?
) {

    val map = mutableMapOf<String, String>().apply {
        userIds?.let { this["user_ids"] = it.joinToString(",") }
        title?.let { this["title"] = it }
    }
}
